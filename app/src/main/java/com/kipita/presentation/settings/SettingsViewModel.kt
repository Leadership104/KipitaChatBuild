package com.kipita.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.ErrorLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun refreshLogs() {
        viewModelScope.launch {
            val logs = withContext(Dispatchers.IO) { errorLogger.allLogs() }
            _state.value = _state.value.copy(logs = logs)
        }
    }

    fun flushLogs() {
        viewModelScope.launch {
            runCatching { errorLogger.flushUnsent() }
                .onSuccess { _state.value = _state.value.copy(lastFlushStatus = "Log sync attempted") }
                .onFailure { _state.value = _state.value.copy(lastFlushStatus = "Log sync failed: ${it.message}") }
            refreshLogs()
        }
    }
}

data class SettingsUiState(
    val logs: List<ErrorLogEntity> = emptyList(),
    val lastFlushStatus: String = ""
)
