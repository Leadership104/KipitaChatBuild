package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.data.api.GovernmentApiService
import com.kipita.data.api.TravelNoticeDto
import com.kipita.data.local.TravelNoticeDao
import com.kipita.data.local.TravelNoticeEntity
import com.kipita.data.repository.SafetyRepository
import com.kipita.data.validation.DataValidationLayer
import com.kipita.data.validation.SourceVerificationLayer
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RepositoryCachingTest {
    @Test
    fun `uses cache when api fails`() = runTest {
        val api = mockk<GovernmentApiService>()
        val dao = mockk<TravelNoticeDao>()
        val validator = DataValidationLayer(SourceVerificationLayer(setOf("cdc.gov")))
        val repository = SafetyRepository(api, dao, validator)

        coEvery { api.getSafetyNotices(any()) } throws RuntimeException("network")
        coEvery { dao.findByCategory(any()) } returns listOf(
            TravelNoticeEntity(
                id = "1",
                title = "Cached",
                description = "Cached desc",
                latitude = 1.0,
                longitude = 1.0,
                category = "SAFETY",
                severity = "LOW",
                sourceName = "CDC",
                sourceUrl = "https://cdc.gov/x",
                verified = true,
                lastUpdatedEpochMillis = 1,
                retrievedAtEpochMillis = 1
            )
        )
        coEvery { dao.upsertAll(any()) } returns Unit

        val notices = repository.fetch("x")

        assertThat(notices).hasSize(1)
        assertThat(notices.first().title).isEqualTo("Cached")
    }

    @Test
    fun `normalizes verified response`() = runTest {
        val api = mockk<GovernmentApiService>()
        val dao = mockk<TravelNoticeDao>()
        val validator = DataValidationLayer(SourceVerificationLayer(setOf("who.int")))
        val repository = SafetyRepository(api, dao, validator)

        coEvery { api.getSafetyNotices(any()) } returns listOf(
            TravelNoticeDto("t", "d", 0.0, 0.0, "safety", "low", "WHO", "https://who.int/a", 1000L)
        )
        coEvery { dao.upsertAll(any()) } returns Unit

        val notices = repository.fetch("x")
        assertThat(notices.first().verified).isTrue()
    }
}
