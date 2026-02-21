package com.kipita.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// ---------------------------------------------------------------------------
// Yelp Fusion API — real-time Places + Advisory integration
// Endpoint: https://api.yelp.com/v3/
// Auth: Bearer token in Authorization header
// ---------------------------------------------------------------------------

interface YelpApiService {

    /**
     * Search businesses by category near a lat/lon. Used for the 9-grid
     * Places dashboard (police, hospitals, restaurants, EV, ATMs, etc.)
     */
    @GET("businesses/search")
    suspend fun searchBusinesses(
        @Header("Authorization") bearerToken: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("categories") categoryAlias: String,
        @Query("open_now") openNow: Boolean = true,
        @Query("limit") limit: Int = 20,
        @Query("sort_by") sortBy: String = "distance",
        @Query("attributes") attributes: String? = null
    ): YelpSearchResponse

    /**
     * Fetch full details (hours, phone, attributes) for a single business.
     */
    @GET("businesses/{id}")
    suspend fun getBusinessDetails(
        @Header("Authorization") bearerToken: String,
        @retrofit2.http.Path("id") businessId: String
    ): YelpBusinessDto
}

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class YelpSearchResponse(
    @Json(name = "businesses") val businesses: List<YelpBusinessDto> = emptyList(),
    @Json(name = "total") val total: Int = 0,
    @Json(name = "region") val region: YelpRegionDto? = null
)

@JsonClass(generateAdapter = true)
data class YelpBusinessDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "alias") val alias: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "url") val yelpUrl: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "display_phone") val displayPhone: String = "",
    @Json(name = "review_count") val reviewCount: Int = 0,
    @Json(name = "rating") val rating: Double = 0.0,
    @Json(name = "price") val priceLevel: String? = null,
    @Json(name = "distance") val distanceMeters: Double = 0.0,
    @Json(name = "is_closed") val isClosed: Boolean = false,
    @Json(name = "categories") val categories: List<YelpCategoryDto> = emptyList(),
    @Json(name = "location") val location: YelpLocationDto,
    @Json(name = "coordinates") val coordinates: YelpCoordinatesDto? = null
)

@JsonClass(generateAdapter = true)
data class YelpCategoryDto(
    @Json(name = "alias") val alias: String,
    @Json(name = "title") val title: String
)

@JsonClass(generateAdapter = true)
data class YelpLocationDto(
    @Json(name = "address1") val address1: String? = null,
    @Json(name = "city") val city: String = "",
    @Json(name = "state") val state: String = "",
    @Json(name = "country") val country: String = "",
    @Json(name = "display_address") val displayAddress: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YelpCoordinatesDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

@JsonClass(generateAdapter = true)
data class YelpRegionDto(
    @Json(name = "center") val center: YelpCoordinatesDto
)

// ---------------------------------------------------------------------------
// Category grid config — maps each UI icon to Yelp API category alias
// Matches the 9-grid in the SOW exactly
// ---------------------------------------------------------------------------

enum class PlaceCategory(
    val label: String,
    val yelpAlias: String,
    val iconSlug: String,
    val emoji: String,
    val attributes: String? = null
) {
    SAFETY("Safety", "policestations", "police_shield", "🛡"),
    URGENT_CARE("Urgent Care", "hospitals,emergencyrooms", "medical_cross", "🏥"),
    RESTAURANTS("Restaurants", "restaurants", "fork_knife", "🍜", attributes = "hot_and_new"),
    EV_CHARGING("EV Charging", "evchargingstations", "electric_car", "⚡"),
    BANKS_ATMS("Banks/ATMs", "banks,atms", "bank", "🏦"),
    CAFES("Cafes", "cafes", "coffee", "☕"),
    PHARMACIES("Pharmacy", "drugstores", "pharmacy", "💊"),
    TRANSPORT("Transport", "taxis,publictransport", "bus", "🚌"),
    HOTELS("Hotels", "hotels", "hotel", "🏨")
}
