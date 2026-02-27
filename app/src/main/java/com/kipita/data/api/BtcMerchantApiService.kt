package com.kipita.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BtcMerchantApiService {
    /**
     * BTCMap v2 elements — returns all active BTC-accepting merchants.
     * updated_since: ISO-8601 timestamp for incremental syncs (e.g. "2024-01-01T00:00:00Z")
     */
    @GET("v2/elements")
    suspend fun getBtcMapMerchants(
        @Query("updated_since") updatedSince: String? = null
    ): List<BtcMapElementDto>

    @GET("v1/bitcoin/merchants")
    suspend fun getCashAppMerchants(@Header("Authorization") bearer: String): List<CashAppMerchantDto>
}

// ---------------------------------------------------------------------------
// BTCMap v2 Element DTO — nested osm_json structure
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class BtcMapElementDto(
    @Json(name = "id")          val id: String,
    @Json(name = "osm_json")    val osmJson: BtcMapOsmJson? = null,
    @Json(name = "tags")        val tags: Map<String, String>? = null,
    @Json(name = "updated_at")  val updatedAt: String? = null,
    @Json(name = "deleted_at")  val deletedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class BtcMapOsmJson(
    @Json(name = "lat")  val lat: Double? = null,
    @Json(name = "lon")  val lon: Double? = null,
    @Json(name = "tags") val tags: Map<String, String>? = null
)

// Convenience accessors on the DTO
val BtcMapElementDto.latitude:  Double? get() = osmJson?.lat
val BtcMapElementDto.longitude: Double? get() = osmJson?.lon
val BtcMapElementDto.name: String
    get() = osmJson?.tags?.get("name")
        ?: tags?.get("name")
        ?: "Bitcoin Merchant"
val BtcMapElementDto.acceptsLightning: Boolean
    get() = osmJson?.tags?.get("payment:lightning")?.lowercase() == "yes"
        || tags?.get("payment:lightning")?.lowercase() == "yes"
val BtcMapElementDto.acceptsOnchain: Boolean
    get() = osmJson?.tags?.get("payment:bitcoin")?.lowercase() == "yes"
        || osmJson?.tags?.get("payment:onchain")?.lowercase() == "yes"
        || tags?.get("payment:onchain")?.lowercase() == "yes"
val BtcMapElementDto.isDeleted: Boolean get() = !deletedAt.isNullOrBlank()

// ---------------------------------------------------------------------------
// CashApp merchant DTO (unchanged)
// ---------------------------------------------------------------------------

data class CashAppMerchantDto(
    val id: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val acceptsCashAppPay: Boolean,
    val updatedAt: Long
)
