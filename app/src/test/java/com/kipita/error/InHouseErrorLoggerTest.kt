package com.kipita.error

import com.google.common.truth.Truth.assertThat
import com.kipita.data.api.ErrorReportApiService
import com.kipita.data.local.ErrorLogDao
import com.kipita.data.local.ErrorLogEntity
import com.kipita.data.error.InHouseErrorLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InHouseErrorLoggerTest {
    @Test
    fun `log stores and attempts send`() = runTest {
        val dao = mockk<ErrorLogDao>()
        val api = mockk<ErrorReportApiService>()
        coEvery { dao.upsert(any()) } returns Unit
        coEvery { dao.getUnsent() } returns listOf(
            ErrorLogEntity("1", "tag", "m", "stack", 1L, false)
        )
        coEvery { api.sendErrorReport(any()) } returns Unit
        coEvery { dao.markSent(any()) } returns Unit

        val logger = InHouseErrorLogger(dao, api)
        logger.log("test", IllegalStateException("boom"))

        coVerify(atLeast = 1) { dao.upsert(any()) }
        coVerify { api.sendErrorReport(any()) }
        coVerify { dao.markSent(listOf("1")) }
    }

    @Test
    fun `flush keeps unsent on api failure corner case`() = runTest {
        val dao = mockk<ErrorLogDao>()
        val api = mockk<ErrorReportApiService>()
        coEvery { dao.getUnsent() } returns listOf(
            ErrorLogEntity("1", "tag", "m", "stack", 1L, false)
        )
        coEvery { api.sendErrorReport(any()) } throws RuntimeException("network")
        coEvery { dao.markSent(any()) } returns Unit

        val logger = InHouseErrorLogger(dao, api)
        logger.flushUnsent()

        coVerify(exactly = 0) { dao.markSent(any()) }
    }

    @Test
    fun `allLogs returns dao values`() = runTest {
        val dao = mockk<ErrorLogDao>()
        val api = mockk<ErrorReportApiService>()
        val logs = listOf(ErrorLogEntity("x", "tag", "message", "stack", 1L, true))
        coEvery { dao.getAll() } returns logs

        val logger = InHouseErrorLogger(dao, api)
        assertThat(logger.allLogs()).isEqualTo(logs)
    }
}
