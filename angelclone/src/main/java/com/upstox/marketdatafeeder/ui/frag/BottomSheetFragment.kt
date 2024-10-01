package com.upstox.marketdatafeeder.ui.frag

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.upstox.ApiClient
import com.upstox.Configuration
import com.upstox.auth.OAuth
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.UserData
import com.upstox.marketdatafeeder.WatchlistAdapter
import com.upstox.marketdatafeeder.databinding.FragmentBottomSheetBinding
import com.upstox.marketdatafeeder.pref.AccessTokenPref
import com.upstox.marketdatafeeder.pref.OhlcData
import com.upstox.marketdatafeeder.pref.WachlistData
import com.upstox.marketdatafeeder.rpc.proto.MarketFeeder
import io.swagger.client.api.WebsocketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding
    private lateinit var client: WebSocketClient
    companion object {
        const val ARG_WATCHLIST_DATA = "arg_watchlist_data"

        fun newInstance(watchlistData: WachlistData): BottomSheetFragment {
            val fragment = BottomSheetFragment()
            val args = Bundle()
            args.putParcelable(ARG_WATCHLIST_DATA, watchlistData)
            fragment.arguments = args
            return fragment
        }


    }
    private var isFetchingEnabled = true // Flag to control fetching

    private val upstoxApiUrlOhlc = "https://api.upstox.com/v2/market-quote/ohlc"
    private var instrumentKeyOhlc : String = ""
    lateinit var accessToken : String  // Replace with your actual access token

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val watchlistData = arguments?.getParcelable<WachlistData>(ARG_WATCHLIST_DATA)

        // Update UI with watchlistData in the bottom sheet
        watchlistData?.let {

            binding.tvSymbolName.text = it.firstName
            binding.tvSymbolDescription.text = it.lastName
            binding.tvSegmentType.text = it.nse


            binding.btnBuyLong.setOnClickListener {
                showBottomSheet(watchlistData)
                stopFetchingOhlcData()
                dismiss()
                true
            }



            val accessTokenManager = AccessTokenPref(requireContext())

            // Use the access token as needed in other activities
             accessToken = accessTokenManager.accessToken.toString()

            val instrumentKeys = listOf("${watchlistData.marketId}")

            // Perform other non-WebSocket-related initialization here
            instrumentKeyOhlc = watchlistData.marketId

            if (watchlistData.type == "1" || watchlistData.type == "2"){

                // Initialize WebSocketManager only when needed
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val authenticatedClient = authenticateApiClient(accessToken)
                        val serverUri = getAuthorizedWebSocketUri(authenticatedClient)

                        launch(Dispatchers.Main) {
                            try {
                                client = createWebSocketClient(serverUri, instrumentKeys,watchlistData)
                                client.connect()
                            } catch (e: Exception) {
                                handleWebSocketClientError("Error creating WebSocket client: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        handleAuthError("Error authenticating API client: ${e.message}")
                    }
                }


            } else {  startFetchingOhlcData()}




        }





    }



    private fun startFetchingOhlcData() {

        val watchlistData = arguments?.getParcelable<WachlistData>(ARG_WATCHLIST_DATA)
        handler.postDelayed({
             if (isFetchingEnabled) {
                 watchlistData?.let { fetchOhlcData(it) } // Schedule the next execution after 1.5 seconds
                 startFetchingOhlcData() // Schedule the next execution after 4 seconds
            }

        }, 3000)
    }

    private fun stopFetchingOhlcData() { isFetchingEnabled = false }


    override fun onDestroyView() {
        super.onDestroyView()
        stopFetchingOhlcData()
        handler.removeCallbacksAndMessages(null) // Remove all callbacks to avoid memory leaks
    }

    override fun onPause() {
        super.onPause()
        stopFetchingOhlcData()
        handler.removeCallbacksAndMessages(null) // Remove all callbacks to avoid memory leaks
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
                handleBinaryMessage(bytes, wachlistData)
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


    private fun handleBinaryMessage(bytes: ByteBuffer, userData: WachlistData) {



        try {
            val feedResponse = MarketFeeder.FeedResponse.parseFrom(bytes.array())
            val feedResponse2 = MarketFeeder.FeedResponse.parseFrom(bytes.array())


            Log.d("WebSocket","$feedResponse")

            if (userData.type == "1"){

                val ltp0 = feedResponse.feeds["${userData.marketId}"]?.ff?.indexFF?.ltpc?.ltp
                val close = feedResponse.feeds["${userData.marketId}"]?.ff?.indexFF?.ltpc?.cp

                val marketId = userData.marketId
                val marketFF = feedResponse2.feeds[marketId]?.ff?.indexFF


                (context as? Activity)?.runOnUiThread {

                    if (ltp0!! >= close!!){
                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
                        binding.tvPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
                        binding.tvPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        binding.tvPrice.compoundDrawablePadding = 8
                    }else{

                        val resolvedColor = ContextCompat.getColor(requireActivity(), R.color.sell_red)
                        binding.tvPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                        binding.tvPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        binding.tvPrice.compoundDrawablePadding = 8

                    }




                    if (marketFF != null) {
                        val ohlcList = marketFF.marketOHLC.ohlcList

                        for (ohlc in ohlcList) {
                            val interval = ohlc.interval
                            val open = ohlc.open
                            val high = ohlc.high
                            val low = ohlc.low
                            val close = ohlc.close
                            val volume = ohlc.volume
                            val ts = ohlc.ts


                            binding.textView15.text = open.toString()
                            binding.textView14.text = high.toString()
                            binding.textView17.text = low.toString()
                            binding.textView16.text = close.toString()



                            // Now you can use 'interval', 'open', 'high', 'low', 'close', 'volume', 'ts' as needed
                            Log.d("OHLC Data", "Interval: $interval, Open: $open, High: $high, Low: $low, Close: $close, Volume: $volume, Timestamp: $ts")
                        }
                    } else {
                        Log.d("OHLC Data", "MarketFF data not available for marketId: $marketId")
                    }


                    binding.tvPrice.text = "₹ "+formatIntWithDecimal(ltp0!!)
                    val newPrice = ltp0 - close!!
                    val perc = newPrice / 100
                    binding.tvChangeInPrice.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"



                }






            }else {

                val ltp0 = feedResponse.feeds["${userData.marketId}"]?.ff?.marketFF?.ltpc?.ltp
                val close = feedResponse.feeds["${userData.marketId}"]?.ff?.marketFF?.ltpc?.cp

                val marketId = userData.marketId
                val marketFF = feedResponse2.feeds[marketId]?.ff?.marketFF


                (context as? Activity)?.runOnUiThread {

                    if (ltp0!! >= close!!){
                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
                       binding.tvPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
                       binding.tvPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                       binding.tvPrice.compoundDrawablePadding = 8
                    }else{

                        val resolvedColor = ContextCompat.getColor(requireActivity(), R.color.sell_red)
                       binding.tvPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                       binding.tvPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                       binding.tvPrice.compoundDrawablePadding = 8

                    }




                    if (marketFF != null) {
                        val ohlcList = marketFF.marketOHLC.ohlcList

                        for (ohlc in ohlcList) {
                            val interval = ohlc.interval
                            val open = ohlc.open
                            val high = ohlc.high
                            val low = ohlc.low
                            val close = ohlc.close
                            val volume = ohlc.volume
                            val ts = ohlc.ts


                            binding.textView15.text = open.toString()
                            binding.textView14.text = high.toString()
                            binding.textView17.text = low.toString()
                            binding.textView16.text = close.toString()



                            // Now you can use 'interval', 'open', 'high', 'low', 'close', 'volume', 'ts' as needed
                            Log.d("OHLC Data", "Interval: $interval, Open: $open, High: $high, Low: $low, Close: $close, Volume: $volume, Timestamp: $ts")
                        }
                    } else {
                        Log.d("OHLC Data", "MarketFF data not available for marketId: $marketId")
                    }


                   binding.tvPrice.text = "₹ "+formatIntWithDecimal(ltp0)
                    val newPrice = ltp0 - close
                    val perc = newPrice / 100
                    binding.tvChangeInPrice.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"

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
        val bottomSheetFragment = BottomSheetBuyFragment.newInstance(wachlistData)
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
                Log.d("OHLC Response", jsonResponse)

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

    private fun fetchOhlcData(userData: WachlistData) {
        GlobalScope.launch(Dispatchers.IO) {
            val ohlcResult = getOhlcData(userData)

            withContext(Dispatchers.Main) {
                if (ohlcResult != null) {
                    // Handle the OHLC data here
                    if (ohlcResult.lastPrice!! >= ohlcResult.close!!){
                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
                        binding.tvPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
                        binding.tvPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        binding.tvPrice.compoundDrawablePadding = 8

                        val newPrice = ohlcResult.lastPrice - ohlcResult.close
                        val perc = newPrice / ohlcResult.close * 100
                        binding.tvChangeInPrice.text =  "+${formatIntWithDecimal(newPrice)} (+${formatIntWithDecimal(perc)}%)"

                    }else{

                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
                        binding.tvPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                        binding.tvPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        binding.tvPrice.compoundDrawablePadding = 8

                        val newPrice = ohlcResult.lastPrice - ohlcResult.close
                        val perc = newPrice / ohlcResult.close * 100
                        binding.tvChangeInPrice.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"

                    }

                    binding.tvPrice.text =  "₹"+formatIntWithDecimal(ohlcResult.lastPrice)
                    binding.textView15.text = ohlcResult.open.toString()
                    binding.textView14.text = ohlcResult.high.toString()
                    binding.textView17.text = ohlcResult.low.toString()
                    binding.textView16.text = ohlcResult.close.toString()


                    Log.d("backgroundrun", "run bottom sheet 1")

                    Log.d("OHLC Data", "Open: ${ohlcResult.open}, High: ${ohlcResult.high}, Low: ${ohlcResult.low}, Close: ${ohlcResult.close}, Last Price: ${ohlcResult.lastPrice}")
                } else {
                    Log.e("OHLC Data", "Error: Unable to retrieve OHLC data.")
                }
            }
        }
    }





}
