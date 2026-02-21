package com.kipita.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.api.WalletApiService
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.AggregatedWallet
import com.kipita.data.repository.BitcoinPriceRepository
import com.kipita.data.repository.CryptoPrices
import com.kipita.data.repository.CryptoWalletRepository
import com.kipita.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletApiService: WalletApiService,
    private val currencyRepository: CurrencyRepository,
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val bitcoinPriceRepository: BitcoinPriceRepository,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {

    private val _state = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = _state.asStateFlow()

    init {
        loadAvailableCurrencies()
        loadCryptoWallets()
        startPricePoll()
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

    /** Poll BTC/ETH/SOL prices every 30 seconds from CoinGecko */
    private fun startPricePoll() {
        viewModelScope.launch {
            while (isActive) {
                runCatching { bitcoinPriceRepository.getPrices() }
                    .onSuccess { prices ->
                        _state.value = _state.value.copy(cryptoPrices = prices)
                    }
                    .onFailure { errorLogger.log("WalletViewModel.pricePoll", it) }
                delay(30_000L)
            }
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

    fun refreshPrices() {
        viewModelScope.launch {
            runCatching { bitcoinPriceRepository.getPrices(forceRefresh = true) }
                .onSuccess { prices -> _state.value = _state.value.copy(cryptoPrices = prices) }
                .onFailure { errorLogger.log("WalletViewModel.refreshPrices", it) }
        }
    }

    fun refreshBalances(coinbaseToken: String, cashAppToken: String) {
        loadCryptoWallets(forceRefresh = true)
        refreshPrices()
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
    // Live crypto prices (CoinGecko)
    val cryptoPrices: CryptoPrices? = null,
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
