package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.data.repository.AdvisoryRepository
import com.kipita.data.repository.HealthRepository
import com.kipita.data.repository.SafetyRepository
import com.kipita.domain.model.NoticeCategory
import com.kipita.domain.model.SeverityLevel
import com.kipita.domain.model.TravelNotice
import com.kipita.domain.usecase.TravelDataEngine
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TravelDataEngineTest {
    private val safetyRepo = mockk<SafetyRepository>()
    private val healthRepo = mockk<HealthRepository>()
    private val advisoryRepo = mockk<AdvisoryRepository>()

    @Test
    fun `compute safety score should decrease with critical notices`() {
        val engine = TravelDataEngine(safetyRepo, healthRepo, advisoryRepo)
        val notices = listOf(notice(SeverityLevel.CRITICAL), notice(SeverityLevel.HIGH))

        val score = engine.computeSafetyScore(notices)

        assertThat(score).isInstanceOf(com.kipita.domain.model.SafetyScore.Value::class.java)
        val scoreValue = score as com.kipita.domain.model.SafetyScore.Value
        assertThat(scoreValue.score).isLessThan(80)
    }

    @Test
    fun `evaluate alert returns warning for mid score`() = runTest {
        val engine = TravelDataEngine(safetyRepo, healthRepo, advisoryRepo)
        coEvery { safetyRepo.fetch(any()) } returns listOf(notice(SeverityLevel.MEDIUM))
        coEvery { healthRepo.fetch(any()) } returns listOf(notice(SeverityLevel.HIGH))
        coEvery { advisoryRepo.fetch(any()) } returns emptyList()

        val alert = engine.evaluateAlert("region")

        assertThat(alert).isInstanceOf(com.kipita.domain.model.TravelAlert.Warning::class.java)
    }

    private fun notice(severity: SeverityLevel) = TravelNotice(
        title = "Notice",
        description = "desc",
        location = com.kipita.domain.model.LatLng(1.0, 1.0),
        category = NoticeCategory.SAFETY,
        severity = severity,
        sourceName = "CDC",
        sourceUrl = "https://cdc.gov/travel",
        verified = true,
        lastUpdated = Instant.now()
    )
}
