package com.kipita.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.ai.KipitaAIManager
import com.kipita.ai.SafetyAiEngine
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.domain.usecase.AiOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiOrchestrator: AiOrchestrator,
    private val kipitaAI: KipitaAIManager,
    private val safetyAiEngine: SafetyAiEngine,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {

    private val _response = MutableStateFlow<String?>(null)
    val response: StateFlow<String?> = _response.asStateFlow()

    val isAiTyping: StateFlow<Boolean> = kipitaAI.isAiTyping

    // Set when planTrip() is called — lets the UI show "Add to Trips"
    private val _lastPlanDestination = MutableStateFlow<String?>(null)
    val lastPlanDestination: StateFlow<String?> = _lastPlanDestination.asStateFlow()

    private val _lastPlanDays = MutableStateFlow(7)
    val lastPlanDays: StateFlow<Int> = _lastPlanDays.asStateFlow()

    fun analyze(region: String, prompt: String) {
        viewModelScope.launch {
            runCatching { aiOrchestrator.handleIntent(prompt, region) }
                .onSuccess { _response.value = it.naturalLanguage }
                .onFailure { errorLogger.log("AiViewModel.analyze", it) }
        }
    }

    fun chat(message: String) {
        viewModelScope.launch {
            runCatching { kipitaAI.chat(message) }
                .onSuccess { _response.value = it }
                .onFailure { errorLogger.log("AiViewModel.chat", it) }
        }
    }

    fun planTrip(destination: String, days: Int = 7) {
        _lastPlanDestination.value = destination
        _lastPlanDays.value = days
        viewModelScope.launch {
            runCatching { kipitaAI.planTrip(destination, days) }
                .onSuccess { _response.value = it }
                .onFailure { errorLogger.log("AiViewModel.planTrip", it) }
        }
    }

    /** Clears the last plan destination (e.g. after adding trip to calendar). */
    fun clearLastPlan() {
        _lastPlanDestination.value = null
    }

    fun parseNlpSearch(query: String) {
        viewModelScope.launch {
            runCatching { kipitaAI.parseNlpSearch(query) }
                .onSuccess { _response.value = it }
                .onFailure { errorLogger.log("AiViewModel.parseNlpSearch", it) }
        }
    }

    /**
     * Fetches live Dwaat advisory/weather/restriction data for [country],
     * then synthesizes a safety briefing via Gemini. Results shown in chat UI.
     */
    fun analyzeSafety(country: String, lat: Double = 0.0, lng: Double = 0.0) {
        _lastPlanDestination.value = null   // clear any trip plan state
        viewModelScope.launch {
            runCatching { safetyAiEngine.analyze(country, lat, lng) }
                .onSuccess { report ->
                    _response.value = buildString {
                        appendLine("📍 Safety Report: ${report.country}")
                        if (report.weatherLine.isNotBlank()) appendLine("🌤 ${report.weatherLine}")
                        appendLine()
                        appendLine(report.aiInsight)
                        if (report.restrictionsSummary.isNotBlank()) {
                            appendLine()
                            appendLine("Entry restrictions: ${report.restrictionsSummary}")
                        }
                    }.trim()
                }
                .onFailure { errorLogger.log("AiViewModel.analyzeSafety", it) }
        }
    }
}

// Extension for weather line on RealTimeSafetyReport
private val com.kipita.ai.RealTimeSafetyReport.weatherLine: String
    get() = buildString {
        if (weatherTemperature.isNotBlank()) append(weatherTemperature)
        if (weatherCondition.isNotBlank()) { if (isNotEmpty()) append(" · "); append(weatherCondition) }
    }
