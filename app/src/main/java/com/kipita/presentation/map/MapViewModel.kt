package com.kipita.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val travelDataEngine: TravelDataEngine
) : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun load(region: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val notices = travelDataEngine.collectRegionNotices(region)
            _state.value = _state.value.copy(loading = false, notices = notices)
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

enum class OverlayType { BTC_MERCHANTS, SAFETY, HEALTH, INFRASTRUCTURE }

data class MapUiState(
    val loading: Boolean = false,
    val notices: List<TravelNotice> = emptyList(),
    val activeOverlays: Set<OverlayType> = setOf(OverlayType.SAFETY, OverlayType.HEALTH)
)
