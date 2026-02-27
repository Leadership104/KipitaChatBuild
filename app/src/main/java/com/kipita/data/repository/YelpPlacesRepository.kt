package com.kipita.data.repository

import com.kipita.BuildConfig
import com.kipita.data.api.GooglePlaceDto
import com.kipita.data.api.GooglePlacesApiService
import com.kipita.data.api.LocationRestriction
import com.kipita.data.api.NearbySearchRequest
import com.kipita.data.api.PLACES_LIST_FIELD_MASK
import com.kipita.data.api.PlaceCategory
import com.kipita.data.api.PlaceCircle
import com.kipita.data.api.PlaceLatLng
import com.kipita.data.api.TextSearchRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// GooglePlacesRepository
//
// Fetches real-time local business data from Google Places API (New) v1.
// • Replaces the former Yelp Fusion integration.
// • API key sourced from BuildConfig.GOOGLE_PLACES_API_KEY (local.properties).
// • Results are cached in-memory for 15 minutes for real-time feel.
// • Used by the Map "Nearby Places" bottom sheet + Explore tab category grid.
// ---------------------------------------------------------------------------

data class NearbyPlace(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val emoji: String,
    val address: String,
    val distanceKm: Double,
    val rating: Double,
    val reviewCount: Int,
    val isOpen: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val phone: String
)

@Singleton
class GooglePlacesRepository @Inject constructor(
    private val placesApi: GooglePlacesApiService
) {
    // In-memory cache: cacheKey → (timestamp, results)
    private val cache = mutableMapOf<String, Pair<Long, List<NearbyPlace>>>()
    private val cacheMaxAgeMs = 15 * 60 * 1000L // 15 minutes

    private val apiKey get() = BuildConfig.GOOGLE_PLACES_API_KEY

    /**
     * Load all category grid entries in parallel, returning a map of
     * PlaceCategory → list of nearby places.
     */
    suspend fun loadAllCategories(
        latitude: Double,
        longitude: Double
    ): Map<PlaceCategory, List<NearbyPlace>> = coroutineScope {
        PlaceCategory.entries.map { category ->
            async { category to fetchCategory(latitude, longitude, category) }
        }.awaitAll().toMap()
    }

    /**
     * Fetch a single category by GPS coordinates.
     * Called when GPS is available or when category chip is tapped.
     */
    suspend fun fetchCategory(
        latitude: Double,
        longitude: Double,
        category: PlaceCategory
    ): List<NearbyPlace> {
        val cacheKey = "${category.name}@${latitude.toLong()},${longitude.toLong()}"
        val cached = cache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < cacheMaxAgeMs) {
            return cached.second
        }
        return try {
            val response = placesApi.searchNearby(
                apiKey = apiKey,
                fieldMask = PLACES_LIST_FIELD_MASK,
                request = NearbySearchRequest(
                    includedTypes = category.googleTypes,
                    maxResultCount = 20,
                    locationRestriction = LocationRestriction(
                        circle = PlaceCircle(
                            center = PlaceLatLng(latitude, longitude),
                            radius = 1000.0
                        )
                    )
                )
            )
            val places = response.places.map { it.toNearbyPlace(category) }
            cache[cacheKey] = Pair(System.currentTimeMillis(), places)
            places
        } catch (e: Exception) {
            cache[cacheKey]?.second ?: emptyList()
        }
    }

    /**
     * Fetch a single category by location string (city, address, etc.)
     * Used when user types a location manually in the search bar.
     */
    suspend fun fetchCategoryByLocation(
        locationString: String,
        category: PlaceCategory
    ): List<NearbyPlace> {
        val cacheKey = "${category.name}@loc:${locationString.lowercase().trim()}"
        val cached = cache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < cacheMaxAgeMs) {
            return cached.second
        }
        return try {
            val response = placesApi.searchByText(
                apiKey = apiKey,
                fieldMask = PLACES_LIST_FIELD_MASK,
                request = TextSearchRequest(
                    textQuery = "${category.label} in $locationString",
                    maxResultCount = 20,
                    openNow = false
                )
            )
            val places = response.places.map { it.toNearbyPlace(category) }
            cache[cacheKey] = Pair(System.currentTimeMillis(), places)
            places
        } catch (e: Exception) {
            cache[cacheKey]?.second ?: emptyList()
        }
    }

    private fun GooglePlaceDto.toNearbyPlace(category: PlaceCategory) = NearbyPlace(
        id = id,
        name = displayName?.text ?: "",
        category = category,
        emoji = category.emoji,
        address = formattedAddress,
        distanceKm = 0.0,
        rating = rating,
        reviewCount = 0,
        isOpen = currentOpeningHours?.openNow ?: false,
        latitude = location?.latitude,
        longitude = location?.longitude,
        phone = nationalPhoneNumber
    )
}

