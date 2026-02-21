package com.kipita.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.api.PlaceCategory
import com.kipita.data.repository.NearbyPlace
import com.kipita.data.repository.YelpPlacesRepository
import com.kipita.data.service.TransitDeepLinkService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// ExploreViewModel
//
// Powers the Explore screen's Places tab:
//   • Fetches Yelp results for the selected category + location
//   • Exposes transit deep-link helpers (Uber / Lyft) for place cards
//   • Tracks loading and error states without persisting anything to disk
// ---------------------------------------------------------------------------

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val yelpPlacesRepository: YelpPlacesRepository,
    val transitService: TransitDeepLinkService
) : ViewModel() {

    data class ExploreUiState(
        val places: List<NearbyPlace> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null,
        // User's current lat/lng (updated when GPS is acquired)
        val userLat: Double = 35.6762,
        val userLng: Double = 139.6503,
        val uberInstalled: Boolean = false,
        val lyftInstalled: Boolean = false
    )

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(
            uberInstalled = transitService.isUberInstalled(),
            lyftInstalled = transitService.isLyftInstalled()
        )
    }

    // -----------------------------------------------------------------------
    // Location updates from GPS
    // -----------------------------------------------------------------------

    fun updateUserLocation(lat: Double, lng: Double) {
        _state.value = _state.value.copy(userLat = lat, userLng = lng)
    }

    // -----------------------------------------------------------------------
    // Yelp category fetching
    // -----------------------------------------------------------------------

    /**
     * Fetch places by GPS coordinates + category.
     * Called when GPS is available or when category chip is tapped.
     */
    fun fetchByCoordinates(
        category: PlaceCategory,
        lat: Double = _state.value.userLat,
        lng: Double = _state.value.userLng
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                yelpPlacesRepository.fetchCategory(lat, lng, category)
            }.onSuccess { places ->
                _state.value = _state.value.copy(loading = false, places = places)
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message)
            }
        }
    }

    /**
     * Fetch places by typed location string + category.
     * Called when user types a city/address/country in the search bar.
     */
    fun fetchByLocation(locationString: String, category: PlaceCategory) {
        if (locationString.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                yelpPlacesRepository.fetchCategoryByLocation(locationString, category)
            }.onSuccess { places ->
                _state.value = _state.value.copy(loading = false, places = places)
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message)
            }
        }
    }

    // -----------------------------------------------------------------------
    // Transit deep-link actions — called directly from place card buttons
    // -----------------------------------------------------------------------

    /**
     * Request an Uber ride to a Yelp place.
     * Passes user's GPS origin + place's coordinates as destination.
     */
    fun bookUberToPlace(place: NearbyPlace) {
        val destLat = place.latitude ?: return
        val destLng = place.longitude ?: return
        transitService.requestUberRide(
            originLat = _state.value.userLat,
            originLng = _state.value.userLng,
            destLat = destLat,
            destLng = destLng,
            destName = place.name
        )
    }

    /**
     * Request a Lyft ride to a Yelp place.
     * Passes user's GPS origin + place's coordinates as destination.
     */
    fun bookLyftToPlace(place: NearbyPlace) {
        val destLat = place.latitude ?: return
        val destLng = place.longitude ?: return
        transitService.requestLyftRide(
            originLat = _state.value.userLat,
            originLng = _state.value.userLng,
            destLat = destLat,
            destLng = destLng,
            destName = place.name
        )
    }
}
