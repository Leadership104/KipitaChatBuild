package com.kipita.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.ai.KipitaAIManager
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
}
