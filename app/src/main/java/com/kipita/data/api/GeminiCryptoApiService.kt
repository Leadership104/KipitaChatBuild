package com.kipita.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header

// ---------------------------------------------------------------------------
// Gemini REST API
// Base URL: https://api.gemini.com/
// Auth: Gemini API key + HMAC-SHA384 signature in headers
// ---------------------------------------------------------------------------

interface GeminiCryptoApiService {

    /**
     * Get all available balances across all currencies in the account.
     * Auth: Gemini requires X-GEMINI-APIKEY + X-GEMINI-SIGNATURE headers.
     */
    @GET("v1/balances")
    suspend fun getBalances(
        @Header("X-GEMINI-APIKEY") apiKey: String,
        @Header("X-GEMINI-PAYLOAD") payload: String,
        @Header("X-GEMINI-SIGNATURE") signature: String
    ): List<GeminiBalanceDto>

    /**
     * Get ticker data for a symbol (e.g., BTCUSD) — REST fallback when
     * WebSocket is unavailable.
     */
    @GET("v1/pubticker/{symbol}")
    suspend fun getTicker(
        @retrofit2.http.Path("symbol") symbol: String
    ): GeminiTickerDto
}

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class GeminiBalanceDto(
    @param:Json(name = "type") val type: String,           // "exchange"
    @param:Json(name = "currency") val currency: String,   // "BTC", "ETH", "USD"
    @param:Json(name = "amount") val amount: String,
    @param:Json(name = "available") val available: String,
    @param:Json(name = "availableForWithdrawal") val availableForWithdrawal: String
)

@JsonClass(generateAdapter = true)
data class GeminiTickerDto(
    @param:Json(name = "bid") val bid: String,
    @param:Json(name = "ask") val ask: String,
    @param:Json(name = "last") val last: String,
    @param:Json(name = "volume") val volume: GeminiVolumeDto
)

@JsonClass(generateAdapter = true)
data class GeminiVolumeDto(
    @param:Json(name = "BTC") val btc: String? = null,
    @param:Json(name = "USD") val usd: String? = null,
    @param:Json(name = "ETH") val eth: String? = null,
    @param:Json(name = "timestamp") val timestamp: Long = 0
)

// ---------------------------------------------------------------------------
// Gemini WebSocket message models (for the Market Data feed)
// wss://api.gemini.com/v1/marketdata/{symbol}
// ---------------------------------------------------------------------------

data class GeminiWsEvent(
    val type: String,          // "update" | "heartbeat"
    val eventId: Long,
    val timestamp: Long,
    val events: List<GeminiWsMarketEvent>
)

data class GeminiWsMarketEvent(
    val type: String,          // "trade" | "change" | "auction"
    val price: String,
    val quantity: String,
    val side: String,          // "bid" | "ask"
    val reason: String         // "place" | "trade" | "cancel" | "initial"
)
