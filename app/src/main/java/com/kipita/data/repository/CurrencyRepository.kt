package com.kipita.data.repository

import com.kipita.data.api.CurrencyApiService
import com.kipita.domain.model.CurrencyConversion
import java.time.Instant

class CurrencyRepository(private val api: CurrencyApiService) {
    suspend fun convert(amount: Double, from: String, to: String): CurrencyConversion {
        val response = api.getRates(from.uppercase())
        val rate = response.rates[to.uppercase()] ?: 1.0
        return CurrencyConversion(
            from = from.uppercase(),
            to = to.uppercase(),
            rate = rate,
            convertedAmount = amount * rate,
            timestamp = Instant.ofEpochMilli(response.timestampEpochMillis)
        )
    }
}
