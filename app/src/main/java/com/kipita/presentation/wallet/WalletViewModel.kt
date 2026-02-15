package com.kipita.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.api.WalletApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletApiService: WalletApiService
) : ViewModel() {

    private val _state = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = _state.asStateFlow()

    fun refreshBalances(coinbaseToken: String, cashAppToken: String) {
        viewModelScope.launch {
            val coinbase = runCatching { walletApiService.coinbaseBalance("Bearer $coinbaseToken") }.getOrNull()
            val cashApp = runCatching { walletApiService.cashAppBalance("Bearer $cashAppToken") }.getOrNull()
            _state.value = WalletUiState(
                coinbaseBalance = coinbase?.btcBalance ?: 0.0,
                cashAppBalance = cashApp?.btcBalance ?: 0.0
            )
        }
    }
}

data class WalletUiState(
    val coinbaseBalance: Double = 0.0,
    val cashAppBalance: Double = 0.0
)
