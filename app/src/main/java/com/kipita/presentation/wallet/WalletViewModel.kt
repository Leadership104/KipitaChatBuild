package com.kipita.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.api.WalletApiService
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.AggregatedWallet
import com.kipita.data.repository.CryptoWalletRepository
import com.kipita.data.repository.CurrencyRepository
import com.kipita.data.repository.WalletBalance
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
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {

    private val _state = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = _state.asStateFlow()

    init {
        loadAvailableCurrencies()
        loadCryptoWallets()
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

    fun loadCryptoWallets(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncingWallets = true, walletError = null)
            runCatching { cryptoWalletRepository.getAggregatedWallet(forceRefresh) }
                .onSuccess { wallet ->
                    _state.value = _state.value.copy(
                        syncingWallets = false,
                        aggregatedWallet = wallet,
                        totalWalletUsd = wallet.totalUsd,
                        // Legacy fields for animated balance counter
                        coinbaseBalance = wallet.wallets
                            .filter { it.source.name == "COINBASE" }
                            .sumOf { it.balance },
                        cashAppBalance = wallet.wallets
                            .filter { it.source.name == "RIVER" }
                            .sumOf { it.balance }
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(syncingWallets = false, walletError = "Could not sync wallets")
                    errorLogger.log("WalletViewModel.loadCryptoWallets", it)
                }
        }
    }

    // Legacy method kept for compatibility with any existing calls
    fun refreshBalances(coinbaseToken: String, cashAppToken: String) {
        loadCryptoWallets(forceRefresh = true)
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
    // Aggregated crypto wallet
    val aggregatedWallet: AggregatedWallet? = null,
    val totalWalletUsd: Double = 0.0,
    val syncingWallets: Boolean = false,
    val walletError: String? = null,
    // Legacy balance fields (for animated counter)
    val coinbaseBalance: Double = 0.0,
    val cashAppBalance: Double = 0.0,
    // Currency converter
    val conversionRate: Double? = null,
    val conversionValue: Double? = null,
    val conversionLabel: String = "",
    val lastUpdated: String = "",
    val converting: Boolean = false,
    val error: String? = null,
    val availableCurrencies: Map<String, String> = emptyMap()
)
