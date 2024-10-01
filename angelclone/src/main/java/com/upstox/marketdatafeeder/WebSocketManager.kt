package com.upstox.marketdatafeeder

import android.app.Activity
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.upstox.ApiClient
import com.upstox.Configuration
import com.upstox.auth.OAuth
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

// WebSocketManager.kt

interface WebSocketCallback {
    fun onLtpReceived(ltp: Double,cp:Double)
}

class WebSocketManager(private val webSocketCallback: WebSocketCallback , private val activityRef: WeakReference<Activity>) {

    private lateinit var client: WebSocketClient

    fun launchWebSocket(accessToken: String, instrumentKeys: List<String>) {


        GlobalScope.launch(Dispatchers.IO) {
            try {
                val authenticatedClient = authenticateApiClient(accessToken)
                val serverUri = getAuthorizedWebSocketUri(authenticatedClient)

                launch(Dispatchers.Main) {
                    try {
                         client = createWebSocketClient(serverUri , instrumentKeys)
                        client.connect()
                    } catch (e: Exception) {
                        handleWebSocketClientError("Error creating WebSocket client: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                handleAuthError("Error authenticating API client: ${e.message}")
            }
        }


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

    private fun createWebSocketClient(serverUri: URI, instrumentKeys: List<String>): WebSocketClient {
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
            val ltp = feedResponse.feeds["NSE_INDEX|Nifty Bank"]?.ff?.indexFF?.ltpc?.ltp
            val ltp2 = feedResponse.feeds["NSE_INDEX|Nifty Bank"]?.ff?.indexFF?.ltpc?.cp

            activityRef.get()?.runOnUiThread {
                webSocketCallback.onLtpReceived(ltp ?: 0.0 , ltp2 ?: 0.0  )
            }
            // Continue handling as needed...
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing binary message: ${e.message}")
        }
    }


    private fun handleWebSocketClientError(errorMessage: String) {
        activityRef.get()?.runOnUiThread {
            // Handle the WebSocket client error on the UI thread
            Log.e("WebSocket", errorMessage)

        }
    }


    private fun handleAuthError(errorMessage: String) {
        activityRef.get()?.runOnUiThread {
            // Handle the authentication error on the UI thread
            Log.e("WebSocket", errorMessage)

        }
    }
}
