package com.upstox.marketdatafeeder.ui.notifications

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.SharedPreferencesManager
import com.upstox.marketdatafeeder.UserData
import com.upstox.marketdatafeeder.databinding.FragmentNotificationsBinding
import com.upstox.marketdatafeeder.pref.AccessTokenPref
import com.upstox.marketdatafeeder.pref.OhlcData
import com.upstox.marketdatafeeder.ui.frag.BottomSheetExitFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.java_websocket.client.WebSocketClient
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    var todaypnl: Double = 0.0
    private val upstoxApiUrlOhlc = "https://api.upstox.com/v2/market-quote/ohlc"
    private var instrumentKeyOhlc: String = ""
    private var instrumentKeyOhlc2: String = ""
    lateinit var accessToken: String  // Replace with your actual access token
    private lateinit var client: WebSocketClient

    private val handler = Handler(Looper.getMainLooper())
    private var isFetchingEnabled = true // Flag to control fetching
    private var isChecking = false
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Load the blink animation
        val blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink_anim)

        // Apply the animation to the TextView
        binding.newblink.startAnimation(blinkAnimation)


        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        itemcount()

        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white)


        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.black),
            ContextCompat.getColor(requireContext(), R.color.black),
            ContextCompat.getColor(requireContext(), R.color.black)
        )

        binding.swipeRefreshLayout.setOnRefreshListener {

            Handler().postDelayed({

                // Stop the refresh animation
                binding.swipeRefreshLayout.isRefreshing = false
            }, 500) // 1000 milliseconds = 1 second delay
        }


        val accessTokenManager = AccessTokenPref(requireContext())
        // Use the access token as needed in other activities
        accessToken = accessTokenManager.accessToken.toString()

        if (userData != null){


            val instrumentKeys = listOf("${userData.marketid}")
            instrumentKeyOhlc = userData.marketid

            if (userData.type == "1" || userData.type == "2") {
                Toast.makeText(requireContext(), "Equity Segment Not Active", Toast.LENGTH_SHORT).show() } else {   startFetchingOhlcData(requireContext())  }

        }
        return root
    }

    private fun startFetchingOhlcData(context: Context) {

        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        handler.postDelayed({

            if (isFetchingEnabled) {
                startFetchingOhlcData(context) // Schedule the next execution after 1.5 seconds
                if (userData != null) {
                    fetchOhlcData(userData)
                }
            }


        }, 2000)
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
        stopChecking()
        handler.removeCallbacksAndMessages(null) // Remove all callbacks to avoid memory leaks
    }

    fun retrivedata(context: Context, ltp: Double,close : Double) {



        val sharedPreferencesManager = SharedPreferencesManager(context) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()

        // running position delete

        binding.textView.setOnClickListener {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())
            sharedPreferencesManager.clearUserData()
        }
        binding.textVieww.setOnClickListener {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())
            sharedPreferencesManager.clearUserData2()
        }


        if (userData != null) {

            binding.constraintLayout3.visibility = View.VISIBLE
            binding.hideposition.visibility = View.GONE
            binding.cardView9.visibility = View.VISIBLE

            if (userData.type == "buy") {

                if (userData.radio == "I") {
                    binding.exgTag.text = "INTRADAY"
                } else {
                    binding.exgTag.text = "CF"
                }



                val buy = ContextCompat.getColor(context, R.color.buy_green)
                binding.tvTagBuySell.text = "BUY"
                binding.tvTagBuySell.setTextColor(buy)
                binding.tvTagBuySell.setBackgroundResource(R.drawable.bg_buy_green_12_rectangle)

                binding.layout1.visibility = View.VISIBLE

                binding.textView.text = userData.name
                binding.textView24.text = userData.last
                binding.textView26.text = formatIntWithDecimal(userData.avg.toDouble())
                binding.textView22.text = userData.lot
                binding.tvLotSize.text = "Lot (1 Lot = ${userData.qty})"
                binding.textViewsda.text = userData.name
                binding.textView24sda.text = userData.last
                binding.nsetag.text = userData.nse


                var currentValue = userData.avg.toDouble()
                val minValue = "-${userData.ltpmin}"
                val maxValue = userData.ltpmax


//                 trade close



                // Get the random value from the list
                val fluctuation = ltp // ltp value

                currentValue = fluctuation

                val fixvalue = currentValue - userData.avg.toDouble()

                var qty = userData.qty.toInt() * userData.lot.toInt()

                val f = fixvalue * qty

                var minus = currentValue - userData.avg.toDouble()

                var devide = minus / userData.avg.toDouble()

                val perc =  devide * 100.0





                binding.layout1.setOnClickListener {
                    showBottomSheet(userData)
                }

                // Update the UI with the new value
                if (currentValue < userData.avg.toDouble()) {

                    val textColor = ContextCompat.getColor(context, R.color.red2)
                    binding.textView27.setTextColor(textColor)
                    binding.totalgainimg.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP)

                    val ltp = formatIntWithDecimal(currentValue)
                    val fluct = formatIntWithDecimal(f)

                    binding.textView29.text = ltp
                    binding.textView27.text = "${fluct}"
                    binding.ltpprecent.text = "(${formatIntWithDecimal(perc)}%)"

                    todaypnl = f * -1.0

                    if (userData2 == null) {

                        binding.constraintLayout5.setBackgroundResource(R.drawable.bg_rounded_corner_sell_red12_4r)
                        binding.totalgain.text = "Total"
                        val fin = formatIntWithDecimal(f)
                        val formattedLoss = if (f < 0) "- ₹${fin.substring(1)}" else "₹$fin" // Handle '-' sign
                        binding.tpl.text = formattedLoss

                    }


                } else {

                    val textColor = ContextCompat.getColor(context, R.color.green)
                    binding.textView27.setTextColor(textColor)
                    binding.totalgainimg.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP)

                    val ltp = formatIntWithDecimal(currentValue)
                    val fluct = formatIntWithDecimal(f)

                    binding.textView29.text = ltp
                    binding.textView27.text = "+${fluct}"
                    binding.ltpprecent.text = "(+${formatIntWithDecimal(perc)}%)"

                    todaypnl = f

                    if (userData2 == null) {
                        binding.constraintLayout5.setBackgroundResource(R.drawable.bg_buy_green_12_rectangle)
                        binding.totalgain.text = "Total"
                        val fin = formatIntWithDecimal(f)

                        binding.tpl.text = "+₹$fin"
                    }

                }
            }
        }
    }

    fun formatIntWithDecimal(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(value.toDouble())
    }

    fun itemcount() {

        var itemcount = 0
        var closecount = 0

        val sharedPreferencesManager =
            SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()

        if (userData != null) {
            itemcount += 1
            if (userData.bal == "y") { closecount += 1 } }

        if (userData2 != null) {
            itemcount += 1
            if (userData2.bal == "y") {
                closecount += 1 } }

        if (userData3 != null) {
            itemcount += 1
            if (userData3.bal == "y") {
                closecount += 1 } }

        if (itemcount <= 0) { return }

    }


    private fun showBottomSheet(wachlistData: UserData) {
        val bottomSheetFragment = BottomSheetExitFragment.newInstance(wachlistData)
        bottomSheetFragment.show((context as? FragmentActivity)?.supportFragmentManager!!, bottomSheetFragment.tag) }



    private suspend fun getOhlcData(userData: UserData, retryCount: Int = 0): OhlcData? {
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
                val instrumentObject = dataObject.getJSONObject("NSE_FO:${userData.marketid2}")
                val ohlcObject = instrumentObject.getJSONObject("ohlc")
                open = ohlcObject.getDouble("open")
                high = ohlcObject.getDouble("high")
                low = ohlcObject.getDouble("low")
                close = ohlcObject.getDouble("close")
                lastPrice = instrumentObject.getDouble("last_price")

            } else if (responseCode == 429 && retryCount < 3) {
                // Handle rate limiting by retrying with exponential backoff
                val backoffTime = (2.0.pow(retryCount.toDouble()) * 1000).toLong()
                delay(backoffTime)
                return getOhlcData(userData, retryCount + 1)
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

        return if (open != null && high != null && low != null && close != null && lastPrice != null) {
            OhlcData(open, high, low, close, lastPrice)
        } else {
            null
        }
    }

    private fun fetchOhlcData(userData: UserData) {
        GlobalScope.launch(Dispatchers.IO) {
            val ohlcResult = getOhlcData(userData)

            withContext(Dispatchers.Main) {
                if (ohlcResult != null) {
                    // Handle the OHLC data here

                    val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                    val userData = sharedPreferencesManager.getData()

                    if (ohlcResult.lastPrice!! >= ohlcResult.close!!) {
                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
                        binding.textView29.setTextColor(resolvedColor)

                        if (userData?.bal == "y") {
                            binding.textView29sda.text = formatIntWithDecimal(ohlcResult.lastPrice)
                            binding.textView29sda.setTextColor(resolvedColor)

                            val newPrice = ohlcResult.lastPrice - ohlcResult.close
                            val perc = newPrice / ohlcResult.close * 100

                            binding.ltpprecentsda.text = "(+${formatIntWithDecimal(perc)}%)"
                        }

                    } else {
                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
                        binding.textView29.setTextColor(resolvedColor)

                        if (userData?.bal == "y") {
                            binding.textView29sda.text = formatIntWithDecimal(ohlcResult.lastPrice)
                            binding.textView29sda.setTextColor(resolvedColor)

                            val newPrice = ohlcResult.lastPrice - ohlcResult.close
                            val perc = newPrice / ohlcResult.close * 100

                            binding.ltpprecentsda.text = "(${formatIntWithDecimal(perc)}%)"
                        }
                    }

                    if (userData?.bal != "y") {
                        retrivedata(requireContext(), ohlcResult.lastPrice, ohlcResult.close)
                    }

                    Log.d("backgroundrun", "run bottom sheet buy 2")

                } else {
                    Log.e("OHLC Data", "Error: Unable to retrieve OHLC data.")
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        startChecking()
    }

    private fun startChecking() {
        isChecking = true
        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        // Start checking every second
        handler.postDelayed(checkUserData, 2000)
    }

    private fun stopChecking() {
        isChecking = false
        // Remove pending callbacks to stop checking
        handler.removeCallbacks(checkUserData)
    }


    private val checkUserData = object : Runnable {
        override fun run() {
            // Check if fragment is still active and checking is enabled
            if (!isChecking || !isAdded) return

            val userData = sharedPreferencesManager.getData()

            if (userData != null) {
                if ( userData.bal == "y"){
                    fetchOhlcData(userData)
                    stopFetchingOhlcData()
                    binding.layout1.visibility = View.GONE
                    binding.linearLayout3.visibility = View.VISIBLE
                    binding.layout11.visibility = View.VISIBLE
                    binding.hideposition.visibility = View.GONE
                    binding.cardView9.visibility = View.VISIBLE




                    binding.linearLayout3.setOnClickListener {
                        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
                        sharedPreferencesManager.clearUserData()
                    }




                    if (userData.radio == "I"){

                        binding.lytTag.text = "INTRADAY"

                    }else{

                        binding.lytTag.text = "CF"

                    }



                    binding.textView26sda.text = formatIntWithDecimal(userData.avg.toDouble())
                    binding.textView22sda.text = userData.lot
                    binding.textView24sda.text = userData.last

                    binding.textViewsda.text = userData.name





                    val fixvalue = userData.ltp.toDouble() - userData.avg.toDouble()

                    var qty = userData.qty.toInt() * userData.lot.toInt()

                    val f = fixvalue * qty

                    var minus = userData.ltp.toDouble() - userData.avg.toDouble()

                    var devide = minus / userData.avg.toDouble()

                    val ltppercent =  devide * 100.0

                    val textColor = ContextCompat.getColor(requireContext(), R.color.green)



                    if (userData.ltp.toDouble() < userData.avg.toDouble()) {

                        val textColor = ContextCompat.getColor(requireContext(), R.color.red2)
                        binding.textView27sda.setTextColor(textColor)
                        binding.totalgainimg.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP)


                        val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                        val fluct = formatIntWithDecimal(f)
                        val ltpper = formatIntWithDecimal(ltppercent)

                        binding.txtSqPrice.text = ltp
                        binding.textView27sda.text = "${fluct}"

                        todaypnl = f*-1.0



                        binding.constraintLayout3.visibility = View.GONE
                        binding.constraintLayout5.setBackgroundResource(R.drawable.bg_rounded_corner_sell_red12_4r)
                        binding.totalgain.text = "Total"
                        val fin = formatIntWithDecimal(f)
                        val formattedLoss = if (f < 0) "- ₹${fin.substring(1)}" else "₹$fin" // Handle '-' sign
                        binding.tpl.text = formattedLoss





                    } else {


                        val textColor = ContextCompat.getColor(requireContext(), R.color.green)
                        binding.textView27sda.setTextColor(textColor)
                        binding.totalgainimg.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP)



                        val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                        val fluct = formatIntWithDecimal(f)
                        val ltpper = formatIntWithDecimal(ltppercent)


                        binding.txtSqPrice.text = ltp
                        binding.textView27sda.text = "+${fluct}"

                        todaypnl = f



                        binding.constraintLayout3.visibility = View.GONE
                        binding.constraintLayout5.setBackgroundResource(R.drawable.bg_buy_green_12_rectangle)
                        binding.totalgain.text = "Total"

                        val fin = formatIntWithDecimal(f)

                        binding.tpl.text = "+₹$fin"



                    }



                }
                 } else { Log.d("data","null") }

            // Schedule next check
            handler.postDelayed(this, 2000)
        }
    }




}




















// websocket equity data code

//private fun authenticateApiClient(accessToken: String): ApiClient {
//    val defaultClient = Configuration.getDefaultApiClient()
//    val oAuth = defaultClient.getAuthentication("OAUTH2") as OAuth
//    oAuth.accessToken = accessToken
//    return defaultClient
//}
//
//private fun getAuthorizedWebSocketUri(authenticatedClient: ApiClient): URI {
//    val websocketApi = WebsocketApi(authenticatedClient)
//    val response = websocketApi.getMarketDataFeedAuthorize("2.0")
//    return URI.create(response.data.authorizedRedirectUri)
//}
//
//private fun createWebSocketClient(serverUri: URI, instrumentKeys: List<String>, wachlistData: UserData): WebSocketClient {
//    return object : WebSocketClient(serverUri) {
//        override fun onOpen(handshakedata: ServerHandshake) {
//            Log.d("WebSocket", "Opened connection")
//            sendSubscriptionRequest(this, instrumentKeys) }
//
//        override fun onMessage(message: String) {
//            Log.d("WebSocket", "Received: $message") }
//
//        override fun onMessage(bytes: ByteBuffer) {
//            handleBinaryMessage(bytes, wachlistData) }
//
//        override fun onClose(code: Int, reason: String, remote: Boolean) {
//            Log.d("WebSocket", "Connection closed by ${if (remote) "remote peer" else "us"}. Info: $reason") }
//
//        override fun onError(ex: Exception) {
//            ex.printStackTrace() }
//    }
//}
//
//private fun sendSubscriptionRequest(client: WebSocketClient, instrumentKeys: List<String>) {
//    val requestObject = constructSubscriptionRequest(instrumentKeys)
//    val binaryData = requestObject.toString().toByteArray(StandardCharsets.UTF_8)
//    client.send(binaryData) }
//
//private fun constructSubscriptionRequest(instrumentKeys: List<String>): JsonObject {
//    val dataObject = JsonObject().apply {
//        addProperty("mode", "full")
//        val instrumentArray = instrumentKeys.fold(JsonArray()) { acc, item ->
//            acc.add(item)
//            acc}
//        add("instrumentKeys", instrumentArray) }
//
//    return JsonObject().apply {
//        addProperty("guid", "someguid")
//        addProperty("method", "sub")
//        add("data", dataObject) }
//}
//
//private fun handleBinaryMessage(bytes: ByteBuffer, userData: UserData) {
//    try {
//        val feedResponse = MarketFeeder.FeedResponse.parseFrom(bytes.array())
//        Log.d("WebSocket", "$feedResponse")
//
//        val ltp0 = feedResponse.feeds["${userData.marketid}"]?.ff?.marketFF?.ltpc?.ltp
//        val close = feedResponse.feeds["${userData.marketid}"]?.ff?.marketFF?.ltpc?.cp
//
//        (context as? Activity)?.runOnUiThread {
//            if (ltp0 != null) {
//                retrivedata(requireContext(), ltp0, close!!) }
//        }
//    } catch (e: Exception) {
//        Log.e("WebSocket", "Error parsing binary message: ${e.message}") }
//}
//
//private fun handleWebSocketClientError(errorMessage: String) {
//    (context as? Activity)?.runOnUiThread {
//        // Handle the WebSocket client error on the UI thread
//        Log.e("WebSocket", errorMessage) }
//}
//
//private fun handleAuthError(errorMessage: String) {
//    (context as? Activity)?.runOnUiThread {
//        // Handle the authentication error on the UI thread
//        Log.e("WebSocket", errorMessage) }
//}



