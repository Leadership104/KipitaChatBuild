package com.kipita.data.repository

import com.kipita.data.api.CurrencyApiService
import com.kipita.domain.model.CurrencyConversion
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CurrencyRepository(private val api: CurrencyApiService) {

    // Approximate crypto rates (BTC, ETH) vs USD since Frankfurter doesn't cover crypto
    private val cryptoApproxRatesPerUsd = mapOf(
        "BTC" to 1.0 / 96_000.0,   // ~96,000 USD per BTC
        "ETH" to 1.0 / 3_400.0      // ~3,400 USD per ETH
    )

    suspend fun convert(amount: Double, from: String, to: String): CurrencyConversion = withContext(Dispatchers.IO) {
        val fromUpper = from.uppercase()
        val toUpper = to.uppercase()
        val isCrypto = { c: String -> c == "BTC" || c == "ETH" }

        when {
            !isCrypto(fromUpper) && !isCrypto(toUpper) -> {
                // Fiat-to-fiat via Frankfurter (ECB real-time data)
                val response = api.getRates(fromUpper, toUpper)
                val rate = response.rates[toUpper] ?: 1.0
                val dateMillis = runCatching {
                    LocalDate.parse(response.date).atStartOfDay(ZoneOffset.UTC).toInstant()
                }.getOrElse { Instant.now() }
                CurrencyConversion(from = fromUpper, to = toUpper, rate = rate, convertedAmount = amount * rate, timestamp = dateMillis)
            }
            isCrypto(fromUpper) && !isCrypto(toUpper) -> {
                // Crypto-to-fiat: crypto -> USD -> target fiat
                val usdPerCrypto = 1.0 / (cryptoApproxRatesPerUsd[fromUpper] ?: 1.0)
                val fiatResponse = api.getRates("USD", toUpper)
                val usdToFiat = fiatResponse.rates[toUpper] ?: 1.0
                val finalRate = usdPerCrypto * usdToFiat
                CurrencyConversion(from = fromUpper, to = toUpper, rate = finalRate, convertedAmount = amount * finalRate, timestamp = Instant.now())
            }
            !isCrypto(fromUpper) && isCrypto(toUpper) -> {
                // Fiat-to-crypto: fiat -> USD -> crypto
                val fiatResponse = api.getRates(fromUpper, "USD")
                val fiatToUsd = fiatResponse.rates["USD"] ?: 1.0
                val cryptoRate = (cryptoApproxRatesPerUsd[toUpper] ?: 1.0) * fiatToUsd
                CurrencyConversion(from = fromUpper, to = toUpper, rate = cryptoRate, convertedAmount = amount * cryptoRate, timestamp = Instant.now())
            }
            else -> {
                // Crypto-to-crypto: via USD pivot
                val fromUsdValue = 1.0 / (cryptoApproxRatesPerUsd[fromUpper] ?: 1.0)
                val toUsdValue = 1.0 / (cryptoApproxRatesPerUsd[toUpper] ?: 1.0)
                val rate = toUsdValue / fromUsdValue
                CurrencyConversion(from = fromUpper, to = toUpper, rate = rate, convertedAmount = amount * rate, timestamp = Instant.now())
            }
        }
    }

    suspend fun getAvailableCurrencies(): Map<String, String> = withContext(Dispatchers.IO) {
        runCatching { api.getCurrencies() }.getOrElse { emptyMap() }
    }
}
