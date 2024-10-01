package com.upstox.marketdatafeeder

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.upstox.ApiClient
import com.upstox.Configuration
import com.upstox.auth.OAuth
import com.upstox.marketdatafeeder.databinding.FragmentExitLastPositionBinding
import com.upstox.marketdatafeeder.pref.AccessTokenPref
import com.upstox.marketdatafeeder.pref.OhlcData
import com.upstox.marketdatafeeder.rpc.proto.MarketFeeder
import io.swagger.client.api.WebsocketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
import kotlin.math.pow


class ExitLast_Position_Fragment : BottomSheetDialogFragment() {

    lateinit var binding : FragmentExitLastPositionBinding
    private lateinit var client: WebSocketClient
    private lateinit var firstTab: ConstraintLayout
    private lateinit var secondTab: ConstraintLayout
    private lateinit var tvFirstTab: TextView
    private lateinit var tvSecondTab: TextView
    var type : String = ""

    var todaypnl: Double = 0.0


    companion object {
        const val ARG_WATCHLIST_DATA = "arg_watchlist_exit"

        fun newInstance(watchlistData: UserData): ExitLast_Position_Fragment {
            val fragment = ExitLast_Position_Fragment()
            val args = Bundle()
            args.putParcelable(ARG_WATCHLIST_DATA, watchlistData)
            fragment.arguments = args
            return fragment
        }

    }


    private var isFetchingEnabled = true // Flag to control fetching


    private val upstoxApiUrlOhlc = "https://api.upstox.com/v2/market-quote/ohlc"
    private var instrumentKeyOhlc: String = ""
    private var instrumentKeyOhlc2: String = ""
    lateinit var accessToken: String  // Replace with your actual access token

    private val handler = Handler(Looper.getMainLooper())



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExitLastPositionBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val watchlistData =
            arguments?.getParcelable<UserData>(ExitLast_Position_Fragment.ARG_WATCHLIST_DATA)


        binding.btnAddBasket.setOnClickListener { binding.btnAddBasket.visibility = View.GONE }
        binding.edit.setOnClickListener { binding.btnAddBasket.visibility = View.VISIBLE }


        // Update UI with watchlistData in the bottom sheet
        watchlistData?.let {

            binding.tvStockName.text = it.name
            binding.tvStockDescription.text = it.last
            binding.tvExchange.text = it.nse
            binding.tvLotSizeTitle.text = "1 Lot Size = ${it.qty}"
            binding.edtQty.setText(it.lot)

            val accessTokenManager = AccessTokenPref(requireContext())

            // Use the access token as needed in other activities
            accessToken = accessTokenManager.accessToken.toString()

            val instrumentKeys = listOf("${watchlistData.marketid}")
            instrumentKeyOhlc = watchlistData.marketid

            // Perform other non-WebSocket-related initialization here

            if (watchlistData.type == "1" || watchlistData.type == "2") { // Initialize WebSocketManager only when needed
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val authenticatedClient = authenticateApiClient(accessToken)
                        val serverUri = getAuthorizedWebSocketUri(authenticatedClient)

                        launch(Dispatchers.Main) {
                            try {
                                client =
                                    createWebSocketClient(serverUri, instrumentKeys, watchlistData)
                                client.connect()
                            } catch (e: Exception) {
                                handleWebSocketClientError("Error creating WebSocket client: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        handleAuthError("Error authenticating API client: ${e.message}")
                    }
                } } else { startFetchingOhlcData()}

        }


        firstTab = view.findViewById(R.id.first_tab)
        secondTab = view.findViewById(R.id.second_tab)
        tvFirstTab = view.findViewById(R.id.tv_first_tab)
        tvSecondTab = view.findViewById(R.id.tv_second_tab)

        // Set click listeners on both ConstraintLayouts
        firstTab.setOnClickListener { onTabClicked(firstTab, tvFirstTab)
            type = "C"
        }
        secondTab.setOnClickListener { onTabClicked(secondTab, tvSecondTab)
            type = "I"
        }


        val radioGroup = view.findViewById<RadioGroup>(R.id.rgPriceLimitMarket)
        val rbLimitPrice = view.findViewById<MaterialRadioButton>(R.id.rbLimitPrice)
        val rbMarketPrice = view.findViewById<MaterialRadioButton>(R.id.rbMarketPrice)

        // Set a listener on the RadioGroup
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Check which radio button is selected and change background color accordingly
            when (checkedId) {
                R.id.rbLimitPrice -> {
                    rbLimitPrice.setBackgroundResource(R.drawable.radio_button_orderpad_selected_exit) // Replace with your unselected background
                }

                R.id.rbMarketPrice -> {
                    val resolvedColor =
                        ContextCompat.getColor(requireContext(), R.color.textlight)
                    binding.edtTriggerPrice.setTextColor(resolvedColor)
                    rbMarketPrice.setBackgroundResource(R.drawable.radio_button_orderpad_selected_exit) // Replace with your unselected background
                }
            }
        }

    }




    private fun startFetchingOhlcData() {

        val watchlistData = arguments?.getParcelable<UserData>(ExitLast_Position_Fragment.ARG_WATCHLIST_DATA)
        handler.postDelayed({

            if (isFetchingEnabled) {
                watchlistData?.let { fetchOhlcData(it) }
                startFetchingOhlcData() // Schedule the next execution after 4 seconds
            }

        }, 1500)
    }


    private fun stopFetchingOhlcData() { isFetchingEnabled = false }


    private fun onTabClicked(clickedTab: ConstraintLayout, clickedTextView: TextView) {
        // Reset properties for both tabs
        resetTabProperties(firstTab, tvFirstTab)
        resetTabProperties(secondTab, tvSecondTab)

        // Update properties for the clicked tab
        clickedTab.setBackgroundResource(R.drawable.order_pad_selected_buy_background) // Replace with your background resource
        clickedTextView.setTextColor(resources.getColor(R.color.buy_green)) // Replace with your text color resource
    }

    private fun resetTabProperties(tab: ConstraintLayout, textView: TextView) {
        tab.setBackgroundResource(R.drawable.bg_white_4dp) // Replace with your default background resource
        textView.setTextColor(resources.getColor(R.color.title_black)) // Replace with your default text color resource
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
        wachlistData: UserData
    ): WebSocketClient {
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


    private fun handleBinaryMessage(bytes: ByteBuffer, userData: UserData) {
        try {
            val feedResponse = MarketFeeder.FeedResponse.parseFrom(bytes.array())

            Log.d("WebSocket", "$feedResponse")


            if (userData.type == "1") {

                val ltp0 = feedResponse.feeds["${userData.marketid}"]?.ff?.indexFF?.ltpc?.ltp

                val close = feedResponse.feeds["${userData.marketid}"]?.ff?.indexFF?.ltpc?.cp

                (context as? Activity)?.runOnUiThread {

                    if (ltp0!! >= close!!) {
                        val resolvedColor =
                            ContextCompat.getColor(requireContext(), R.color.buy_green)
                        binding.tvStockPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? =
                            requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
                        binding.tvStockPrice.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            yourDrawableIcon,
                            null
                        )
                        binding.tvStockPrice.compoundDrawablePadding = 8
                    } else {

                        val resolvedColor =
                            ContextCompat.getColor(requireContext(), R.color.sell_red)
                        binding.tvStockPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? =
                            requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                        binding.tvStockPrice.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            yourDrawableIcon,
                            null
                        )
                        binding.tvStockPrice.compoundDrawablePadding = 8

                    }

                    binding.tvStockPrice.text = "₹ " + formatIntWithDecimal(ltp0)
                    binding.edtTriggerPrice.setText(formatIntWithDecimal(ltp0))
                    val newPrice = ltp0 - close!!
                    val perc = newPrice / 100
                    binding.tvStockPricePerChange.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"

                    try {
                        val qtyEditText = binding.edtQty.text.toString()
                        val lot = if (qtyEditText.isNotEmpty()) qtyEditText.toInt() * userData.lot.toInt() else 0
                        val qty = ltp0 * lot

                        binding.tvTotalOrderValueAmount.text = "₹$qty +"
                    } catch (e: NumberFormatException) {
                        // Handle the case where the text in edtQty is not a valid integer
                        // For example, you may want to display an error message or provide a default value.
                        binding.tvTotalOrderValueAmount.text = "Invalid input"
                    }

                    // Continue handling as needed...
                    binding.confirm.setOnClickListener {
                        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                        val userDataa = UserData("50", "", userData.name, "buy", ltp0.toString(), "","",userData.name,"","",userData.lot, userData.marketid,type,userData.marketid2 ,userData.type,userData.nse)
                        sharedPreferencesManager.saveData(userDataa)
                        showProgressBarForOneSecond() }
                }

            } else {

                val ltp0 = feedResponse.feeds["${userData.marketid}"]?.ff?.marketFF?.ltpc?.ltp

                val close = feedResponse.feeds["${userData.marketid}"]?.ff?.marketFF?.ltpc?.cp

                (context as? Activity)?.runOnUiThread {

                    if (ltp0!! >= close!!) {
                        val resolvedColor =
                            ContextCompat.getColor(requireContext(), R.color.buy_green)
                        binding.tvStockPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? =
                            requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
                        binding.tvStockPrice.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            yourDrawableIcon,
                            null
                        )
                        binding.tvStockPrice.compoundDrawablePadding = 8
                    } else {

                        val resolvedColor =
                            ContextCompat.getColor(requireActivity(), R.color.sell_red)
                        binding.tvStockPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? =
                            requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                        binding.tvStockPrice.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            yourDrawableIcon,
                            null
                        )
                        binding.tvStockPrice.compoundDrawablePadding = 8

                    }


                    binding.tvStockPrice.text = "₹ " + formatIntWithDecimal(ltp0)
                    binding.edtTriggerPrice.setText(formatIntWithDecimal(ltp0))
                    val newPrice = ltp0 - close!!
                    val perc = newPrice / 100
                    binding.tvStockPricePerChange.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"



                    try {
                        val qtyEditText = binding.edtQty.text.toString()
                        val lot = if (qtyEditText.isNotEmpty()) qtyEditText.toInt() * userData.lot.toInt() else 0
                        val qty = ltp0 * lot

                        binding.tvTotalOrderValueAmount.text = "₹$qty +"
                    } catch (e: NumberFormatException) {
                        // Handle the case where the text in edtQty is not a valid integer
                        // For example, you may want to display an error message or provide a default value.
                        binding.tvTotalOrderValueAmount.text = "Invalid input"
                    }
                    // Continue handling as needed...


                    binding.confirm.setOnClickListener {
                        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                        val userDataa = UserData(binding.edtQty.text.toString(), binding.edtTriggerPrice.text.toString(), userData.name, "buy",ltp0.toString(), "","y",userData.last,"","",userData.lot, userData.marketid,type, userData.marketid2,userData.type,userData.nse)



                        sharedPreferencesManager.saveData(userDataa)
                        showProgressBarForOneSecond()
                        stopFetchingOhlcData()
                        true
                    }


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
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(value.toDouble())
    }


    private fun showProgressBarForOneSecond() {
//        binding.progressCircular.visibility = View.VISIBLE
//        binding.progressCircular.progressTintList = ColorStateList.valueOf(Color.WHITE) // Set the progress bar color to white

//        // Delay for 1.5 seconds and then hide the progress bar
//        Handler().postDelayed({
//            binding.progressCircular.visibility = View.GONE
//            dismiss()
//        }, 1500)


        showToast(requireContext())

        dismiss()
    }

    fun showToast(context: Context) {
        // Inflate custom layout
        val layoutInflater = LayoutInflater.from(context)
        val layout = layoutInflater.inflate(R.layout.custom_toast_layout, null)

        // Adjust the width of the layout
        val screenWidth = context.resources.displayMetrics.widthPixels
        val toastWidth = (screenWidth * 0.8).toInt() // Adjust as needed
        val layoutParams = ViewGroup.LayoutParams(toastWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.layoutParams = layoutParams

        // Create and show the toast
        with (Toast(context)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

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

        // Return OHLC data if all values are not null
        return if (open != null && high != null && low != null && close != null && lastPrice != null) {
            OhlcData(open, high, low, close, lastPrice)
        } else {
            null
        }
    }
    private fun fetchOhlcData(userData: UserData) {
        val context = context ?: return  // Ensuring that a valid context is available

        GlobalScope.launch(Dispatchers.IO) {
            val ohlcResult = getOhlcData(userData)

            withContext(Dispatchers.Main) {
                if (ohlcResult != null) {
                    val lastPrice = ohlcResult.lastPrice ?: return@withContext
                    val closePrice = ohlcResult.close ?: return@withContext

                    val priceDiff = lastPrice - closePrice
                    val percentageChange = (priceDiff / closePrice) * 100

                    val resolvedColor = if (lastPrice >= closePrice) {
                        ContextCompat.getColor(context, R.color.buy_green)
                    } else {
                        ContextCompat.getColor(context, R.color.sell_red)
                    }

                    binding.tvStockPrice.setTextColor(resolvedColor)

                    val drawableIcon = if (lastPrice >= closePrice) {
                        ContextCompat.getDrawable(context, R.drawable.ic_arrow_gain)
                    } else {
                        ContextCompat.getDrawable(context, R.drawable.ic_arrow_loss)
                    }

                    binding.tvStockPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableIcon, null)
                    binding.tvStockPrice.compoundDrawablePadding = 8

                    binding.tvStockPricePerChange.text = if (lastPrice >= closePrice) {
                        "+${formatIntWithDecimal(priceDiff)} (+${formatIntWithDecimal(percentageChange)}%)"
                    } else {
                        "${formatIntWithDecimal(priceDiff)} (${formatIntWithDecimal(percentageChange)}%)"
                    }

                    binding.tvStockPrice.text = "₹${formatIntWithDecimal(lastPrice)}"
                    binding.edtTriggerPrice.setText(formatIntWithDecimal(lastPrice))

                    binding.confirm.setOnClickListener {
                        val sharedPreferencesManager = SharedPreferencesManager(context)
                        val userDataa = UserData(userData.qty, lastPrice.toString(), userData.name, "buy", userData.avg, "", "y", userData.last, "", "", userData.lot, userData.marketid, type, userData.marketid2, userData.type, userData.nse)
                        sharedPreferencesManager.saveData(userDataa)
                        showProgressBarForOneSecond()
                    }

                    retrivedata(context, lastPrice)
                    Log.d("backgroundrun", "run bottom sheet buy 2")
                } else {
                    Log.e("OHLC Data", "Error: Unable to retrieve OHLC data.")
                }
            }
        }
    }


    fun retrivedata(context: Context, ltp: Double) {



        val sharedPreferencesManager = SharedPreferencesManager(context) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()


        if (userData != null) {

//            binding.constraintLayout3.visibility = View.VISIBLE

            if (userData.type == "buy") {



                val buy = ContextCompat.getColor(context, R.color.buy_green)
//                binding.tvTagBuySell.text = "BUY"
//                binding.tvTagBuySell.setTextColor(buy)
//                binding.tvTagBuySell.setBackgroundResource(R.drawable.bg_buy_green_12_rectangle)
//
//                binding.layout1.visibility = View.VISIBLE
//
//                binding.textView.text = userData.name
//                binding.textView24.text = userData.last
//                binding.textView26.text = userData.avg
//                binding.textView22.text = userData.lot
//                binding.tvLotSize.text = "Lot (1 Lot = ${userData.qty})"
//                binding.textViewsda.text = userData.name
//                binding.textView24sda.text = userData.last


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

                val ltppercent = devide * 100.0




                // Update the UI with the new value
                if (currentValue < userData.avg.toDouble()) {

                    val textColor = ContextCompat.getColor(context, R.color.red2)
                    binding.totalgainimg.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP)
//                    binding.textView27.setTextColor(textColor)
//                    binding.textView29.setTextColor(textColor)

                    val ltp = formatIntWithDecimal(currentValue)
                    val fluct = formatIntWithDecimal(f)
                    val ltpper = formatIntWithDecimal(ltppercent)

//                    binding.textView29.text = ltp
//                    binding.textView27.text = "${fluct}"
//                    binding.ltpprecent.text = "(${ltpper}%)"

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
                    binding.totalgainimg.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP)
//                    binding.textView27.setTextColor(textColor)
//                    binding.textView29.setTextColor(textColor)

                    val ltp = formatIntWithDecimal(currentValue)
                    val fluct = formatIntWithDecimal(f)
                    val ltpper = formatIntWithDecimal(ltppercent)

//                    binding.textView29.text = ltp
//                    binding.textView27.text = "+${fluct}"
//                    binding.ltpprecent.text = "(+${ltpper}%)"

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



}