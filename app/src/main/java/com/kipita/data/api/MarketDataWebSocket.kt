package com.kipita.data.api

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// MarketDataWebSocket
//
// Connects to Gemini's real-time market data WebSocket feed:
//   wss://api.gemini.com/v1/marketdata/{symbol}
//
// Emits TickerUpdate events with the latest bid/ask/trade price.
// Used by WalletViewModel to show "Live USD Value" of crypto balances.
// Reconnects automatically on network interruptions.
// ---------------------------------------------------------------------------

data class TickerUpdate(
    val symbol: String,   // e.g. "BTCUSD"
    val price: Double,    // latest trade price in USD
    val bid: Double,
    val ask: Double
)

sealed class MarketFeedEvent {
    data class Tick(val update: TickerUpdate) : MarketFeedEvent()
    data class Connected(val symbol: String) : MarketFeedEvent()
    data class Disconnected(val symbol: String, val reason: String) : MarketFeedEvent()
    data class Error(val symbol: String, val message: String) : MarketFeedEvent()
}

@Singleton
class MarketDataWebSocket @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    // Hold active WebSocket connections keyed by symbol
    private val activeSockets = mutableMapOf<String, WebSocket>()

    // In-memory last known prices for quick reads without Flow subscription
    private val lastPrices = mutableMapOf<String, Double>()
    val prices: Map<String, Double> get() = lastPrices.toMap()

    /**
     * Returns a cold Flow for a given Gemini symbol (e.g. "BTCUSD", "ETHUSD").
     * Connects when collected, disconnects when cancelled.
     */
    fun observeSymbol(symbol: String): Flow<MarketFeedEvent> = callbackFlow {
        val url = "wss://api.gemini.com/v1/marketdata/$symbol?top_of_book=true&trades=true"
        val request = Request.Builder().url(url).build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                activeSockets[symbol] = webSocket
                trySend(MarketFeedEvent.Connected(symbol))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    if (type != "update") return

                    val events = json.optJSONArray("events") ?: return
                    var latestTradePrice = lastPrices[symbol] ?: 0.0
                    var latestBid = 0.0
                    var latestAsk = 0.0

                    for (i in 0 until events.length()) {
                        val event = events.getJSONObject(i)
                        when (event.optString("type")) {
                            "trade" -> latestTradePrice = event.optString("price").toDoubleOrNull() ?: latestTradePrice
                            "change" -> {
                                val price = event.optString("price").toDoubleOrNull() ?: continue
                                when (event.optString("side")) {
                                    "bid" -> latestBid = price
                                    "ask" -> latestAsk = price
                                }
                            }
                        }
                    }

                    if (latestTradePrice > 0) {
                        lastPrices[symbol] = latestTradePrice
                        trySend(MarketFeedEvent.Tick(
                            TickerUpdate(symbol, latestTradePrice, latestBid, latestAsk)
                        ))
                    }
                } catch (e: Exception) {
                    trySend(MarketFeedEvent.Error(symbol, e.message ?: "Parse error"))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                activeSockets.remove(symbol)
                trySend(MarketFeedEvent.Error(symbol, t.message ?: "Connection error"))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                activeSockets.remove(symbol)
                trySend(MarketFeedEvent.Disconnected(symbol, reason))
            }
        }

        val ws = okHttpClient.newWebSocket(request, listener)
        activeSockets[symbol] = ws

        awaitClose {
            ws.close(1000, "Flow cancelled")
            activeSockets.remove(symbol)
        }
    }

    /** Disconnect a specific symbol feed. */
    fun disconnect(symbol: String) {
        activeSockets[symbol]?.close(1000, "Manual disconnect")
        activeSockets.remove(symbol)
    }

    /** Disconnect all active feeds. */
    fun disconnectAll() {
        activeSockets.values.forEach { it.close(1000, "Disconnect all") }
        activeSockets.clear()
    }

    /** Returns the last known price for a symbol, or null if never seen. */
    fun lastPrice(symbol: String): Double? = lastPrices[symbol]
}
