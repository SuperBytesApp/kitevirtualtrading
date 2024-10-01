package com.upstox.marketdatafeeder.ui.frag

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.upstox.ApiClient
import com.upstox.Configuration
import com.upstox.auth.OAuth
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.WatchlistAdapter
import com.upstox.marketdatafeeder.WebSocketCallback
import com.upstox.marketdatafeeder.WebSocketManager
import com.upstox.marketdatafeeder.databinding.FragmentDashboardBinding
import com.upstox.marketdatafeeder.databinding.FragmentWatchlistBinding
import com.upstox.marketdatafeeder.pref.AccessTokenPref
import com.upstox.marketdatafeeder.pref.WachlistData
import com.upstox.marketdatafeeder.pref.WatchlistSharedPreferencesManager
import com.upstox.marketdatafeeder.rpc.proto.MarketFeeder
import io.swagger.client.api.WebsocketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.ref.WeakReference
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class WatchlistFragment : Fragment() , WebSocketCallback {


    lateinit var binding: FragmentWatchlistBinding
    private lateinit var userDataAdapter: WatchlistAdapter
    private val userDataList = mutableListOf<WachlistData>()
    private val webSocketManager by lazy { WebSocketManager(this, WeakReference(requireActivity())) }
    private lateinit var client: WebSocketClient
    private lateinit var sharedPreferences: SharedPreferences

    private val sharedPreferencesFileName = "MySharedPreferences"
    private val marketExp1Key = "marketexpy1"
    private val marketExp2Key = "marketexp2"
     private val exp= "expycolor1"
    private val exp2 =  "expycolor2"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPreferencesManager = WatchlistSharedPreferencesManager(requireContext())

        val watchlistItems = sharedPreferencesManager.getWatchlist()

        sharedPreferences = requireActivity().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE)


        if (watchlistItems.isNotEmpty()) {
            // Do something with the watchlistItems
            // For example, update your RecyclerView adapter
            userDataList.addAll(watchlistItems)
            binding.constraintLayout.visibility = View.GONE
        } else {
            // Handle the case where there are no watchlist items
            // For example, show a message or update the UI accordingly
            binding.constraintLayout.visibility = View.VISIBLE
        }


        // Set up RecyclerView

        userDataAdapter = WatchlistAdapter(userDataList,requireContext())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        binding.recyclerView.adapter = userDataAdapter

        var sharedPreferencesManager2 = AccessTokenPref(requireContext())

        // Use the access token as needed in other activities
        val accessToken = sharedPreferencesManager2.accessToken.toString()
        val instrumentKeys2 = listOf("NSE_INDEX|Nifty Bank")
        val instrumentKeys = listOf("NSE_INDEX|Nifty 50")

        // Perform other non-WebSocket-related initialization here

        // Initialize WebSocketManager only when needed
        webSocketManager.launchWebSocket(accessToken, instrumentKeys2)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val authenticatedClient = authenticateApiClient(accessToken)
                val serverUri = getAuthorizedWebSocketUri(authenticatedClient)

                launch(Dispatchers.Main) {
                    try {
                        client = createWebSocketClient(serverUri, instrumentKeys)
                        client.connect()
                    } catch (e: Exception) {
                        handleWebSocketClientError("Error creating WebSocket client: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                handleAuthError("Error authenticating API client: ${e.message}")
            }
        }


        // Load data from SharedPreferences and display
        loadDataFromSharedPreferences()

        binding.textView11.setOnClickListener {
            showInputDialog()
        }

        return root
    }




    @SuppressLint("MissingInflatedId")
    private fun showInputDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_input, null)
        dialogBuilder.setView(dialogView)

        val editTextMarketExp1 = dialogView.findViewById(R.id.editTextMarketExp1) as EditText
        val editTextMarketExp2 = dialogView.findViewById(R.id.editTextMarketExp2) as EditText
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroups)
        val radioGroup2 = dialogView.findViewById<RadioGroup>(R.id.radioGroups2)

        val marketExp1Value = sharedPreferences.getString(marketExp1Key, "")
        val marketExp2Value = sharedPreferences.getString(marketExp2Key, "")
        editTextMarketExp1.setText(marketExp1Value)
        editTextMarketExp2.setText(marketExp2Value)

        var radio = ""
        var radio2 = ""
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected radio button by its ID
            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)

            // Check which radio button was clicked and perform an action
            when (selectedRadioButton.id) {
                R.id.radioButtonsgreen -> {
                    // Radio Button 1 is clicked
                    // Perform your action here
                    radio = "g"

                }
                R.id.radioButtonsred -> {
                    // Radio Button 2 is clicked
                    // Perform your action here
                    radio = "r"

                }
            }
        }


        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected radio button by its ID
            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)

            // Check which radio button was clicked and perform an action
            when (selectedRadioButton.id) {
                R.id.radioButtons2green -> {
                    // Radio Button 1 is clicked
                    // Perform your action here
                    radio2 = "g"

                }
                R.id.radioButtons2red -> {
                    // Radio Button 2 is clicked
                    // Perform your action here
                    radio2 = "r"

                }
            }
        }



        dialogBuilder.setTitle("Enter Market Exp Values")
        dialogBuilder.setPositiveButton("Save") { _, _ ->
            // Save input values to SharedPreferences
            val marketExp1Value = editTextMarketExp1.text.toString()
            val marketExp2Value = editTextMarketExp2.text.toString()
            saveDataToSharedPreferences(marketExp1Value, marketExp2Value,radio,radio2)
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun saveDataToSharedPreferences(marketExp1: String, marketExp2: String,radio:String,radio2: String) {
        val editor = sharedPreferences.edit()
        editor.putString(marketExp1Key, marketExp1)
        editor.putString(marketExp2Key, marketExp2)
        editor.putString(exp, radio)
        editor.putString(exp2, radio2)
        editor.apply()
        // Update display with new values
        loadDataFromSharedPreferences()
    }

    private fun loadDataFromSharedPreferences() {
        val marketExp1Value = sharedPreferences.getString(marketExp1Key, "")
        val marketExp2Value = sharedPreferences.getString(marketExp2Key, "")
        val exp1 = sharedPreferences.getString(exp, "")
        val exps2 = sharedPreferences.getString(exp2, "")
        binding.expy1.text = marketExp1Value
        binding.expy2.text = marketExp2Value

        if (exp1 != null && exp1 == "r"){
            val textColor = ContextCompat.getColor(requireContext(), R.color.red2)
            binding.expy1.setTextColor(textColor)
            binding.bgexp.setBackgroundResource(R.drawable.nifty_background3)

        }
        if (exps2 != null && exps2 == "r"){
            val textColor = ContextCompat.getColor(requireContext(), R.color.red2)
            binding.expy2.setTextColor(textColor)
            binding.bgexp2.setBackgroundResource(R.drawable.nifty_background3)

        }

        requireActivity().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE)

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
        instrumentKeys: List<String>): WebSocketClient {
        return object : WebSocketClient(serverUri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                Log.d("WebSocket", "Opened connection")
                sendSubscriptionRequest(this, instrumentKeys)
            }

            override fun onMessage(message: String) {
                Log.d("WebSocket", "Received: $message")
            }

            override fun onMessage(bytes: ByteBuffer) {
                handleBinaryMessage(bytes)
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


    private fun handleBinaryMessage(bytes: ByteBuffer) {
        try {
            val feedResponse = MarketFeeder.FeedResponse.parseFrom(bytes.array())

            Log.d("WebSocket","$feedResponse")

            val ltp0 = feedResponse.feeds["NSE_INDEX|Nifty 50"]?.ff?.indexFF?.ltpc?.ltp

            val close = feedResponse.feeds["NSE_INDEX|Nifty 50"]?.ff?.indexFF?.ltpc?.cp

            // UI-related code
            (context as? Activity)?.runOnUiThread {

                if (ltp0!! >= close!!){
                    val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
                    binding.tvFirstPosPriceChange.setTextColor(resolvedColor)
                    // Change drawable icon programmatically
                    val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
                    binding.tvFirstPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                    binding.tvFirstPosPriceChange.compoundDrawablePadding = 8
                    val newPrice = ltp0 - close
                    val perc = newPrice / close * 100
                    binding.tvFirstPosPercentageChange.text =  "+${formatIntWithDecimal(newPrice)} (+${formatIntWithDecimal(perc)}%)"


                }else{

                    val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
                    binding.tvFirstPosPriceChange.setTextColor(resolvedColor)
                    // Change drawable icon programmatically
                    val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                    binding.tvFirstPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                    binding.tvFirstPosPriceChange.compoundDrawablePadding = 8

                    val newPrice = ltp0 - close
                    val perc = newPrice / close * 100
                    binding.tvFirstPosPercentageChange.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"


                }


                binding.tvFirstPosPriceChange.text = formatIntWithDecimal(ltp0!!)

                // Continue handling as needed...
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

    override fun onLtpReceived(ltp: Double, cp: Double) {

        if (ltp >= cp){
            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
            binding.tvSecondPosPriceChange.setTextColor(resolvedColor)
            // Change drawable icon programmatically
            val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
            binding.tvSecondPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
            binding.tvSecondPosPriceChange.compoundDrawablePadding = 8

            val newPrice = ltp - cp
            val perc = newPrice / cp * 100
            binding.tvSecondPosPercentageChange.text =  "+${formatIntWithDecimal(newPrice)} (+${formatIntWithDecimal(perc)}%)"



        }else{

            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
            binding.tvSecondPosPriceChange.setTextColor(resolvedColor)
            // Change drawable icon programmatically
            val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
            binding.tvSecondPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
            binding.tvSecondPosPriceChange.compoundDrawablePadding = 8
            val newPrice = ltp - cp
            val perc = newPrice / cp * 100
            binding.tvSecondPosPercentageChange.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"



        }

        binding.tvSecondPosPriceChange.text = formatIntWithDecimal(ltp)


    }




    override fun onPause() {
        super.onPause()
        // Call stopFetchingOhlcData when the fragment pauses
        userDataAdapter.stopFetchingOhlcData()
    }


    override fun onDestroy() {
        super.onDestroy()
        // Call stopFetchingOhlcData when the fragment is destroyed
        userDataAdapter.stopFetchingOhlcData()
    }





}