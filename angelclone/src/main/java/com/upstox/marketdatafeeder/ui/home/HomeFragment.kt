package com.upstox.marketdatafeeder.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.upstox.ApiClient
import com.upstox.Configuration
import com.upstox.auth.OAuth
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.SharedPreferencesManager
import com.upstox.marketdatafeeder.UserData
import com.upstox.marketdatafeeder.WebSocketCallback
import com.upstox.marketdatafeeder.WebSocketManager
import com.upstox.marketdatafeeder.databinding.FragmentHomeBinding
import com.upstox.marketdatafeeder.pref.AccessTokenPref
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

class HomeFragment : Fragment(){

    lateinit var binding: FragmentHomeBinding
//    private val webSocketManager by lazy { WebSocketManager(this, WeakReference(requireActivity())) }
//    private lateinit var client: WebSocketClient


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//
//        var sharedPreferencesManager2 = AccessTokenPref(requireContext())
//
//        // Use the access token as needed in other activities
//        val accessToken = sharedPreferencesManager2.accessToken.toString()
//        val instrumentKeys2 = listOf("NSE_INDEX|Nifty Bank")
//        val instrumentKeys = listOf("NSE_INDEX|Nifty 50")
//
//        // Perform other non-WebSocket-related initialization here
//
//        // Initialize WebSocketManager only when needed
//        webSocketManager.launchWebSocket(accessToken, instrumentKeys2)
//
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val authenticatedClient = authenticateApiClient(accessToken)
//                val serverUri = getAuthorizedWebSocketUri(authenticatedClient)
//
//                launch(Dispatchers.Main) {
//                    try {
//                        client = createWebSocketClient(serverUri, instrumentKeys)
//                        client.connect()
//                    } catch (e: Exception) {
//                        handleWebSocketClientError("Error creating WebSocket client: ${e.message}")
//                    }
//                }
//            } catch (e: Exception) {
//                handleAuthError("Error authenticating API client: ${e.message}")
//            }
//        }
//
//
        return root
    }



//
//    private fun authenticateApiClient(accessToken: String): ApiClient {
//        val defaultClient = Configuration.getDefaultApiClient()
//        val oAuth = defaultClient.getAuthentication("OAUTH2") as OAuth
//        oAuth.accessToken = accessToken
//        return defaultClient
//    }
//
//    private fun getAuthorizedWebSocketUri(authenticatedClient: ApiClient): URI {
//        val websocketApi = WebsocketApi(authenticatedClient)
//        val response = websocketApi.getMarketDataFeedAuthorize("2.0")
//        return URI.create(response.data.authorizedRedirectUri)
//    }
//
//    private fun createWebSocketClient(
//        serverUri: URI,
//        instrumentKeys: List<String>): WebSocketClient {
//        return object : WebSocketClient(serverUri) {
//            override fun onOpen(handshakedata: ServerHandshake) {
//                Log.d("WebSocket", "Opened connection")
//                sendSubscriptionRequest(this, instrumentKeys)
//            }
//
//            override fun onMessage(message: String) {
//                Log.d("WebSocket", "Received: $message")
//            }
//
//            override fun onMessage(bytes: ByteBuffer) {
//                handleBinaryMessage(bytes)
//            }
//
//            override fun onClose(code: Int, reason: String, remote: Boolean) {
//                Log.d("WebSocket", "Connection closed by ${if (remote) "remote peer" else "us"}. Info: $reason")
//            }
//
//            override fun onError(ex: Exception) {
//                ex.printStackTrace()
//            }
//        }
//    }
//
//    private fun sendSubscriptionRequest(client: WebSocketClient, instrumentKeys: List<String>) {
//        val requestObject = constructSubscriptionRequest(instrumentKeys)
//        val binaryData = requestObject.toString().toByteArray(StandardCharsets.UTF_8)
//        client.send(binaryData)
//    }
//
//    private fun constructSubscriptionRequest(instrumentKeys: List<String>): JsonObject {
//        val dataObject = JsonObject().apply {
//            addProperty("mode", "full")
//
//            val instrumentArray = instrumentKeys.fold(JsonArray()) { acc, item ->
//                acc.add(item)
//                acc
//            }
//            add("instrumentKeys", instrumentArray)
//        }
//
//        return JsonObject().apply {
//            addProperty("guid", "someguid")
//            addProperty("method", "sub")
//            add("data", dataObject)
//        }
//    }
//
//
//    private fun handleBinaryMessage(bytes: ByteBuffer) {
//        try {
//            val feedResponse = MarketFeeder.FeedResponse.parseFrom(bytes.array())
//
//            Log.d("WebSocket","$feedResponse")
//
//            val ltp0 = feedResponse.feeds["NSE_INDEX|Nifty 50"]?.ff?.indexFF?.ltpc?.ltp
//
//            val close = feedResponse.feeds["NSE_INDEX|Nifty 50"]?.ff?.indexFF?.ltpc?.cp
//
//            // UI-related code
//            (context as? Activity)?.runOnUiThread {
//
//                if (ltp0!! >= close!!){
//                    val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
//                    binding.tvFirstPosPriceChange.setTextColor(resolvedColor)
//                    // Change drawable icon programmatically
//                    val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
//                    binding.tvFirstPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
//                    binding.tvFirstPosPriceChange.compoundDrawablePadding = 8
//                    val newPrice = ltp0 - close
//                    val perc = newPrice / close * 100
//                    binding.tvFirstPosPercentageChange.text =  "+${formatIntWithDecimal(newPrice)} (+${formatIntWithDecimal(perc)}%)"
//
//                }else{
//
//                    val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
//                    binding.tvFirstPosPriceChange.setTextColor(resolvedColor)
//                    // Change drawable icon programmatically
//                    val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
//                    binding.tvFirstPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
//                    binding.tvFirstPosPriceChange.compoundDrawablePadding = 8
//                    val newPrice = ltp0 - close
//                    val perc = newPrice / close * 100
//                    binding.tvFirstPosPercentageChange.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"
//
//
//                }
//
//
//                binding.tvFirstPosPriceChange.text = formatIntWithDecimal(ltp0!!)
//
//                // Continue handling as needed...
//            }
//        } catch (e: Exception) {
//            Log.e("WebSocket", "Error parsing binary message: ${e.message}")
//        }
//    }
//
//    private fun handleWebSocketClientError(errorMessage: String) {
//        (context as? Activity)?.runOnUiThread {
//            // Handle the WebSocket client error on the UI thread
//            Log.e("WebSocket", errorMessage)
//        }
//    }
//
//    private fun handleAuthError(errorMessage: String) {
//        (context as? Activity)?.runOnUiThread {
//            // Handle the authentication error on the UI thread
//            Log.e("WebSocket", errorMessage)
//        }
//    }
//
//
//
//    fun formatIntWithDecimal(value: Double): String {
//        val symbols = DecimalFormatSymbols(Locale.US)
//        symbols.groupingSeparator = ','
//        val formatter = DecimalFormat("##,##0.00", symbols)
//        return formatter.format(value)
//    }
//
//    override fun onLtpReceived(ltp: Double, cp: Double) {
//
//        if (ltp >= cp){
//            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.buy_green)
//            binding.tvSecondPosPriceChange.setTextColor(resolvedColor)
//            // Change drawable icon programmatically
//            val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_gain)
//            binding.tvSecondPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
//            binding.tvSecondPosPriceChange.compoundDrawablePadding = 8
//            val newPrice = ltp - cp
//            val perc = newPrice / cp * 100
//            binding.tvSecondPosPercentageChange.text =  "+${formatIntWithDecimal(newPrice)} (+${formatIntWithDecimal(perc)}%)"
//
//        }else{
//
//            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
//            binding.tvSecondPosPriceChange.setTextColor(resolvedColor)
//            // Change drawable icon programmatically
//            val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
//            binding.tvSecondPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
//            binding.tvSecondPosPriceChange.compoundDrawablePadding = 8
//
//            val newPrice = ltp - cp
//            val perc = newPrice / cp * 100
//            binding.tvSecondPosPercentageChange.text =  "${formatIntWithDecimal(newPrice)} (${formatIntWithDecimal(perc)}%)"
//
//
//        }
//
//        binding.tvSecondPosPriceChange.text = formatIntWithDecimal(ltp)
//
//    }
//
//





}