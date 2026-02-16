package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.domain.usecase.GeminiUsageLimiter
import java.time.Instant
import org.junit.Test

class GeminiUsageLimiterTest {
    @Test
    fun `allows calls under free tier limits`() {
        val limiter = GeminiUsageLimiter(rpmLimit = 2, rpdLimit = 3)
        limiter.onRequest(Instant.parse("2026-01-01T00:00:00Z"))
        limiter.onRequest(Instant.parse("2026-01-01T00:00:10Z"))
        assertThat(true).isTrue()
    }

    @Test(expected = IllegalStateException::class)
    fun `blocks rpm overflow corner case`() {
        val limiter = GeminiUsageLimiter(rpmLimit = 1, rpdLimit = 5)
        limiter.onRequest(Instant.parse("2026-01-01T00:00:00Z"))
        limiter.onRequest(Instant.parse("2026-01-01T00:00:10Z"))
    }

    @Test(expected = IllegalStateException::class)
    fun `blocks rpd overflow corner corner case`() {
        val limiter = GeminiUsageLimiter(rpmLimit = 5, rpdLimit = 1)
        limiter.onRequest(Instant.parse("2026-01-01T00:00:00Z"))
        limiter.onRequest(Instant.parse("2026-01-01T01:10:00Z"))
    }
}
