package com.kipita.data.repository

import com.kipita.data.api.CurrencyApiService
import com.kipita.domain.model.CurrencyConversion
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CurrencyRepository(private val api: CurrencyApiService) {
    suspend fun convert(amount: Double, from: String, to: String): CurrencyConversion = withContext(Dispatchers.IO) {
        val response = api.getRates(from.uppercase())
        val rate = response.rates[to.uppercase()] ?: 1.0
        CurrencyConversion(
            from = from.uppercase(),
            to = to.uppercase(),
            rate = rate,
            convertedAmount = amount * rate,
            timestamp = Instant.ofEpochMilli(response.timestampEpochMillis)
        )
    }
}
