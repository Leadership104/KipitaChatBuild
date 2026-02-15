package com.kipita.domain.model

import java.time.Instant

data class LatLng(val latitude: Double, val longitude: Double)

enum class NoticeCategory { SAFETY, HEALTH, ADVISORY, INFRASTRUCTURE }

enum class SeverityLevel { LOW, MEDIUM, HIGH, CRITICAL }

data class TravelNotice(
    val title: String,
    val description: String,
    val location: LatLng,
    val category: NoticeCategory,
    val severity: SeverityLevel,
    val sourceName: String,
    val sourceUrl: String,
    val verified: Boolean,
    val lastUpdated: Instant
)

sealed class TravelAlert {
    data class Safe(val notices: List<TravelNotice>) : TravelAlert()
    data class Warning(val notices: List<TravelNotice>, val score: SafetyScore) : TravelAlert()
    data class Critical(val notices: List<TravelNotice>, val score: SafetyScore) : TravelAlert()
}

sealed class SafetyScore {
    data class Value(val score: Int, val confidence: Double) : SafetyScore()
    data object Unknown : SafetyScore()
}

sealed class HealthNotice {
    data class Advisory(val notice: TravelNotice) : HealthNotice()
    data class Outbreak(val notice: TravelNotice) : HealthNotice()
}

sealed class InfrastructureNotice {
    data class TransitDelay(val notice: TravelNotice) : InfrastructureNotice()
    data class AirportDisruption(val notice: TravelNotice) : InfrastructureNotice()
}
