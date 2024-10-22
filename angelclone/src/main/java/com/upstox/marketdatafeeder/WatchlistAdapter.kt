package com.upstox.marketdatafeeder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.upstox.ApiClient
import com.upstox.Configuration
import com.upstox.auth.OAuth
import com.upstox.marketdatafeeder.pref.AccessTokenPref
import com.upstox.marketdatafeeder.pref.OhlcData
import com.upstox.marketdatafeeder.pref.WachlistData
import com.upstox.marketdatafeeder.pref.WatchlistSharedPreferencesManager
import com.upstox.marketdatafeeder.rpc.proto.MarketFeeder
import com.upstox.marketdatafeeder.ui.frag.BottomSheetFragment
import io.swagger.client.api.WebsocketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class WatchlistAdapter(private val itemList: List<WachlistData>, private val context: Context) : RecyclerView.Adapter<WatchlistAdapter.ViewHolder>() {

    private lateinit var client: WebSocketClient
    private val upstoxApiUrlOhlc = "https://api.upstox.com/v2/market-quote/ohlc"
    private var instrumentKeyOhlc : String = ""
    private var instrumentKeyOhlc2 : String = ""
    lateinit var accessToken : String  // Replace with your actual access token
    private val handler = Handler(Looper.getMainLooper())
    private var backgroundJob: Job? = null
    private var isFetchingEnabled = true // Flag to control fetching


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvScriptTitle)
        val nseTextView: TextView = itemView.findViewById(R.id.tvScriptSegment)
        val lastname: TextView = itemView.findViewById(R.id.tvScriptSubTitle)
        val ltp: TextView = itemView.findViewById(R.id.tvScriptPrice)
        val ltpp: TextView = itemView.findViewById(R.id.tvScriptPerChange)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.watchlist_item, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userData = itemList[position]
        holder.nameTextView.text = userData.firstName
        holder.nseTextView.text = userData.nse
        holder.lastname.text = userData.lastName

        val accessTokenManager = AccessTokenPref(context)

        accessToken =  accessTokenManager.accessToken.toString()
        // Use the access token as needed in other activities

        val instrumentKeys = listOf("${userData.marketId}")
        instrumentKeyOhlc = userData.marketId.toString()
        // Perform other non-WebSocket-related initialization here

        // Initialize WebSocketManager only when needed

        if (userData.type == "1" || userData.type == "2"){

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val authenticatedClient = authenticateApiClient(accessToken)
                    val serverUri = getAuthorizedWebSocketUri(authenticatedClient)

                    launch(Dispatchers.Main) {
                        try {
                            client = createWebSocketClient(serverUri, instrumentKeys, holder,userData)
                            client.connect()
                        } catch (e: Exception) {
                            handleWebSocketClientError("Error creating WebSocket client: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    handleAuthError("Error authenticating API client: ${e.message}")
                }
            } }

        else {
            startFetchingOhlcData(holder, userData)
        }

        holder.itemView.setOnClickListener {
            stopFetchingOhlcData()
            showBottomSheet(userData)
            true
        }



        holder.nseTextView.setOnClickListener {
            val sharedPreferencesManager = WatchlistSharedPreferencesManager(context)
            sharedPreferencesManager.removeItemFromWatchlist(userData)
            // Notify the adapter that the data set has changed
            notifyDataSetChanged()
        }
        
    }

    private fun startFetchingOhlcData(holder: ViewHolder, userData: WachlistData) {
        handler.postDelayed({

            if (isFetchingEnabled) {
                fetchOhlcData(holder, userData)
                startFetchingOhlcData(holder, userData) // Schedule the next execution after 4 seconds
            }
        }, 2500)
    }

      fun stopFetchingOhlcData() {
        // Stop fetching data for the item
        isFetchingEnabled = false
        // Update UI if necessary
        // For example, change button text or color
    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun authenticateApiClient(accessToken: String): ApiClient {
        val defaultClient = Configuration.getDefaultApiClient()
        val oAuth = defaultClient.getAuthentication("OAUTH2") as OAuth
        oAuth.accessToken = accessToken
        return defaultClient
    }

    private fun getAuthorizedWebSocketUri(authenticatedClient: ApiClient): URI {
        val websocketApi = WebsocketApi(authenticatedClient)
        val response = websocketApi.getMarketDataFeedAuthorize("2.0")
        return URI.create(response.data.authorizedRedirectUri)
    }

    private fun createWebSocketClient(
        serverUri: URI,
        instrumentKeys: List<String>,
        holder: ViewHolder,
    wachlistData: WachlistData): WebSocketClient {
        return object : WebSocketClient(serverUri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                Log.d("WebSocket", "Opened connection")
                sendSubscriptionRequest(this, instrumentKeys)
            }

            override fun onMessage(message: String) {
                Log.d("WebSocket", "Received: $message")
            }

            override fun onMessage(bytes: ByteBuffer) {
                handleBinaryMessage(bytes, holder,  wachlistData)
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.d("WebSocket", "Connection closed by ${if (remote) "remote peer" else "us"}. Info: $reason")
            }

            override fun onError(ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun sendSubscriptionRequest(client: WebSocketClient, instrumentKeys: List<String>) {
        val requestObject = constructSubscriptionRequest(instrumentKeys)
        val binaryData = requestObject.toString().toByteArray(StandardCharsets.UTF_8)
        client.send(binaryData)
    }

    private fun constructSubscriptionRequest(instrumentKeys: List<String>): JsonObject {
        val dataObject = JsonObject().apply {
            addProperty("mode", "full")

            val instrumentArray = instrumentKeys.fold(JsonArray()) { acc, item ->
                acc.add(item)
                acc
            }
            add("instrumentKeys", instrumentArray)
        }

        return JsonObject().apply {
            addProperty("guid", "someguid")
            addProperty("method", "sub")
            add("data", dataObject)
        }
    }

    private fun handleBinaryMessage(bytes: ByteBuffer, holder: ViewHolder,userData: WachlistData) {
        try {
            val feedResponse = MarketFeeder.FeedResponse.parseFrom(bytes.array())

            Log.d("WebSocket","$feedResponse")


            if (userData.type == "1"){

                val ltp0 = feedResponse.feeds["${userData.marketId}"]?.ff?.indexFF?.ltpc?.ltp

                val close = feedResponse.feeds["${userData.marketId}"]?.ff?.indexFF?.ltpc?.cp

                (context as? Activity)?.runOnUiThread {

                    if (ltp0!! >= close!!){
                        val resolvedColor = ContextCompat.getColor(context, R.color.buy_green)
                        holder.ltp.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = context.resources.getDrawable(R.drawable.ic_arrow_gain)
                        holder.ltp.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        holder.ltp.compoundDrawablePadding = 8
                    }else{
                        val resolvedColor = ContextCompat.getColor(context, R.color.sell_red)
                        holder.ltp.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = context.resources.getDrawable(R.drawable.ic_arrow_loss)
                        holder.ltp.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        holder.ltp.compoundDrawablePadding = 8
                    }

                   holder.ltp.text = formatIntWithDecimal(ltp0!!)

                    val newPrice = ltp0 - close
                    val perc = newPrice / close * 100
                    holder.ltpp.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"

                    // Continue handling as needed...
                }
            }else{

                val ltp0 = feedResponse.feeds["${userData.marketId}"]?.ff?.marketFF?.ltpc?.ltp

                val close = feedResponse.feeds["${userData.marketId}"]?.ff?.marketFF?.ltpc?.cp

                (context as? Activity)?.runOnUiThread {

                    if (ltp0!! >= close!!){
                        val resolvedColor = ContextCompat.getColor(context, R.color.buy_green)
                        holder.ltp.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = context.resources.getDrawable(R.drawable.ic_arrow_gain)
                        holder.ltp.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        holder.ltp.compoundDrawablePadding = 8
                    }else{

                        val resolvedColor = ContextCompat.getColor(context, R.color.sell_red)
                        holder.ltp.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = context.resources.getDrawable(R.drawable.ic_arrow_loss)
                        holder.ltp.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        holder.ltp.compoundDrawablePadding = 8

                    }

                    holder.ltp.text = formatIntWithDecimal(ltp0!!)

                    val newPrice = ltp0 - close
                    val perc = newPrice / close * 100
                    holder.ltpp.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"

                    // Continue handling as needed...
                }

            }

        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing binary message: ${e.message}")
        }

    }

    private fun handleWebSocketClientError(errorMessage: String) {
        (context as? Activity)?.runOnUiThread {
            // Handle the WebSocket client error on the UI thread
            Log.e("WebSocket", errorMessage)
        }
    }

    private fun handleAuthError(errorMessage: String) {
        (context as? Activity)?.runOnUiThread {
            // Handle the authentication error on the UI thread
            Log.e("WebSocket", errorMessage)
        }
    }


    fun formatIntWithDecimal(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("##,##0.00", symbols)
        return formatter.format(value)
    }

    private fun showBottomSheet(wachlistData: WachlistData) {
        val bottomSheetFragment = BottomSheetFragment.newInstance(wachlistData)
        bottomSheetFragment.show((context as? FragmentActivity)?.supportFragmentManager!!, bottomSheetFragment.tag)
    }



    private fun getOhlcData(userData: WachlistData): OhlcData? {
        var open: Double? = null
        var high: Double? = null
        var low: Double? = null
        var close: Double? = null
        var lastPrice: Double? = null

        try {
            val interval = "I1"  // Update this with the correct interval ("1d" or "I1" or "I30")

            val url = URL("$upstoxApiUrlOhlc?instrument_key=$instrumentKeyOhlc&interval=$interval")
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

            // Set the request method to GET
            connection.requestMethod = "GET"

            // Set headers
            connection.setRequestProperty("Api-Version", "2.0")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Accept", "application/json")

            // Get the response code
            val responseCode: Int = connection.responseCode



            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonResponse = reader.readLines().joinToString("\n")
                reader.close()

                // Log the JSON response for debugging




                // Parse the JSON response
                val jsonObject = JSONObject(jsonResponse)
                val dataObject = jsonObject.getJSONObject("data")
                val instrumentObject = dataObject.getJSONObject("NSE_FO:${userData.marketId2}")
                val ohlcObject = instrumentObject.getJSONObject("ohlc")
                open = ohlcObject.getDouble("open")
                high = ohlcObject.getDouble("high")
                low = ohlcObject.getDouble("low")
                close = ohlcObject.getDouble("close")
                lastPrice = instrumentObject.getDouble("last_price")

                Log.d("OHLC F&O", "$dataObject")


            } else {
                // Log the error response
                val errorStream = connection.errorStream
                if (errorStream != null) {
                    val reader = BufferedReader(InputStreamReader(errorStream))
                    val errorResponse = reader.readLines().joinToString("\n")
                    Log.e("API Request (OHLC)", "Failed with response code: $responseCode, Error: $errorResponse")
                    reader.close()
                } else {
                    Log.e("API Request (OHLC)", "Failed with response code: $responseCode")
                }
            }

            // Disconnect the connection
            connection.disconnect()
        } catch (e: Exception) {
            Log.e("API Request (OHLC)", "Error: ${e.message}")
        }

        return OhlcData(open, high, low, close, lastPrice)
    }

    private fun fetchOhlcData(holder: ViewHolder,userData: WachlistData) {
        GlobalScope.launch(Dispatchers.IO) {

            val ohlcResult = getOhlcData(userData)

            withContext(Dispatchers.Main) {

                if (ohlcResult!!.lastPrice != null) {
                    // Handle the OHLC data here

                    if (ohlcResult.lastPrice!! >= ohlcResult.close!!){
                        val resolvedColor = ContextCompat.getColor(context, R.color.buy_green)
                        holder.ltp.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = context.resources.getDrawable(R.drawable.ic_arrow_gain)
                        holder.ltp.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        holder.ltp.compoundDrawablePadding = 8
                        val newPrice = ohlcResult.lastPrice - ohlcResult.close
                        val perc = newPrice / ohlcResult.close * 100
                        holder.ltpp.text =  "+${formatIntWithDecimal(newPrice)} (+${formatIntWithDecimal(perc)}%)"

                    }else{

                        val resolvedColor = ContextCompat.getColor(context, R.color.sell_red)
                        holder.ltp.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = context.resources.getDrawable(R.drawable.ic_arrow_loss)
                        holder.ltp.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        holder.ltp.compoundDrawablePadding = 8

                        val newPrice = ohlcResult.lastPrice - ohlcResult.close
                        val perc = newPrice / ohlcResult.close * 100
                        holder.ltpp.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"


                    }

                    holder.ltp.text =  formatIntWithDecimal(ohlcResult.lastPrice)
                    holder.nameTextView.text = userData.firstName
                    holder.lastname.text = userData.lastName
                    holder.nseTextView.text = userData.nse


//                    percent change


                    Log.d("backgroundrun", "run adapter")

                    Log.d("OHLC F&O", "Open: ${ohlcResult.open}, High: ${ohlcResult.high}, Low: ${ohlcResult.low}, Close: ${ohlcResult.close}, Last Price: ${ohlcResult.lastPrice}")
                } else {
                    Log.e("OHLC Data", "Error: Unable to retrieve OHLC data.")
                }





            }
        }
    }





}
