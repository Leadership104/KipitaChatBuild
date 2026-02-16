package com.kipita.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NomadApiService {
    @GET("v1/places")
    suspend fun getPlaces(@Query("country") country: String? = null): List<NomadPlaceDto>
}

interface CurrencyApiService {
    @GET("v1/latest")
    suspend fun getRates(@Query("base") base: String): CurrencyRateDto
}

data class NomadPlaceDto(
    val id: String,
    val city: String,
    val country: String,
    val costOfLivingUsd: Double,
    val internetMbps: Double,
    val safetyScore: Double,
    val walkabilityScore: Double,
    val weatherSummary: String,
    val timezone: String,
    val updatedAtEpochMillis: Long
)

data class CurrencyRateDto(
    val base: String,
    val rates: Map<String, Double>,
    val timestampEpochMillis: Long
)
