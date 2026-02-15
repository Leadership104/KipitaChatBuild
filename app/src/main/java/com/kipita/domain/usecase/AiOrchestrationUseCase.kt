package com.kipita.domain.usecase

import com.kipita.domain.model.SafetyScore
import com.kipita.domain.model.TravelAlert
import java.time.Instant

data class AiInsight(
    val summary: String,
    val confidence: Double,
    val timestamp: Instant,
    val citedSources: List<String>
)

class AiOrchestrationUseCase(
    private val travelDataEngine: TravelDataEngine
) {
    suspend fun assessRegion(region: String): AiInsight {
        val alert = travelDataEngine.evaluateAlert(region)
        val notices = when (alert) {
            is TravelAlert.Safe -> alert.notices
            is TravelAlert.Warning -> alert.notices
            is TravelAlert.Critical -> alert.notices
        }
        val score = travelDataEngine.computeSafetyScore(notices)
        val confidence = (score as? SafetyScore.Value)?.confidence ?: 0.0
        val label = when (alert) {
            is TravelAlert.Safe -> "generally safe"
            is TravelAlert.Warning -> "use caution"
            is TravelAlert.Critical -> "high risk"
        }
        return AiInsight(
            summary = "Region $region is $label based on ${notices.size} verified government notices.",
            confidence = confidence,
            timestamp = Instant.now(),
            citedSources = notices.map { "${it.sourceName} (${it.lastUpdated})" }.distinct()
        )
    }
}
