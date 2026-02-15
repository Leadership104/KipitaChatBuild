package com.kipita.data.api

import retrofit2.http.GET
import retrofit2.http.Header

interface WalletApiService {
    @GET("coinbase/v1/balance")
    suspend fun coinbaseBalance(@Header("Authorization") token: String): WalletBalanceDto

    @GET("cashapp/v1/balance")
    suspend fun cashAppBalance(@Header("Authorization") token: String): WalletBalanceDto
}

data class WalletBalanceDto(val provider: String, val btcBalance: Double, val updatedAt: Long)
