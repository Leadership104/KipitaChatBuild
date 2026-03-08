package com.kipita.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

// ---------------------------------------------------------------------------
// Generic envelope
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatResponse<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null
)

// ---------------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatLoginRequest(val email: String, val password: String, val action: String = "Login")

@JsonClass(generateAdapter = true)
data class DwaatLoginResponse(
    val status: String,
    val message: String,
    val userId: String? = null,
    val token: String? = null
)

@JsonClass(generateAdapter = true)
data class DwaatRegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val action: String = "Register"
)

@JsonClass(generateAdapter = true)
data class DwaatSocialLoginRequest(
    val email: String,
    val name: String,
    val provider: String,   // "google" | "apple" | "facebook"
    val providerId: String,
    val action: String = "SocialLogin"
)

@JsonClass(generateAdapter = true)
data class DwaatDeleteAccountRequest(
    val userId: String,
    val token: String,
    val action: String = "DeleteAccount"
)

@JsonClass(generateAdapter = true)
data class DwaatValidateProfileRequest(
    val userId: String,
    val token: String,
    val action: String = "ValidateProfile"
)

@JsonClass(generateAdapter = true)
data class DwaatGenericResponse(val status: String, val message: String)

// ---------------------------------------------------------------------------
// Preferences
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatPreferencesRequest(
    val userId: String,
    val token: String,
    val action: String = "GetPreferences"
)

@JsonClass(generateAdapter = true)
data class DwaatSavePreferencesRequest(
    val userId: String,
    val token: String,
    val preferences: Map<String, String>,
    val action: String = "SavePreferences"
)

@JsonClass(generateAdapter = true)
data class DwaatPreferencesData(
    val currency: String? = null,
    val language: String? = null,
    val notifications: Boolean? = null,
    val travelStyle: String? = null
)

// ---------------------------------------------------------------------------
// Advisory / Weather
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatAdvisoryRequest(
    val country: String? = null,
    val action: String = "GetAdvisorySections"
)

@JsonClass(generateAdapter = true)
data class DwaatWeatherAdvisoryRequest(
    val lat: Double,
    val lng: Double,
    val country: String? = null,
    val action: String = "GetWeatherAdvisory"
)

@JsonClass(generateAdapter = true)
data class DwaatAdvisorySection(
    val id: Int? = null,
    val title: String? = null,
    val content: String? = null,
    val icon: String? = null,
    val level: String? = null   // "safe" | "caution" | "danger"
)

@JsonClass(generateAdapter = true)
data class DwaatWeatherAdvisory(
    val temperature: String? = null,
    val condition: String? = null,
    val advisory: String? = null,
    val level: String? = null
)

// ---------------------------------------------------------------------------
// Restrictions
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatRestrictionsRequest(
    val country: String? = null,
    val action: String = "GetRestrictions"
)

@JsonClass(generateAdapter = true)
data class DwaatRestrictionsData(
    val advisory_main: Map<String, String>? = null,
    val advisory_tab: List<DwaatAdvisorySection>? = null
)

// ---------------------------------------------------------------------------
// Planner & Places sections
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatPlannerRequest(
    val destination: String? = null,
    val userId: String? = null,
    val token: String? = null,
    val action: String = "GetPlannerSections"
)

@JsonClass(generateAdapter = true)
data class DwaatPlannerSection(
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val items: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class DwaatPlacesSectionsRequest(
    val lat: Double? = null,
    val lng: Double? = null,
    val destination: String? = null,
    val action: String = "GetPlacesSections"
)

@JsonClass(generateAdapter = true)
data class DwaatPlaceSection(
    val id: Int? = null,
    val name: String? = null,
    val category: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val rating: Double? = null,
    val address: String? = null
)

// ---------------------------------------------------------------------------
// Groups
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatGroupRequest(
    val userId: String,
    val token: String,
    val action: String = "GetGroups"
)

@JsonClass(generateAdapter = true)
data class DwaatCreateGroupRequest(
    val userId: String,
    val token: String,
    val name: String,
    val description: String? = null,
    val action: String = "CreateGroup"
)

@JsonClass(generateAdapter = true)
data class DwaatGroup(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val memberCount: Int? = null,
    val createdAt: String? = null
)

// ---------------------------------------------------------------------------
// Bookmarks
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatBookmarksRequest(
    val userId: String,
    val token: String,
    val action: String = "GetBookmarks"
)

@JsonClass(generateAdapter = true)
data class DwaatAddBookmarkRequest(
    val userId: String,
    val token: String,
    val placeId: String,
    val placeName: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val action: String = "AddBookmark"
)

@JsonClass(generateAdapter = true)
data class DwaatBookmark(
    val id: Int? = null,
    val placeId: String? = null,
    val placeName: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val createdAt: String? = null
)

// ---------------------------------------------------------------------------
// Notifications
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatNotificationsRequest(
    val userId: String,
    val token: String,
    val action: String = "GetNotifications"
)

@JsonClass(generateAdapter = true)
data class DwaatPushNotificationRequest(
    val userId: String,
    val token: String,
    val fcmToken: String,
    val action: String = "RegisterPushToken"
)

@JsonClass(generateAdapter = true)
data class DwaatNotification(
    val id: Int? = null,
    val title: String? = null,
    val body: String? = null,
    val read: Boolean? = null,
    val createdAt: String? = null
)

// ---------------------------------------------------------------------------
// Airport
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatAirportRequest(
    val query: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val action: String = "SearchAirport"
)

@JsonClass(generateAdapter = true)
data class DwaatAirport(
    val name: String? = null,
    val iata: String? = null,
    val country: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

// ---------------------------------------------------------------------------
// Zip code
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatZipcodeRequest(
    val zipcode: String,
    val action: String = "ReadZipcode"
)

@JsonClass(generateAdapter = true)
data class DwaatZipcodeData(
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

// ---------------------------------------------------------------------------
// Affiliates / Perks
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class AffiliatesRequest(val action: String = "GetAllList")

@JsonClass(generateAdapter = true)
data class AffiliatesResponse(val status: String? = null, val data: List<PerkItem>? = null)

@JsonClass(generateAdapter = true)
data class PerkItem(
    val id: Int? = null,
    val name: String = "",
    val description: String = "",
    val link: String = "",
    val image: String = "",
    val order: Int? = null
)

// ---------------------------------------------------------------------------
// Image
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DwaatImageListRequest(
    val userId: String,
    val token: String,
    val action: String = "GetImages"
)

@JsonClass(generateAdapter = true)
data class DwaatImageSearchRequest(
    val query: String,
    val action: String = "SearchImages"
)

@JsonClass(generateAdapter = true)
data class DwaatImage(
    val id: Int? = null,
    val url: String? = null,
    val title: String? = null,
    val thumbnailUrl: String? = null
)

// ---------------------------------------------------------------------------
// Retrofit interface
// ---------------------------------------------------------------------------

interface DwaatApiService {

    // Auth
    @POST("login.php")
    suspend fun login(@Body request: DwaatLoginRequest): DwaatLoginResponse

    @POST("register.php")
    suspend fun register(@Body request: DwaatRegisterRequest): DwaatGenericResponse

    @POST("forgotPassword.php")
    suspend fun forgotPassword(@Body request: Map<String, String>): DwaatGenericResponse

    @POST("socialLogin.php")
    suspend fun socialLogin(@Body request: DwaatSocialLoginRequest): DwaatLoginResponse

    @POST("deleteAccount.php")
    suspend fun deleteAccount(@Body request: DwaatDeleteAccountRequest): DwaatGenericResponse

    @POST("validateProfile.php")
    suspend fun validateProfile(@Body request: DwaatValidateProfileRequest): DwaatGenericResponse

    // Preferences
    @POST("preferences.php")
    suspend fun getPreferences(@Body request: DwaatPreferencesRequest): DwaatResponse<DwaatPreferencesData>

    @POST("preferences.php")
    suspend fun savePreferences(@Body request: DwaatSavePreferencesRequest): DwaatGenericResponse

    // Advisory / Weather
    @POST("advisorySections.php")
    suspend fun getAdvisorySections(@Body request: DwaatAdvisoryRequest): DwaatResponse<List<DwaatAdvisorySection>>

    @POST("getWeatherAdvisory.php")
    suspend fun getWeatherAdvisory(@Body request: DwaatWeatherAdvisoryRequest): DwaatResponse<DwaatWeatherAdvisory>

    @POST("restrictions.php")
    suspend fun getRestrictions(@Body request: DwaatRestrictionsRequest): DwaatResponse<DwaatRestrictionsData>

    // Planner & Places
    @POST("plannerSections.php")
    suspend fun getPlannerSections(@Body request: DwaatPlannerRequest): DwaatResponse<List<DwaatPlannerSection>>

    @POST("placesSections.php")
    suspend fun getPlacesSections(@Body request: DwaatPlacesSectionsRequest): DwaatResponse<List<DwaatPlaceSection>>

    // Groups
    @POST("group.php")
    suspend fun getGroups(@Body request: DwaatGroupRequest): DwaatResponse<List<DwaatGroup>>

    @POST("group.php")
    suspend fun createGroup(@Body request: DwaatCreateGroupRequest): DwaatResponse<DwaatGroup>

    // Bookmarks
    @POST("bookmarks.php")
    suspend fun getBookmarks(@Body request: DwaatBookmarksRequest): DwaatResponse<List<DwaatBookmark>>

    @POST("bookmarks.php")
    suspend fun addBookmark(@Body request: DwaatAddBookmarkRequest): DwaatGenericResponse

    // Notifications
    @POST("notification.php")
    suspend fun getNotifications(@Body request: DwaatNotificationsRequest): DwaatResponse<List<DwaatNotification>>

    @POST("pushNotification.php")
    suspend fun registerPushToken(@Body request: DwaatPushNotificationRequest): DwaatGenericResponse

    // Airport
    @POST("airport.php")
    suspend fun searchAirport(@Body request: DwaatAirportRequest): DwaatResponse<List<DwaatAirport>>

    @GET("airport.json")
    suspend fun getAllAirports(): List<DwaatAirport>

    // Zip code
    @POST("readZipcode.php")
    suspend fun readZipcode(@Body request: DwaatZipcodeRequest): DwaatResponse<DwaatZipcodeData>

    // Affiliates
    @POST("affiliates.php")
    suspend fun getAffiliates(@Body request: AffiliatesRequest = AffiliatesRequest()): AffiliatesResponse

    @POST
    suspend fun getAffiliatesAt(
        @Url url: String,
        @Body request: AffiliatesRequest = AffiliatesRequest()
    ): AffiliatesResponse

    // Images
    @POST("imageList.php")
    suspend fun getImages(@Body request: DwaatImageListRequest): DwaatResponse<List<DwaatImage>>

    @POST("imageSearch.php")
    suspend fun searchImages(@Body request: DwaatImageSearchRequest): DwaatResponse<List<DwaatImage>>
}
