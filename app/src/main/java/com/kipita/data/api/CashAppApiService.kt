package com.kipita.data.api

import retrofit2.http.GET
import retrofit2.http.Header

// ---------------------------------------------------------------------------
// CashApp Pay API — Template (plug-and-play)
//
// SETUP: Store OAuth token via KeystoreManager.storeOAuthToken(CASHAPP_ALIAS, token)
// DOCS : https://developers.cash.app/docs
// ---------------------------------------------------------------------------

interface CashAppApiService {

    /**
     * Fetch the authenticated user's CashApp balance.
     * Requires OAuth2 bearer token from KeystoreManager.CASHAPP_OAUTH_TOKEN_ALIAS.
     *
     * TODO: Update endpoint when CashApp Pay API GA credentials are obtained.
     */
    @GET("v1/balance")
    suspend fun getBalance(
        @Header("Authorization") bearerToken: String
    ): CashAppBalanceResponse

    /**
     * Fetch recent payment history for travel expense tracking.
     * TODO: Implement when CashApp Pay API credentials are obtained.
     */
    @GET("v1/payments")
    suspend fun getPayments(
        @Header("Authorization") bearerToken: String,
        @retrofit2.http.Query("limit") limit: Int = 20
    ): CashAppPaymentsResponse
}

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

data class CashAppBalanceResponse(
    val btcBalance: Double = 0.0,
    val usdBalance: Double = 0.0,
    val status: String = "active",
    val currency: String = "USD"
)

data class CashAppPaymentsResponse(
    val payments: List<CashAppPaymentDto> = emptyList()
)

data class CashAppPaymentDto(
    val id: String = "",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val note: String = "",
    val timestamp: Long = 0L,
    val direction: String = "in" // "in" or "out"
)
