package com.kipita.domain.usecase

import com.kipita.data.repository.AdvisoryRepository
import com.kipita.data.repository.HealthRepository
import com.kipita.data.repository.SafetyRepository
import com.kipita.domain.model.SafetyScore
import com.kipita.domain.model.SeverityLevel
import com.kipita.domain.model.TravelAlert
import com.kipita.domain.model.TravelNotice
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TravelDataEngine(
    private val safetyRepository: SafetyRepository,
    private val healthRepository: HealthRepository,
    private val advisoryRepository: AdvisoryRepository
) {
    suspend fun collectRegionNotices(region: String): List<TravelNotice> = coroutineScope {
        val safety = async { safetyRepository.fetch(region) }
        val health = async { healthRepository.fetch(region) }
        val advisories = async { advisoryRepository.fetch(region) }
        safety.await() + health.await() + advisories.await()
    }

    suspend fun evaluateAlert(region: String): TravelAlert {
        val notices = collectRegionNotices(region)
        val score = computeSafetyScore(notices)
        return when {
            score is SafetyScore.Value && score.score >= 75 -> TravelAlert.Safe(notices)
            score is SafetyScore.Value && score.score >= 40 -> TravelAlert.Warning(notices, score)
            else -> TravelAlert.Critical(notices, score)
        }
    }

    fun computeSafetyScore(notices: List<TravelNotice>): SafetyScore {
        if (notices.isEmpty()) return SafetyScore.Unknown
        val weighted = notices.sumOf {
            when (it.severity) {
                SeverityLevel.LOW -> 1
                SeverityLevel.MEDIUM -> 2
                SeverityLevel.HIGH -> 4
                SeverityLevel.CRITICAL -> 6
            }
        }
        val rawScore = (100 - (weighted * 3)).coerceIn(0, 100)
        val confidence = (notices.count { it.verified }.toDouble() / notices.size).coerceIn(0.0, 1.0)
        return SafetyScore.Value(rawScore, confidence)
    }
}
