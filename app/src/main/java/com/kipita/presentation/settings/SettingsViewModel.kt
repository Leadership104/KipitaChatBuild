package com.kipita.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.ErrorLogEntity
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
    private val keystoreManager: KeystoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        refreshKeyStatus()
    }

    // -----------------------------------------------------------------------
    // API key status checks
    // -----------------------------------------------------------------------

    fun refreshKeyStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(
                hasYelpKey       = keystoreManager.hasKey(KeystoreManager.YELP_API_KEY_ALIAS),
                hasCoinbaseToken = keystoreManager.hasKey(KeystoreManager.COINBASE_OAUTH_TOKEN_ALIAS),
                hasGeminiKey     = keystoreManager.hasKey(KeystoreManager.GEMINI_API_KEY_ALIAS),
                hasGeminiSecret  = keystoreManager.hasKey(KeystoreManager.GEMINI_API_SECRET_ALIAS),
                hasRiverToken    = keystoreManager.hasKey(KeystoreManager.RIVER_OAUTH_TOKEN_ALIAS),
                hasCashAppToken  = keystoreManager.hasKey(KeystoreManager.CASHAPP_OAUTH_TOKEN_ALIAS)
            )
        }
    }

    // -----------------------------------------------------------------------
    // Save keys (each value AES-GCM encrypted in hardware-backed KeyStore)
    // -----------------------------------------------------------------------

    fun saveYelpApiKey(key: String) = saveKey(KeystoreManager.YELP_API_KEY_ALIAS, key,
        onDone = { _state.value = _state.value.copy(hasYelpKey = key.isNotBlank(), saveStatus = "Yelp key saved") })

    fun saveCoinbaseToken(token: String) = saveKey(KeystoreManager.COINBASE_OAUTH_TOKEN_ALIAS, token,
        onDone = { _state.value = _state.value.copy(hasCoinbaseToken = token.isNotBlank(), saveStatus = "Coinbase token saved") })

    fun saveGeminiApiKey(key: String) = saveKey(KeystoreManager.GEMINI_API_KEY_ALIAS, key,
        onDone = { _state.value = _state.value.copy(hasGeminiKey = key.isNotBlank(), saveStatus = "Gemini API key saved") })

    fun saveGeminiApiSecret(secret: String) = saveKey(KeystoreManager.GEMINI_API_SECRET_ALIAS, secret,
        onDone = { _state.value = _state.value.copy(hasGeminiSecret = secret.isNotBlank(), saveStatus = "Gemini secret saved") })

    fun saveRiverToken(token: String) = saveKey(KeystoreManager.RIVER_OAUTH_TOKEN_ALIAS, token,
        onDone = { _state.value = _state.value.copy(hasRiverToken = token.isNotBlank(), saveStatus = "River token saved") })

    fun saveCashAppToken(token: String) = saveKey(KeystoreManager.CASHAPP_OAUTH_TOKEN_ALIAS, token,
        onDone = { _state.value = _state.value.copy(hasCashAppToken = token.isNotBlank(), saveStatus = "CashApp token saved") })

    // -----------------------------------------------------------------------
    // Clear individual keys
    // -----------------------------------------------------------------------

    fun clearYelpApiKey() = clearKey(KeystoreManager.YELP_API_KEY_ALIAS,
        onDone = { _state.value = _state.value.copy(hasYelpKey = false, saveStatus = "Yelp key removed") })

    fun clearCoinbaseToken() = clearKey(KeystoreManager.COINBASE_OAUTH_TOKEN_ALIAS,
        onDone = { _state.value = _state.value.copy(hasCoinbaseToken = false, saveStatus = "Coinbase token removed") })

    fun clearGeminiKeys() {
        clearKey(KeystoreManager.GEMINI_API_KEY_ALIAS) {}
        clearKey(KeystoreManager.GEMINI_API_SECRET_ALIAS,
            onDone = { _state.value = _state.value.copy(hasGeminiKey = false, hasGeminiSecret = false, saveStatus = "Gemini keys removed") })
    }

    fun clearRiverToken() = clearKey(KeystoreManager.RIVER_OAUTH_TOKEN_ALIAS,
        onDone = { _state.value = _state.value.copy(hasRiverToken = false, saveStatus = "River token removed") })

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
    // Internal helpers
    // -----------------------------------------------------------------------

    private fun saveKey(alias: String, value: String, onDone: () -> Unit) {
        if (value.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { keystoreManager.storeApiKey(alias, value.trim()) }
            onDone()
        }
    }

    private fun clearKey(alias: String, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { keystoreManager.deleteKey(alias) }
            onDone()
        }
    }
}

data class SettingsUiState(
    // API key / token presence flags (true = stored in KeyStore)
    val hasYelpKey: Boolean       = false,
    val hasCoinbaseToken: Boolean = false,
    val hasGeminiKey: Boolean     = false,
    val hasGeminiSecret: Boolean  = false,
    val hasRiverToken: Boolean    = false,
    val hasCashAppToken: Boolean  = false,
    // UI feedback
    val saveStatus: String = "",
    // Error log
    val logs: List<ErrorLogEntity> = emptyList(),
    val lastFlushStatus: String = ""
)
