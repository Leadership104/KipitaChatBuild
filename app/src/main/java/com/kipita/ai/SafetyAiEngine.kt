package com.kipita.ai

import com.kipita.data.api.DwaatApiService
import com.kipita.data.api.DwaatAdvisoryRequest
import com.kipita.data.api.DwaatWeatherAdvisoryRequest
import com.kipita.data.api.DwaatRestrictionsRequest
import com.kipita.data.api.DwaatAdvisorySection
import com.kipita.data.api.DwaatWeatherAdvisory
import javax.inject.Inject
import javax.inject.Singleton

data class RealTimeSafetyReport(
    val country: String,
    val weatherCondition: String,
    val weatherTemperature: String,
    val weatherLevel: String,           // "safe" | "caution" | "danger"
    val advisorySections: List<DwaatAdvisorySection>,
    val restrictionsSummary: String,
    val aiInsight: String,              // Gemini synthesis
    val overallLevel: Int               // 1-4 (State Dept scale)
)

@Singleton
class SafetyAiEngine @Inject constructor(
    private val dwaatApiService: DwaatApiService,
    private val kipitaAI: KipitaAIManager
) {
    /**
     * Fetches real-time safety data from the Dwaat API (advisory sections,
     * weather advisory, restrictions) and then asks Gemini to synthesize a
     * concise, actionable safety briefing for travelers.
     *
     * @param country ISO country name / code accepted by the Dwaat backend.
     * @param lat     User's latitude (used for weather advisory).
     * @param lng     User's longitude (used for weather advisory).
     */
    suspend fun analyze(
        country: String,
        lat: Double = 0.0,
        lng: Double = 0.0
    ): RealTimeSafetyReport {
        // ── 1. Parallel fetch from Dwaat ──────────────────────────────────────
        var advisorySections: List<DwaatAdvisorySection> = emptyList()
        var weather: DwaatWeatherAdvisory? = null
        var restrictionsSummary = ""

        runCatching {
            advisorySections = dwaatApiService
                .getAdvisorySections(DwaatAdvisoryRequest(country = country))
                .data ?: emptyList()
        }

        if (lat != 0.0 || lng != 0.0) {
            runCatching {
                weather = dwaatApiService
                    .getWeatherAdvisory(DwaatWeatherAdvisoryRequest(lat = lat, lng = lng, country = country))
                    .data
            }
        }

        runCatching {
            val restrictions = dwaatApiService
                .getRestrictions(DwaatRestrictionsRequest(country = country))
                .data
            val mainMap = restrictions?.advisory_main ?: emptyMap()
            restrictionsSummary = mainMap.entries.take(4)
                .joinToString("; ") { (k, v) -> "$k: $v" }
        }

        // ── 2. Build Gemini context ────────────────────────────────────────────
        val contextLines = buildString {
            appendLine("Country: $country")
            if (weather != null) {
                appendLine("Weather: ${weather!!.condition}, ${weather!!.temperature} — ${weather!!.advisory}")
            }
            if (advisorySections.isNotEmpty()) {
                appendLine("Advisory sections:")
                advisorySections.forEach { s ->
                    appendLine("  • [${s.level?.uppercase() ?: "INFO"}] ${s.title}: ${s.content}")
                }
            }
            if (restrictionsSummary.isNotBlank()) {
                appendLine("Restrictions: $restrictionsSummary")
            }
        }

        val aiInsight = kipitaAI.analyzeSafetyContext(contextLines, country)

        // ── 3. Compute overall level ───────────────────────────────────────────
        val overallLevel = computeLevel(advisorySections, weather)

        return RealTimeSafetyReport(
            country = country,
            weatherCondition = weather?.condition ?: "",
            weatherTemperature = weather?.temperature ?: "",
            weatherLevel = weather?.level ?: "safe",
            advisorySections = advisorySections,
            restrictionsSummary = restrictionsSummary,
            aiInsight = aiInsight,
            overallLevel = overallLevel
        )
    }

    private fun computeLevel(
        sections: List<DwaatAdvisorySection>,
        weather: DwaatWeatherAdvisory?
    ): Int {
        val hasDanger = sections.any { it.level == "danger" } || weather?.level == "danger"
        val hasCaution = sections.any { it.level == "caution" } || weather?.level == "caution"
        return when {
            hasDanger -> 4
            hasCaution -> 3
            sections.isEmpty() && weather == null -> 2
            else -> 1
        }
    }
}
