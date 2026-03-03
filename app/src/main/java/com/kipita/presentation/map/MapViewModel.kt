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
    private val placesRepository: GooglePlacesRepository,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun load(region: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            runCatching {
                val notices = travelDataEngine.collectRegionNotices(region)
                val merchants = merchantRepository.refresh(cashAppToken = null)
                val nomadPlaces = nomadRepository.refresh()
                val isOfflineReady = offlineMapRepository.isRegionAvailableOffline(region)
                val places = placesRepository.fetchCategory(
                    latitude = _state.value.currentLat,
                    longitude = _state.value.currentLon,
                    category = _state.value.selectedMarkerType.category
                )
                _state.value.copy(
                    loading = false,
                    notices = notices,
                    merchants = merchants,
                    nomadPlaces = nomadPlaces,
                    places = places,
                    offlineReady = isOfflineReady
                )
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

    fun updateLocation(lat: Double, lon: Double) {
        _state.value = _state.value.copy(currentLat = lat, currentLon = lon)
        load("global")
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun applySearchCenter(label: String, lat: Double, lon: Double) {
        _state.value = _state.value.copy(searchQuery = label, currentLat = lat, currentLon = lon)
        load("global")
    }

    fun toggleOverlay(overlay: OverlayType) {
        _state.value = _state.value.copy(
            activeOverlays = _state.value.activeOverlays.toMutableSet().apply {
                if (contains(overlay)) remove(overlay) else add(overlay)
            }
        )
    }

    fun setBtcSourceFilter(filter: BtcSourceFilter) {
        _state.value = _state.value.copy(btcSourceFilter = filter)
    }

    fun setMarkerType(markerType: MarkerType) {
        _state.value = _state.value.copy(selectedMarkerType = markerType)
        load("global")
    }

    fun toggleExpanded(placeId: String) {
        _state.value = _state.value.copy(expandedPlaceId = if (_state.value.expandedPlaceId == placeId) null else placeId)
    }

    fun showEmbeddedMapSearch(query: String) {
        _state.value = _state.value.copy(embeddedMapQuery = query)
    }
}

enum class OverlayType(val label: String) {
    BTC_MERCHANTS("BTC merchants"),
    SAFETY("Safety"),
    HEALTH("Health"),
    INFRASTRUCTURE("Infrastructure"),
    NOMAD("Nomad")
}

enum class BtcSourceFilter { BTCMAP, CASH_APP, BOTH }

enum class MarkerType(val label: String, val category: PlaceCategory) {
    FOOD("Food", PlaceCategory.RESTAURANTS),
    CAFE("Cafe", PlaceCategory.CAFES),
    SHOPS("Shops", PlaceCategory.SHOPPING)
}

data class MapUiState(
    val loading: Boolean = false,
    val notices: List<TravelNotice> = emptyList(),
    val merchants: List<MerchantLocation> = emptyList(),
    val nomadPlaces: List<NomadPlaceInfo> = emptyList(),
    val places: List<NearbyPlace> = emptyList(),
    val activeOverlays: Set<OverlayType> = setOf(OverlayType.BTC_MERCHANTS, OverlayType.SAFETY, OverlayType.HEALTH, OverlayType.NOMAD),
    val btcSourceFilter: BtcSourceFilter = BtcSourceFilter.BOTH,
    val selectedMarkerType: MarkerType = MarkerType.FOOD,
    val currentLat: Double = 35.6762,
    val currentLon: Double = 139.6503,
    val searchQuery: String = "",
    val expandedPlaceId: String? = null,
    val embeddedMapQuery: String = "",
    val offlineReady: Boolean = false
)
