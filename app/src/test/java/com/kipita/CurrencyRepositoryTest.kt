package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.data.api.CurrencyApiService
import com.kipita.data.api.CurrencyRateDto
import com.kipita.data.repository.CurrencyRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CurrencyRepositoryTest {
    @Test
    fun `convert computes expected result`() = runTest {
        val api = mockk<CurrencyApiService>()
        coEvery { api.getRates("USD") } returns CurrencyRateDto("USD", mapOf("EUR" to 0.9), 1000)
        val repo = CurrencyRepository(api)

        val conversion = repo.convert(200.0, "usd", "eur")
        assertThat(conversion.convertedAmount).isEqualTo(180.0)
        assertThat(conversion.rate).isEqualTo(0.9)
    }

    @Test
    fun `missing rate falls back to 1 corner case`() = runTest {
        val api = mockk<CurrencyApiService>()
        coEvery { api.getRates("JPY") } returns CurrencyRateDto("JPY", emptyMap(), 1000)
        val repo = CurrencyRepository(api)

        val conversion = repo.convert(123.0, "jpy", "zzz")
        assertThat(conversion.convertedAmount).isEqualTo(123.0)
    }
}
