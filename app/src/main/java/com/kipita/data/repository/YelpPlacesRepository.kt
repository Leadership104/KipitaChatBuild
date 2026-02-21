package com.kipita.data.repository

import com.kipita.data.api.PlaceCategory
import com.kipita.data.api.YelpApiService
import com.kipita.data.api.YelpBusinessDto
import com.kipita.data.security.KeystoreManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// YelpPlacesRepository
//
// Fetches real-time local business data from Yelp Fusion API.
// • open_now=true ensures only currently-accessible places are shown.
// • Category aliases from PlaceCategory enum match the SOW 9-grid spec.
// • Results are used for the Map "Nearby Places" bottom sheet.
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
class YelpPlacesRepository @Inject constructor(
    private val yelpApi: YelpApiService,
    private val keystoreManager: KeystoreManager
) {
    // In-memory cache: category → list of nearby places, plus timestamp
    private val cache = mutableMapOf<String, Pair<Long, List<NearbyPlace>>>()
    private val cacheMaxAgeMs = 15 * 60 * 1000L // 15 minutes for real-time feel

    /**
     * Load all 9 grid categories in parallel, returning a map of
     * PlaceCategory → list of nearby places.
     */
    suspend fun loadAllCategories(
        latitude: Double,
        longitude: Double
    ): Map<PlaceCategory, List<NearbyPlace>> = coroutineScope {
        val apiKey = keystoreManager.getApiKey(KeystoreManager.YELP_API_KEY_ALIAS)
            ?: return@coroutineScope emptyMap()
        val bearer = "Bearer $apiKey"

        PlaceCategory.entries.map { category ->
            async {
                category to fetchCategory(bearer, latitude, longitude, category)
            }
        }.awaitAll().toMap()
    }

    /**
     * Fetch a single category, using in-memory cache if fresh.
     */
    suspend fun fetchCategory(
        latitude: Double,
        longitude: Double,
        category: PlaceCategory
    ): List<NearbyPlace> {
        val apiKey = keystoreManager.getApiKey(KeystoreManager.YELP_API_KEY_ALIAS) ?: return emptyList()
        return fetchCategory("Bearer $apiKey", latitude, longitude, category)
    }

    private suspend fun fetchCategory(
        bearer: String,
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
            val response = yelpApi.searchBusinesses(
                bearerToken = bearer,
                latitude = latitude,
                longitude = longitude,
                categoryAlias = category.yelpAlias,
                openNow = true,
                attributes = category.attributes
            )
            val places = response.businesses.map { it.toNearbyPlace(category) }
            cache[cacheKey] = Pair(System.currentTimeMillis(), places)
            places
        } catch (e: Exception) {
            cache[cacheKey]?.second ?: emptyList()
        }
    }

    private fun YelpBusinessDto.toNearbyPlace(category: PlaceCategory) = NearbyPlace(
        id = id,
        name = name,
        category = category,
        emoji = category.emoji,
        address = location.displayAddress.joinToString(", "),
        distanceKm = distanceMeters / 1000.0,
        rating = rating,
        reviewCount = reviewCount,
        isOpen = !isClosed,
        latitude = coordinates?.latitude,
        longitude = coordinates?.longitude,
        phone = displayPhone
    )
}
