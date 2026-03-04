package com.kipita.data.repository

import com.kipita.data.api.BitcoinPriceApiService
import com.kipita.data.api.CoinbaseApiService
import com.kipita.data.api.CurrencyApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

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
    private val api: BitcoinPriceApiService,
    private val coinbaseApi: CoinbaseApiService,
    private val currencyApi: CurrencyApiService
) {
    private val baseline = CryptoPrices(
        btcUsd = 98_000.0,
        btcChange24h = 0.0,
        ethUsd = 3_400.0,
        ethChange24h = 0.0,
        solUsd = 180.0,
        solChange24h = 0.0
    )
    private var cached: CryptoPrices? = null
    private val cacheMaxAgeMs = 2_000L // 2 seconds

    suspend fun getPrices(forceRefresh: Boolean = false): CryptoPrices {
        val cache = cached
        if (!forceRefresh && cache != null &&
            System.currentTimeMillis() - cache.fetchedAtMs < cacheMaxAgeMs
        ) {
            return cache
        }

        val response = runCatching { api.getPrices() }
            .recoverCatching {
                delay(500)
                api.getPrices()
            }
            .getOrNull()

        if (response != null) {
            try {
                val btc = response["bitcoin"]
                val eth = response["ethereum"]
                val sol = response["solana"]
                val parsed = CryptoPrices(
                    btcUsd = btc?.usd ?: cache?.btcUsd ?: 0.0,
                    btcChange24h = btc?.usd24hChange ?: cache?.btcChange24h ?: 0.0,
                    ethUsd = eth?.usd ?: cache?.ethUsd ?: 0.0,
                    ethChange24h = eth?.usd24hChange ?: cache?.ethChange24h ?: 0.0,
                    solUsd = sol?.usd ?: cache?.solUsd ?: 0.0,
                    solChange24h = sol?.usd24hChange ?: cache?.solChange24h ?: 0.0
                )
                if (parsed.btcUsd > 0.0 || parsed.ethUsd > 0.0 || parsed.solUsd > 0.0) {
                    cached = parsed
                    return parsed
                }
            } catch (_: Exception) {
                // Fall through to fallback source.
            }
        }

        val fallback = runCatching { fetchFromCoinbase(cache) }.getOrNull()
        if (fallback != null) {
            cached = fallback
            return fallback
        }

        val fxFallback = runCatching { fetchFromFxHost(cache) }.getOrNull()
        if (fxFallback != null && (fxFallback.btcUsd > 0.0 || fxFallback.ethUsd > 0.0 || fxFallback.solUsd > 0.0)) {
            cached = fxFallback
            return fxFallback
        }

        if (cache != null) return cache
        // Keep wallet cards populated in constrained demo networks; live poll keeps retrying.
        cached = baseline
        return baseline
    }

    private suspend fun fetchFromCoinbase(cache: CryptoPrices?): CryptoPrices = coroutineScope {
        val btcDeferred = async { coinbaseApi.getSpotPrice("BTC-USD").data.amount.toDoubleOrNull() }
        val ethDeferred = async { coinbaseApi.getSpotPrice("ETH-USD").data.amount.toDoubleOrNull() }
        val solDeferred = async { coinbaseApi.getSpotPrice("SOL-USD").data.amount.toDoubleOrNull() }

        val btc = btcDeferred.await() ?: cache?.btcUsd ?: 0.0
        val eth = ethDeferred.await() ?: cache?.ethUsd ?: 0.0
        val sol = solDeferred.await() ?: cache?.solUsd ?: 0.0

        CryptoPrices(
            btcUsd = btc,
            btcChange24h = cache?.btcChange24h ?: 0.0,
            ethUsd = eth,
            ethChange24h = cache?.ethChange24h ?: 0.0,
            solUsd = sol,
            solChange24h = cache?.solChange24h ?: 0.0
        )
    }

    private suspend fun fetchFromFxHost(cache: CryptoPrices?): CryptoPrices {
        val rates = currencyApi.getRates(base = "USD", symbols = "BTC,ETH,SOL").rates
        val btc = rates["BTC"]?.takeIf { it > 0.0 }?.let { 1.0 / it } ?: cache?.btcUsd ?: 0.0
        val eth = rates["ETH"]?.takeIf { it > 0.0 }?.let { 1.0 / it } ?: cache?.ethUsd ?: 0.0
        val sol = rates["SOL"]?.takeIf { it > 0.0 }?.let { 1.0 / it } ?: cache?.solUsd ?: 0.0
        return CryptoPrices(
            btcUsd = btc,
            btcChange24h = cache?.btcChange24h ?: 0.0,
            ethUsd = eth,
            ethChange24h = cache?.ethChange24h ?: 0.0,
            solUsd = sol,
            solChange24h = cache?.solChange24h ?: 0.0
        )
    }
}
