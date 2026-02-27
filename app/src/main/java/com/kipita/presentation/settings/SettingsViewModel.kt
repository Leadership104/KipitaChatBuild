package com.kipita.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.ErrorLogEntity
import com.kipita.data.repository.AccountRepository
import com.kipita.data.security.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val errorLogger: InHouseErrorLogger,
    private val keystoreManager: KeystoreManager,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    // -----------------------------------------------------------------------
    // Error logs
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // Account deletion (GDPR / CCPA) — clears all local data
    // -----------------------------------------------------------------------

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.deleteAccount()
            // Wipe any cached keys from hardware-backed KeyStore (GDPR compliance)
            listOf(
                KeystoreManager.GOOGLE_PLACES_API_KEY_ALIAS,
                KeystoreManager.COINBASE_OAUTH_TOKEN_ALIAS,
                KeystoreManager.GEMINI_API_KEY_ALIAS,
                KeystoreManager.GEMINI_API_SECRET_ALIAS,
                KeystoreManager.RIVER_OAUTH_TOKEN_ALIAS,
                KeystoreManager.CASHAPP_OAUTH_TOKEN_ALIAS
            ).forEach { runCatching { keystoreManager.deleteKey(it) } }
            withContext(Dispatchers.Main) { onDeleted() }
        }
    }
}

data class SettingsUiState(
    val logs: List<ErrorLogEntity> = emptyList(),
    val lastFlushStatus: String = ""
)
