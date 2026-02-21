package com.kipita.presentation.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.api.PlaceCategory
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.NearbyPlace
import com.kipita.data.repository.YelpPlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val yelpRepository: YelpPlacesRepository,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {

    private val _state = MutableStateFlow(PlacesUiState())
    val state: StateFlow<PlacesUiState> = _state.asStateFlow()

    init {
        // Default location: Tokyo (35.6762, 139.6503)
        // In production: use FusedLocationProviderClient to get device GPS
        loadCategory(PlaceCategory.HOTELS, latitude = 35.6762, longitude = 139.6503)
    }

    fun selectCategory(category: PlaceCategory) {
        _state.value = _state.value.copy(selectedCategory = category)
        val lat = _state.value.currentLat
        val lon = _state.value.currentLon
        loadCategory(category, lat, lon)
    }

    fun updateLocation(lat: Double, lon: Double) {
        val category = _state.value.selectedCategory
        _state.value = _state.value.copy(currentLat = lat, currentLon = lon)
        loadCategory(category, lat, lon)
    }

    fun searchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    private fun loadCategory(category: PlaceCategory, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                yelpRepository.fetchCategory(latitude, longitude, category)
            }.onSuccess { places ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    places = places,
                    selectedCategory = category
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Could not load places — check your connection"
                )
                errorLogger.log("PlacesViewModel.loadCategory", e)
            }
        }
    }
}

data class PlacesUiState(
    val selectedCategory: PlaceCategory = PlaceCategory.HOTELS,
    val places: List<NearbyPlace> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val currentLat: Double = 35.6762,
    val currentLon: Double = 139.6503
) {
    val filteredPlaces: List<NearbyPlace>
        get() = if (searchQuery.isBlank()) places
        else places.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
        }
}
