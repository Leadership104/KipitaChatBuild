package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.data.repository.AdvisoryRepository
import com.kipita.data.repository.HealthRepository
import com.kipita.data.repository.SafetyRepository
import com.kipita.domain.model.NoticeCategory
import com.kipita.domain.model.SeverityLevel
import com.kipita.domain.model.TravelNotice
import com.kipita.domain.usecase.AiOrchestrationUseCase
import com.kipita.domain.usecase.TravelDataEngine
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AiSafetyScoringTest {
    @Test
    fun `ai insight includes confidence and sources`() = runTest {
        val safety = mockk<SafetyRepository>()
        val health = mockk<HealthRepository>()
        val advisory = mockk<AdvisoryRepository>()
        val notice = TravelNotice(
            "warning", "desc", com.kipita.domain.model.LatLng(0.0, 0.0), NoticeCategory.HEALTH,
            SeverityLevel.MEDIUM, "CDC", "https://cdc.gov", true, Instant.now()
        )
        coEvery { safety.fetch(any()) } returns listOf(notice)
        coEvery { health.fetch(any()) } returns emptyList()
        coEvery { advisory.fetch(any()) } returns emptyList()

        val useCase = AiOrchestrationUseCase(TravelDataEngine(safety, health, advisory))
        val insight = useCase.assessRegion("test")

        assertThat(insight.confidence).isGreaterThan(0.0)
        assertThat(insight.citedSources).isNotEmpty()
    }
}
