package com.kipita.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.MerchantRepository
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
                MapUiState(
                    loading = false,
                    notices = notices,
                    merchants = merchants,
                    nomadPlaces = nomadPlaces,
                    activeOverlays = _state.value.activeOverlays,
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
    val activeOverlays: Set<OverlayType> = setOf(OverlayType.BTC_MERCHANTS, OverlayType.SAFETY, OverlayType.HEALTH, OverlayType.NOMAD),
    val offlineReady: Boolean = false
)
