package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.domain.usecase.GeminiUsageLimiter
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GeminiUsageLimiterTest {
    @Test
    fun `allows calls under free tier limits`() = runTest {
        val limiter = GeminiUsageLimiter(rpmLimit = 2, rpdLimit = 3)
        limiter.onRequest(Instant.parse("2026-01-01T00:00:00Z"))
        limiter.onRequest(Instant.parse("2026-01-01T00:00:10Z"))
        assertThat(true).isTrue()
    }

    @Test
    fun `blocks rpm overflow corner case`() = runTest {
        val limiter = GeminiUsageLimiter(rpmLimit = 1, rpdLimit = 5)
        limiter.onRequest(Instant.parse("2026-01-01T00:00:00Z"))
        var caughtException: IllegalStateException? = null
        try {
            limiter.onRequest(Instant.parse("2026-01-01T00:00:10Z"))
        } catch (e: IllegalStateException) {
            caughtException = e
        }
        assertThat(caughtException).isNotNull()
    }

    @Test
    fun `blocks rpd overflow corner corner case`() = runTest {
        val limiter = GeminiUsageLimiter(rpmLimit = 5, rpdLimit = 1)
        limiter.onRequest(Instant.parse("2026-01-01T00:00:00Z"))
        var caughtException: IllegalStateException? = null
        try {
            limiter.onRequest(Instant.parse("2026-01-01T01:10:00Z"))
        } catch (e: IllegalStateException) {
            caughtException = e
        }
        assertThat(caughtException).isNotNull()
    }
}
