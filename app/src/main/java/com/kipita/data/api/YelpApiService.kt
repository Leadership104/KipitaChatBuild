package com.kipita.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// ---------------------------------------------------------------------------
// Google Places API (New) — v1
// Base URL: https://places.googleapis.com/
// Auth: X-Goog-Api-Key header (API key from google-services.json)
// Docs: https://developers.google.com/maps/documentation/places/web-service/op-overview
// ---------------------------------------------------------------------------

interface GooglePlacesApiService {

    /**
     * Nearby Search — finds places within a circle around a lat/lon.
     * Used for the category grid (restaurants, hotels, hospitals, ATMs, etc.)
     */
    @POST("v1/places:searchNearby")
    suspend fun searchNearby(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String,
        @Body request: NearbySearchRequest
    ): PlacesSearchResponse

    /**
     * Text Search — finds places matching a free-text query.
     * Used when user types a location manually or GPS is unavailable.
     */
    @POST("v1/places:searchText")
    suspend fun searchByText(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String,
        @Body request: TextSearchRequest
    ): PlacesSearchResponse

    /**
     * Place Details — fetches full details for a single place by resource name.
     * placeId format: "places/ChIJ..."
     */
    @GET("v1/{placeId}")
    suspend fun getPlaceDetails(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String,
        @Path("placeId", encoded = true) placeId: String
    ): GooglePlaceDto
}

// ---------------------------------------------------------------------------
// Field masks — controls which fields are returned (and billed)
// ---------------------------------------------------------------------------

const val PLACES_LIST_FIELD_MASK =
    "places.id,places.displayName,places.formattedAddress,places.location," +
    "places.rating,places.priceLevel,places.types,places.nationalPhoneNumber," +
    "places.currentOpeningHours,places.photos"

const val PLACES_DETAIL_FIELD_MASK =
    "$PLACES_LIST_FIELD_MASK,places.websiteUri,places.regularOpeningHours," +
    "places.editorialSummary,places.reviews"

// ---------------------------------------------------------------------------
// Request bodies
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class NearbySearchRequest(
    @field:Json(name = "includedTypes") val includedTypes: List<String>,
    @field:Json(name = "maxResultCount") val maxResultCount: Int = 20,
    @field:Json(name = "locationRestriction") val locationRestriction: LocationRestriction,
    @field:Json(name = "rankPreference") val rankPreference: String = "DISTANCE"
)

@JsonClass(generateAdapter = true)
data class TextSearchRequest(
    @field:Json(name = "textQuery") val textQuery: String,
    @field:Json(name = "maxResultCount") val maxResultCount: Int = 20,
    @field:Json(name = "openNow") val openNow: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LocationRestriction(
    @field:Json(name = "circle") val circle: PlaceCircle
)

@JsonClass(generateAdapter = true)
data class PlaceCircle(
    @field:Json(name = "center") val center: PlaceLatLng,
    @field:Json(name = "radius") val radius: Double = 1000.0
)

@JsonClass(generateAdapter = true)
data class PlaceLatLng(
    @field:Json(name = "latitude") val latitude: Double,
    @field:Json(name = "longitude") val longitude: Double
)

// ---------------------------------------------------------------------------
// Response DTOs
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class PlacesSearchResponse(
    @field:Json(name = "places") val places: List<GooglePlaceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GooglePlaceDto(
    @field:Json(name = "id") val id: String = "",
    @field:Json(name = "displayName") val displayName: PlaceDisplayName? = null,
    @field:Json(name = "formattedAddress") val formattedAddress: String = "",
    @field:Json(name = "location") val location: PlaceLatLng? = null,
    @field:Json(name = "rating") val rating: Double = 0.0,
    @field:Json(name = "priceLevel") val priceLevel: String? = null,
    @field:Json(name = "types") val types: List<String> = emptyList(),
    @field:Json(name = "nationalPhoneNumber") val nationalPhoneNumber: String = "",
    @field:Json(name = "websiteUri") val websiteUri: String = "",
    @field:Json(name = "currentOpeningHours") val currentOpeningHours: PlaceOpeningHours? = null,
    @field:Json(name = "photos") val photos: List<PlacePhoto> = emptyList(),
    @field:Json(name = "editorialSummary") val editorialSummary: PlaceDisplayName? = null
)

@JsonClass(generateAdapter = true)
data class PlaceDisplayName(
    @field:Json(name = "text") val text: String = "",
    @field:Json(name = "languageCode") val languageCode: String = "en"
)

@JsonClass(generateAdapter = true)
data class PlaceOpeningHours(
    @field:Json(name = "openNow") val openNow: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PlacePhoto(
    // Resource name: "places/{place_id}/photos/{photo_reference}"
    // Fetch via: https://places.googleapis.com/v1/{name}/media?key=...&maxWidthPx=400
    @field:Json(name = "name") val name: String = ""
)

// ---------------------------------------------------------------------------
// Category grid config — maps each UI tile to Google Places includedTypes
// Matches the 9-grid in the SOW exactly
// ---------------------------------------------------------------------------

enum class PlaceCategory(
    val label: String,
    val googleTypes: List<String>,   // Google Places API "includedTypes"
    val iconSlug: String,
    val emoji: String,
    val groupTag: String = "General"
) {
    // ── Safety & Health ──────────────────────────────────────────────────────
    SAFETY(
        "Safety", listOf("police"), "police_shield", "🛡",
        groupTag = "Safety & Health"
    ),
    URGENT_CARE(
        "Urgent Care", listOf("hospital", "emergency_room_hospital"), "medical_cross", "🏥",
        groupTag = "Safety & Health"
    ),
    PHARMACIES(
        "Pharmacy", listOf("pharmacy", "drugstore"), "pharmacy", "💊",
        groupTag = "Safety & Health"
    ),
    FITNESS(
        "Fitness", listOf("gym", "fitness_center"), "dumbbell", "💪",
        groupTag = "Safety & Health"
    ),

    // ── Travel & Lodging ─────────────────────────────────────────────────────
    HOTELS(
        "Hotels", listOf("lodging", "hotel"), "hotel", "🏨",
        groupTag = "Travel & Lodging"
    ),
    VACATION_RENTALS(
        "Vacation Rentals", listOf("extended_stay_hotel", "campground"), "house", "🏠",
        groupTag = "Travel & Lodging"
    ),
    AIRPORTS(
        "Airport Services", listOf("airport"), "plane", "✈️",
        groupTag = "Travel & Lodging"
    ),
    TOURS(
        "Tours & Activities", listOf("tourist_attraction", "cultural_landmark", "art_gallery"),
        "compass", "🧭",
        groupTag = "Travel & Lodging"
    ),

    // ── Transportation ───────────────────────────────────────────────────────
    TRANSPORT(
        "Public Transit", listOf("subway_station", "bus_station", "transit_station"),
        "bus", "🚌",
        groupTag = "Transportation"
    ),
    CAR_RENTAL(
        "Car Rental", listOf("car_rental"), "car", "🚗",
        groupTag = "Transportation"
    ),
    EV_CHARGING(
        "EV Charging", listOf("electric_vehicle_charging_station"), "electric_car", "⚡",
        groupTag = "Transportation"
    ),
    GAS_STATIONS(
        "Gas & Fuel", listOf("gas_station"), "gas", "⛽",
        groupTag = "Transportation"
    ),

    // ── Dining & Cafes ───────────────────────────────────────────────────────
    RESTAURANTS(
        "Restaurants", listOf("restaurant", "food"), "fork_knife", "🍜",
        groupTag = "Dining"
    ),
    CAFES(
        "Cafes", listOf("cafe", "coffee_shop"), "coffee", "☕",
        groupTag = "Dining"
    ),
    NIGHTLIFE(
        "Bars & Nightlife", listOf("bar", "night_club"), "moon_stars", "🌙",
        groupTag = "Dining"
    ),

    // ── Finance & Services ───────────────────────────────────────────────────
    BANKS_ATMS(
        "Banks & ATMs", listOf("bank", "atm"), "bank", "🏦",
        groupTag = "Finance & Services"
    ),

    // ── Culture & Shopping ───────────────────────────────────────────────────
    ARTS(
        "Arts & Culture", listOf("art_gallery", "museum"), "art", "🎨",
        groupTag = "Culture & Shopping"
    ),
    SHOPPING(
        "Shopping", listOf("shopping_mall", "department_store"), "shopping_bag", "🛍",
        groupTag = "Culture & Shopping"
    ),
    PARKS(
        "Parks & Nature", listOf("park", "national_park", "hiking_area"), "tree", "🌳",
        groupTag = "Culture & Shopping"
    ),
    ENTERTAINMENT(
        "Entertainment", listOf("movie_theater", "amusement_park", "performing_arts_theater"),
        "star", "🎭",
        groupTag = "Culture & Shopping"
    )
}
