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

data class MerchantLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val acceptsOnchainBtc: Boolean,
    val acceptsLightning: Boolean,
    val acceptsCashApp: Boolean,
    val source: String,
    val lastVerified: Instant,
    val metadata: Map<String, String>
)

data class NomadPlaceInfo(
    val placeId: String,
    val city: String,
    val country: String,
    val costOfLivingUsd: Double,
    val internetMbps: Double,
    val safetyScore: Double,
    val walkabilityScore: Double,
    val weatherSummary: String,
    val timezone: String,
    val updatedAt: Instant
)

data class CurrencyConversion(
    val from: String,
    val to: String,
    val rate: Double,
    val convertedAmount: Double,
    val timestamp: Instant
)

data class TripMessage(
    val id: String,
    val tripId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val createdAt: Instant,
    val isAi: Boolean = false
)

data class TripPlan(
    val tripId: String,
    val title: String,
    val participantIds: List<String>,
    val itineraryDraft: List<String>,
    val updatedAt: Instant
)

enum class LlmProvider { OPENAI, CLAUDE, GEMINI }

data class LlmPrompt(
    val provider: LlmProvider,
    val input: String,
    val structured: Boolean = false
)

data class LlmResult(
    val provider: LlmProvider,
    val content: String,
    val model: String,
    val confidence: Double,
    val functionJson: String? = null,
    val timestamp: Instant = Instant.now()
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
