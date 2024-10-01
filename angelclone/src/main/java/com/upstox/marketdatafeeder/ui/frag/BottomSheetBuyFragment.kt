package com.upstox.marketdatafeeder.ui.frag

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.upstox.ApiClient
import com.upstox.Configuration
import com.upstox.auth.OAuth
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.SharedPreferencesManager
import com.upstox.marketdatafeeder.UserData
import com.upstox.marketdatafeeder.WatchlistAdapter
import com.upstox.marketdatafeeder.databinding.FragmentBottomSheetBinding
import com.upstox.marketdatafeeder.databinding.FragmentBottomSheetBuyBinding
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


class BottomSheetBuyFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBuyBinding
    private lateinit var client: WebSocketClient
    private lateinit var firstTab: ConstraintLayout
    private lateinit var secondTab: ConstraintLayout
    private lateinit var tvFirstTab: TextView
    private lateinit var tvSecondTab: TextView
    var type : String = ""

    companion object {
        const val ARG_WATCHLIST_DATA = "arg_watchlist_buy"

        fun newInstance(watchlistData: WachlistData): BottomSheetBuyFragment {
            val fragment = BottomSheetBuyFragment()
            val args = Bundle()
            args.putParcelable(ARG_WATCHLIST_DATA, watchlistData)
            fragment.arguments = args
            return fragment
        }

    }


    private var isFetchingEnabled = true // Flag to control fetching

    private lateinit var sharedPreferences: SharedPreferences

    private val upstoxApiUrlOhlc = "https://api.upstox.com/v2/market-quote/ohlc"
    private var instrumentKeyOhlc: String = ""
    private var instrumentKeyOhlc2: String = ""
    lateinit var accessToken: String  // Replace with your actual access token

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0 // Optional: If you don't want a peek height
        }
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBuyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val watchlistData = arguments?.getParcelable<WachlistData>(BottomSheetBuyFragment.ARG_WATCHLIST_DATA)
         // Get the screen height in dp
        val screenHeightInDp = getScreenHeightInDp()

        // Find the view whose height you want to adjust
        val yourView = view.findViewById<View>(R.id.height) // Replace with the ID of your view in your_bottom_sheet_layout

        // Set the layout parameters to match the screen height
        val layoutParams = yourView.layoutParams
        layoutParams.height = (screenHeightInDp * resources.displayMetrics.density).toInt()
        yourView.layoutParams = layoutParams
        setStatusBarColor(R.color.white)

        binding.btnAddBasket.setOnClickListener { binding.btnAddBasket.visibility = View.GONE }
        binding.edit.setOnClickListener { binding.btnAddBasket.visibility = View.VISIBLE }

        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentBalance = sharedPreferences.getFloat("balance", 0.0f)
        updateBalanceTextView(currentBalance)


        // Update UI with watchlistData in the bottom sheet
        watchlistData?.let {

            binding.tvStockName.text = it.firstName
            binding.tvStockDescription.text = it.lastName
            binding.tvExchange.text = it.nse
            binding.tvLotSizeTitle.text = "1 Lot Size = ${it.lot}"


            val accessTokenManager = AccessTokenPref(requireContext())

            // Use the access token as needed in other activities
            accessToken = accessTokenManager.accessToken.toString()

            val instrumentKeys = listOf("${watchlistData.marketId}")
            instrumentKeyOhlc = watchlistData.marketId

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
                    rbLimitPrice.setBackgroundResource(R.drawable.radio_button_orderpad_selected_buy) // Replace with your unselected background
                }

                R.id.rbMarketPrice -> {
                    val resolvedColor =
                        ContextCompat.getColor(requireContext(), R.color.textlight)
                    binding.edtTriggerPrice.setTextColor(resolvedColor)
                    rbMarketPrice.setBackgroundResource(R.drawable.radio_button_orderpad_selected_buy) // Replace with your unselected background
                }
            }
        }

        // Find all TextViews representing numbers
        val textView1 = view.findViewById<TextView>(R.id.textView32)
        val textView2 = view.findViewById<TextView>(R.id.textView6)
        val textView3 = view.findViewById<TextView>(R.id.three)
        val textView4 = view.findViewById<TextView>(R.id.textView33)
        val textView5 = view.findViewById<TextView>(R.id.textView35)
        val textView6 = view.findViewById<TextView>(R.id.textView41)
        val textView7 = view.findViewById<TextView>(R.id.textView36)
        val textView8 = view.findViewById<TextView>(R.id.textView37)
        val textView9 = view.findViewById<TextView>(R.id.textView38)
        val textView0 = view.findViewById<TextView>(R.id.textView40)

        // Set click listeners for each TextView
        textView1.setOnClickListener { appendText("1") }
        textView2.setOnClickListener { appendText("2") }
        textView3.setOnClickListener { appendText("3") }
        textView4.setOnClickListener { appendText("4") }
        textView5.setOnClickListener { appendText("5") }
        textView6.setOnClickListener { appendText("6") }
        textView7.setOnClickListener { appendText("7") }
        textView8.setOnClickListener { appendText("8") }
        textView9.setOnClickListener { appendText("9") }
        textView0.setOnClickListener { appendText("0") }

        // Find the ImageView for cutting numbers
        val cutImageView = view.findViewById<ImageView>(R.id.imageView5)

        // Set click listener for cutting the last character
        cutImageView.setOnClickListener {
            val currentText = binding.edtQty.text.toString()
            if (currentText.isNotEmpty()) {
               binding.edtQty.setText(currentText.substring(0, currentText.length - 1))
            }
        }




    }

    // Function to append text to the EditText
    private fun appendText(text: String) {
        val currentText = binding.edtQty.text.toString()
        binding.edtQty.setText(String.format("%s%s", currentText, text))
    }

    fun getScreenHeightInDp(): Float {
        val density = Resources.getSystem().displayMetrics.density
        val screenHeightPx = Resources.getSystem().displayMetrics.heightPixels
        return screenHeightPx / density
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor(colorResId: Int) {
        val window = dialog?.window
        WindowCompat.setDecorFitsSystemWindows(window!!, false)
        window?.statusBarColor = requireActivity().getColor(colorResId)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun resetStatusBarColor() {
        val window = dialog?.window
        WindowCompat.setDecorFitsSystemWindows(window!!, true)
        window?.statusBarColor = requireActivity().getColor(android.R.color.transparent) // Reset status bar color
    }

    private fun startFetchingOhlcData() {

        val watchlistData = arguments?.getParcelable<WachlistData>(BottomSheetBuyFragment.ARG_WATCHLIST_DATA)
        handler.postDelayed({

            if (isFetchingEnabled) {
                watchlistData?.let { fetchOhlcData(it) }
                startFetchingOhlcData() // Schedule the next execution after 4 seconds
            }

        }, 3000)
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
        wachlistData: WachlistData
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
                Log.d(
                    "WebSocket",
                    "Connection closed by ${if (remote) "remote peer" else "us"}. Info: $reason"
                )
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

            Log.d("WebSocket", "$feedResponse")


            if (userData.type == "1") {

                val ltp0 = feedResponse.feeds["${userData.marketId}"]?.ff?.indexFF?.ltpc?.ltp

                val close = feedResponse.feeds["${userData.marketId}"]?.ff?.indexFF?.ltpc?.cp

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
                        val userDataa = UserData("50", "", userData.firstName, "buy", ltp0.toString(), "","",userData.lastName,"","",userData.lot, userData.marketId,type,userData.marketId2 ,userData.type,"")
                        sharedPreferencesManager.saveData(userDataa)
                        showProgressBarForOneSecond() }
                }

            } else {

                val ltp0 = feedResponse.feeds["${userData.marketId}"]?.ff?.marketFF?.ltpc?.ltp

                val close = feedResponse.feeds["${userData.marketId}"]?.ff?.marketFF?.ltpc?.cp

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
                        val userDataa = UserData(binding.edtQty.text.toString(), binding.edtTriggerPrice.text.toString(), userData.firstName, "buy",ltp0.toString(), "","y",userData.lastName,"","",userData.lot, userData.marketId,type, userData.marketId2,userData.type,binding.tvExchange.text.toString())



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
        return formatter.format(value)
    }

    private fun showProgressBarForOneSecond() {
        binding.progressCircular.visibility = View.VISIBLE

        // Delay for 1 second (1000 milliseconds) and then hide the progress bar
        Handler().postDelayed({
            binding.progressCircular.visibility = View.GONE
            showToast(requireContext())
            dismiss()
        }, 1500)
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
                        binding.tvStockPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
                        binding.tvStockPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        binding.tvStockPrice.compoundDrawablePadding = 8
                        val newPrice = ohlcResult.lastPrice - ohlcResult.close
                        val perc = newPrice / ohlcResult.close * 100
                        binding.tvStockPricePerChange.text =  "+${formatIntWithDecimal(newPrice)} (+${formatIntWithDecimal(perc)}%)"

                    }else{

                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
                        binding.tvStockPrice.setTextColor(resolvedColor)
                        // Change drawable icon programmatically
                        val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                        binding.tvStockPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                        binding.tvStockPrice.compoundDrawablePadding = 8
                        val newPrice = ohlcResult.lastPrice - ohlcResult.close
                        val perc = newPrice / ohlcResult.close * 100
                        binding.tvStockPricePerChange.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"

                    }

                    binding.tvStockPrice.text =  "₹"+formatIntWithDecimal(ohlcResult.lastPrice)
                    binding.edtTriggerPrice.setText(formatIntWithDecimal(ohlcResult.lastPrice))


                    binding.edtQty.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            // This method is called to notify you that, somewhere within s, the text has been changed.
                            // Implement your calculation logic here
                            val input = s.toString()
                            if (input.isNotEmpty()) {
                                val number = input.toDouble()
                                // Perform your calculation here, for example, multiplying by 2
                                val result = number * userData.lot.toDouble()
                                // Update the TextView with the result
                                var total = result * ohlcResult.lastPrice
                                binding.tvTotalOrderValueAmount.text = "₹"+formatIntWithDecimal(total)+" + "

                                binding.tvChargesText.visibility = View.VISIBLE
                            } else {
                                // Clear the TextView if the input is empty
                                binding.tvTotalOrderValueAmount.text = "₹0.00"
                            }
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            // This method is called to notify you that, within s, the count characters beginning at start are about to be replaced by new text with length after.
                            // You don't need to implement anything here for your use case
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            // This method is called to notify you that, within s, the count characters beginning at start have just replaced old text that had length before.
                            // You don't need to implement anything here for your use case

                        }
                    })



                    // Continue handling as needed...
                    binding.confirm.setOnClickListener {
                        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                        val userDataa = UserData(userData.lot, "", userData.firstName, "buy", ohlcResult.lastPrice.toString(), "","",userData.lastName,"","",binding.edtQty.text.toString(), userData.marketId,type,userData.marketId2 ,userData.type,binding.tvExchange.text.toString())
                        sharedPreferencesManager.saveData(userDataa)
                        showProgressBarForOneSecond()
                    }



                    if (binding.edtQty.text.toString() != ""){
                        val number = binding.edtQty.text.toString().toDouble()
                        // Perform your calculation here, for example, multiplying by 2
                        val result = number * userData.lot.toDouble()
                        // Update the TextView with the result
                        var total = result * ohlcResult.lastPrice
                        binding.tvTotalOrderValueAmount.text = "₹"+formatIntWithDecimal(total)+" + "
                    }


                    Log.d("backgroundrun", "run bottom sheet buy 2")


                } else {
                    Log.e("OHLC Data", "Error: Unable to retrieve OHLC data.")
                }
            }
        }
    }

    private fun updateBalanceTextView(balance: Float) {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        var formattedBalance = formatter.format(balance.toDouble())
        binding.tvAvailableMarginValue.text = "₹"+formattedBalance

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


}