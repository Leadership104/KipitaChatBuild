package com.kipita.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.domain.usecase.AiOrchestrator
import com.kipita.domain.usecase.OrchestratedAssistantResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiOrchestrator: AiOrchestrator,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {
    private val _response = MutableStateFlow<OrchestratedAssistantResponse?>(null)
    val response: StateFlow<OrchestratedAssistantResponse?> = _response.asStateFlow()

    fun analyze(region: String, prompt: String) {
        viewModelScope.launch {
            runCatching { aiOrchestrator.handleIntent(prompt, region) }
                .onSuccess { _response.value = it }
                .onFailure { errorLogger.log("AiViewModel.analyze", it) }
        }
    }
}
