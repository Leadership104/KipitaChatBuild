package com.kipita.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

// ---------------------------------------------------------------------------
// Bitcoin / Crypto Price API — CoinGecko (free tier, no API key required)
// Base URL: https://api.coingecko.com/api/v3/
// ---------------------------------------------------------------------------

interface BitcoinPriceApiService {

    /**
     * Fetches live USD prices + 24h change for BTC, ETH, and SOL.
     * No API key required — CoinGecko free tier supports ~30 req/min.
     */
    @GET("simple/price")
    suspend fun getPrices(
        @Query("ids") ids: String = "bitcoin,ethereum,solana",
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_24hr_change") include24hChange: Boolean = true,
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_last_updated_at") includeUpdatedAt: Boolean = true
    ): Map<String, CoinGeckoPriceDto>

    /**
     * Fetch OHLC data for a single coin (requires CoinGecko Pro on live,
     * useful for charting — template for future implementation).
     */
    @GET("coins/{id}/ohlc")
    suspend fun getOhlc(
        @retrofit2.http.Path("id") coinId: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: Int = 7
    ): List<List<Double>> // [[timestamp, open, high, low, close], ...]
}

// ---------------------------------------------------------------------------
// DTOs — CoinGecko returns a flat map keyed by coin id
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class CoinGeckoPriceDto(
    @Json(name = "usd") val usd: Double,
    @Json(name = "usd_24h_change") val usd24hChange: Double? = null,
    @Json(name = "usd_market_cap") val usdMarketCap: Double? = null,
    @Json(name = "last_updated_at") val lastUpdatedAt: Long? = null
)
