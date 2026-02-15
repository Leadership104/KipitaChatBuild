package com.kipita.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.domain.usecase.AiInsight
import com.kipita.domain.usecase.AiOrchestrationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiUseCase: AiOrchestrationUseCase
) : ViewModel() {
    private val _insight = MutableStateFlow<AiInsight?>(null)
    val insight: StateFlow<AiInsight?> = _insight.asStateFlow()

    fun analyze(region: String) {
        viewModelScope.launch { _insight.value = aiUseCase.assessRegion(region) }
    }
}
