package com.kipita.data.service

import com.kipita.data.api.PlaceCategory
import com.kipita.data.repository.BitcoinPriceRepository
import com.kipita.data.repository.CryptoWalletRepository
import com.kipita.data.repository.YelpPlacesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// StartupDataAggregator
//
// Middleware service that concurrently fetches on app startup:
//   1. Financial balances — Coinbase, Gemini, River  (parallel via CryptoWalletRepository)
//   2. Live crypto prices  — CoinGecko free endpoint (no API key required)
//   3. Local Yelp POIs     — for the detected / default user location
//
// All three streams run simultaneously via coroutineScope + async so the
// slowest source doesn't block the faster ones. Each stream uses a
// SupervisorJob-style catch so one failure doesn't cancel the others.
//
// Zero-Persistence guarantee: financial balance data returned from stream 1
// is held only in the CryptoWalletRepository in-memory cache and never
// written to SQLite, SharedPreferences, or any other persistent storage.
// ---------------------------------------------------------------------------

@Singleton
class StartupDataAggregator @Inject constructor(
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val yelpPlacesRepository: YelpPlacesRepository,
    private val bitcoinPriceRepository: BitcoinPriceRepository
) {

    sealed class AggregationState {
        object Idle : AggregationState()
        object Loading : AggregationState()
        data class Ready(
            val walletSynced: Boolean,
            val pricesSynced: Boolean,
            val poisLoaded: Boolean,
            val errors: List<String> = emptyList()
        ) : AggregationState()
    }

    private val _state = MutableStateFlow<AggregationState>(AggregationState.Idle)
    val state: StateFlow<AggregationState> = _state.asStateFlow()

    // Application-scoped scope — survives screen rotation, outlives ViewModels
    private val aggregatorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Launch the concurrent startup fetch. Safe to call on every cold start —
     * each individual source handles its own cache logic and auth fallbacks.
     *
     * @param userLat  User latitude  (defaults to Tokyo if GPS not yet granted)
     * @param userLng  User longitude (defaults to Tokyo if GPS not yet granted)
     */
    fun launchStartupFetch(
        userLat: Double = DEFAULT_LAT,
        userLng: Double = DEFAULT_LNG
    ) {
        if (_state.value is AggregationState.Loading) return   // deduplicate concurrent calls
        aggregatorScope.launch {
            _state.value = AggregationState.Loading
            val errors = mutableListOf<String>()
            var walletOk = false
            var pricesOk = false
            var poisOk = false

            coroutineScope {
                // Stream 1: Financial balances (Coinbase + Gemini + River in parallel internally)
                val walletJob = async {
                    runCatching { cryptoWalletRepository.getAggregatedWallet(forceRefresh = true) }
                        .onSuccess { walletOk = true }
                        .onFailure { errors += "Wallets: ${it.message?.take(60)}" }
                }

                // Stream 2: Live BTC / ETH / SOL prices (CoinGecko — no key needed)
                val pricesJob = async {
                    runCatching { bitcoinPriceRepository.getPrices(forceRefresh = true) }
                        .onSuccess { pricesOk = true }
                        .onFailure { errors += "Prices: ${it.message?.take(60)}" }
                }

                // Stream 3: Yelp local POIs — restaurants + hotels for the user area
                val poisJob = async {
                    runCatching {
                        yelpPlacesRepository.fetchCategory(userLat, userLng, PlaceCategory.RESTAURANTS)
                        yelpPlacesRepository.fetchCategory(userLat, userLng, PlaceCategory.HOTELS)
                    }.onSuccess { poisOk = true }
                    // Silently skipped when Yelp API key is not yet configured
                }

                walletJob.await()
                pricesJob.await()
                poisJob.await()
            }

            _state.value = AggregationState.Ready(walletOk, pricesOk, poisOk, errors)
        }
    }

    /**
     * Called when GPS lock is acquired after initial startup.
     * Re-fetches key Yelp POI categories for the new location.
     */
    fun onLocationAcquired(lat: Double, lng: Double) {
        aggregatorScope.launch {
            runCatching {
                yelpPlacesRepository.fetchCategory(lat, lng, PlaceCategory.RESTAURANTS)
                yelpPlacesRepository.fetchCategory(lat, lng, PlaceCategory.HOTELS)
                yelpPlacesRepository.fetchCategory(lat, lng, PlaceCategory.TRANSPORT)
            }
        }
    }

    companion object {
        // Default: Tokyo — most nomad-friendly city in the Nomad List index
        const val DEFAULT_LAT = 35.6762
        const val DEFAULT_LNG = 139.6503
    }
}
