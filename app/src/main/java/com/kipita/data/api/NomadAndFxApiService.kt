package com.kipita.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NomadApiService {
    @GET("v1/places")
    suspend fun getPlaces(@Query("country") country: String? = null): List<NomadPlaceDto>
}

// Frankfurter.app: free ECB-sourced exchange rates, no API key required
// GET https://api.frankfurter.app/latest?base=USD
interface CurrencyApiService {
    @GET("latest")
    suspend fun getRates(
        @Query("base") base: String,
        @Query("symbols") symbols: String? = null
    ): CurrencyRateDto

    @GET("currencies")
    suspend fun getCurrencies(): Map<String, String>
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

// Frankfurter API response: {"amount":1.0,"base":"USD","date":"2026-02-21","rates":{...}}
data class CurrencyRateDto(
    val amount: Double = 1.0,
    val base: String,
    val date: String = "",
    val rates: Map<String, Double>
)
