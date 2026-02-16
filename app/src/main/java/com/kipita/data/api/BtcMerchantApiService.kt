package com.kipita.data.api

import retrofit2.http.GET
import retrofit2.http.Header

interface BtcMerchantApiService {
    @GET("api/v2/elements")
    suspend fun getBtcMapMerchants(): List<BtcMapMerchantDto>

    @GET("v1/bitcoin/merchants")
    suspend fun getCashAppMerchants(@Header("Authorization") bearer: String): List<CashAppMerchantDto>
}

data class BtcMapMerchantDto(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val lightning: Boolean? = null,
    val onchain: Boolean? = null,
    val updatedAt: Long? = null
)

data class CashAppMerchantDto(
    val id: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val acceptsCashAppPay: Boolean,
    val updatedAt: Long
)
