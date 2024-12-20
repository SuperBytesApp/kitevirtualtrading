package com.upstox.marketdatafeeder.ui.frag

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.upstox.marketdatafeeder.databinding.FragmentOptionsBinding
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

class OptionsFragment : Fragment()  , WebSocketCallback {


    lateinit var binding: FragmentOptionsBinding
    private lateinit var userDataAdapter: WatchlistAdapter
    private val userDataList = mutableListOf<WachlistData>()
    private val webSocketManager by lazy { WebSocketManager(this, WeakReference(requireActivity())) }
    private lateinit var client: WebSocketClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOptionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val sharedPreferencesManager = WatchlistSharedPreferencesManager(requireContext())
//
//        val watchlistItems = sharedPreferencesManager.getWatchlist()
//
//        if (watchlistItems.isNotEmpty()) {
//            // Do something with the watchlistItems
//            // For example, update your RecyclerView adapter
//            userDataList.addAll(watchlistItems)
//            binding.constraintLayout.visibility = View.GONE
//        } else {
//            // Handle the case where there are no watchlist items
//            // For example, show a message or update the UI accordingly
//            binding.constraintLayout.visibility = View.VISIBLE
//        }
//
//
//        // Set up RecyclerView
//
//        userDataAdapter = WatchlistAdapter(userDataList,requireContext())
//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
//            reverseLayout = true
//            stackFromEnd = true
//        }
//
//        binding.recyclerView.adapter = userDataAdapter

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



        return root
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
                }else{

                    val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
                    binding.tvFirstPosPriceChange.setTextColor(resolvedColor)
                    // Change drawable icon programmatically
                    val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
                    binding.tvFirstPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
                    binding.tvFirstPosPriceChange.compoundDrawablePadding = 8

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
        }else{

            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.sell_red)
            binding.tvSecondPosPriceChange.setTextColor(resolvedColor)
            // Change drawable icon programmatically
            val yourDrawableIcon: Drawable? = requireActivity().resources.getDrawable(R.drawable.ic_arrow_loss)
            binding.tvSecondPosPriceChange.setCompoundDrawablesWithIntrinsicBounds(null, null, yourDrawableIcon, null)
            binding.tvSecondPosPriceChange.compoundDrawablePadding = 8

        }

        binding.tvSecondPosPriceChange.text = formatIntWithDecimal(ltp)
    }






}