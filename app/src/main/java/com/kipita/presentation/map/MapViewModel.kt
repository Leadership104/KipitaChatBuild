package com.kipita.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.api.PlaceCategory
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.GooglePlacesRepository
import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NearbyPlace
import com.kipita.data.repository.NomadRepository
import com.kipita.data.repository.OfflineMapRepository
import com.kipita.domain.model.MerchantLocation
import com.kipita.domain.model.NomadPlaceInfo
import com.kipita.domain.model.TravelNotice
import com.kipita.domain.usecase.TravelDataEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(
    private val travelDataEngine: TravelDataEngine,
    private val merchantRepository: MerchantRepository,
    private val nomadRepository: NomadRepository,
    private val offlineMapRepository: OfflineMapRepository,
    private val googlePlacesRepository: GooglePlacesRepository,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun load(region: String, userLat: Double = 0.0, userLng: Double = 0.0) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, userLat = userLat, userLng = userLng)
            runCatching {
                coroutineScope {
                    val noticesD  = async { travelDataEngine.collectRegionNotices(region) }
                    val merchantsD = async { merchantRepository.refresh(null, userLat, userLng) }
                    val nomadD    = async { nomadRepository.refresh() }
                    val offlineD  = async { offlineMapRepository.isRegionAvailableOffline(region) }
                    val foodD = async {
                        runCatching {
                            googlePlacesRepository.fetchCategory(userLat, userLng, PlaceCategory.RESTAURANTS)
                        }.getOrElse { emptyList() }
                    }
                    val cafeD = async {
                        runCatching {
                            googlePlacesRepository.fetchCategory(userLat, userLng, PlaceCategory.CAFES)
                        }.getOrElse { emptyList() }
                    }
                    val shopD = async {
                        runCatching {
                            googlePlacesRepository.fetchCategory(userLat, userLng, PlaceCategory.SHOPPING)
                        }.getOrElse { emptyList() }
                    }

                    MapUiState(
                        loading           = false,
                        notices           = noticesD.await(),
                        merchants         = merchantsD.await(),
                        nomadPlaces       = nomadD.await(),
                        nearbyFoodPlaces  = foodD.await(),
                        nearbyCafePlaces  = cafeD.await(),
                        nearbyShopPlaces  = shopD.await(),
                        activeOverlays    = _state.value.activeOverlays,
                        offlineReady      = offlineD.await(),
                        userLat           = userLat,
                        userLng           = userLng
                    )
                }
            }.onSuccess { _state.value = it }
                .onFailure {
                    _state.value = _state.value.copy(loading = false)
                    errorLogger.log("MapViewModel.load", it)
                }
        }
    }

    fun cacheRegionOffline(region: String) {
        viewModelScope.launch {
            runCatching { offlineMapRepository.cacheRegion(region) }
                .onSuccess { _state.value = _state.value.copy(offlineReady = true) }
                .onFailure { errorLogger.log("MapViewModel.cacheRegionOffline", it) }
        }
    }

    fun toggleOverlay(overlay: OverlayType) {
        _state.value = _state.value.copy(
            activeOverlays = _state.value.activeOverlays.toMutableSet().apply {
                if (contains(overlay)) remove(overlay) else add(overlay)
            }
        )
    }
}

enum class OverlayType { BTC_MERCHANTS, SAFETY, HEALTH, INFRASTRUCTURE, NOMAD }

data class MapUiState(
    val loading: Boolean = false,
    val notices: List<TravelNotice> = emptyList(),
    val merchants: List<MerchantLocation> = emptyList(),
    val nomadPlaces: List<NomadPlaceInfo> = emptyList(),
    val nearbyFoodPlaces: List<NearbyPlace> = emptyList(),
    val nearbyCafePlaces: List<NearbyPlace> = emptyList(),
    val nearbyShopPlaces: List<NearbyPlace> = emptyList(),
    val activeOverlays: Set<OverlayType> = setOf(OverlayType.BTC_MERCHANTS, OverlayType.SAFETY, OverlayType.HEALTH, OverlayType.NOMAD),
    val offlineReady: Boolean = false,
    val userLat: Double = 0.0,
    val userLng: Double = 0.0
)
