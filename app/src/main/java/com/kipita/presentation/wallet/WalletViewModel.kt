package com.kipita.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.api.WalletApiService
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletApiService: WalletApiService,
    private val currencyRepository: CurrencyRepository,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {

    private val _state = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = _state.asStateFlow()

    init {
        loadAvailableCurrencies()
    }

    private fun loadAvailableCurrencies() {
        viewModelScope.launch {
            runCatching { currencyRepository.getAvailableCurrencies() }
                .onSuccess { currencies ->
                    _state.value = _state.value.copy(availableCurrencies = currencies)
                }
                .onFailure { errorLogger.log("WalletViewModel.loadCurrencies", it) }
        }
    }

    fun refreshBalances(coinbaseToken: String, cashAppToken: String) {
        viewModelScope.launch {
            runCatching {
                val coinbase = walletApiService.coinbaseBalance("Bearer $coinbaseToken")
                val cashApp = walletApiService.cashAppBalance("Bearer $cashAppToken")
                _state.value.copy(
                    coinbaseBalance = coinbase.btcBalance,
                    cashAppBalance = cashApp.btcBalance
                )
            }.onSuccess { _state.value = it }
                .onFailure { errorLogger.log("WalletViewModel.refreshBalances", it) }
        }
    }

    fun convert(amount: Double, from: String, to: String) {
        if (amount <= 0) return
        viewModelScope.launch {
            _state.value = _state.value.copy(converting = true, error = null)
            runCatching { currencyRepository.convert(amount, from, to) }
                .onSuccess { conversion ->
                    _state.value = _state.value.copy(
                        converting = false,
                        conversionRate = conversion.rate,
                        conversionValue = conversion.convertedAmount,
                        conversionLabel = "${conversion.from}→${conversion.to}",
                        lastUpdated = conversion.timestamp.toString().take(10)
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(converting = false, error = "Rate unavailable — check your connection")
                    errorLogger.log("WalletViewModel.convert", it)
                }
        }
    }
}

data class WalletUiState(
    val coinbaseBalance: Double = 0.0,
    val cashAppBalance: Double = 0.0,
    val conversionRate: Double? = null,
    val conversionValue: Double? = null,
    val conversionLabel: String = "",
    val lastUpdated: String = "",
    val converting: Boolean = false,
    val error: String? = null,
    val availableCurrencies: Map<String, String> = emptyMap()
)
