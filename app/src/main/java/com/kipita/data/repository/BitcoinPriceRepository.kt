package com.kipita.data.repository

import com.kipita.data.api.BitcoinPriceApiService
import com.kipita.data.api.CoinGeckoPriceDto
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// BitcoinPriceRepository
//
// Fetches real-time BTC, ETH, SOL prices from CoinGecko (free API, no key).
// 30-second cache prevents excessive API hits while keeping prices fresh.
// ---------------------------------------------------------------------------

data class CryptoPrices(
    val btcUsd: Double,
    val btcChange24h: Double,
    val ethUsd: Double,
    val ethChange24h: Double,
    val solUsd: Double,
    val solChange24h: Double,
    val fetchedAtMs: Long = System.currentTimeMillis()
) {
    val btcChangeIsPositive: Boolean get() = btcChange24h >= 0
    val ethChangeIsPositive: Boolean get() = ethChange24h >= 0
}

@Singleton
class BitcoinPriceRepository @Inject constructor(
    private val api: BitcoinPriceApiService
) {
    private var cached: CryptoPrices? = null
    private val cacheMaxAgeMs = 30_000L // 30 seconds

    suspend fun getPrices(forceRefresh: Boolean = false): CryptoPrices {
        val cache = cached
        if (!forceRefresh && cache != null &&
            System.currentTimeMillis() - cache.fetchedAtMs < cacheMaxAgeMs
        ) {
            return cache
        }

        return try {
            val response = api.getPrices()
            val btc = response["bitcoin"] ?: CoinGeckoPriceDto(0.0)
            val eth = response["ethereum"] ?: CoinGeckoPriceDto(0.0)
            val sol = response["solana"] ?: CoinGeckoPriceDto(0.0)
            CryptoPrices(
                btcUsd = btc.usd,
                btcChange24h = btc.usd24hChange ?: 0.0,
                ethUsd = eth.usd,
                ethChange24h = eth.usd24hChange ?: 0.0,
                solUsd = sol.usd,
                solChange24h = sol.usd24hChange ?: 0.0
            ).also { cached = it }
        } catch (e: Exception) {
            // Return cached data on failure, or offline placeholder
            cache ?: CryptoPrices(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }
}
