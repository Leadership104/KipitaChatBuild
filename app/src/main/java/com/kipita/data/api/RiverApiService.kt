package com.kipita.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Body

// ---------------------------------------------------------------------------
// River Financial — Bitcoin (On-chain + Lightning Network) API
// Base URL: https://app.river.com/
// Auth: Bearer OAuth2 token (stored in Android KeyStore)
// ---------------------------------------------------------------------------

interface RiverApiService {

    /**
     * Retrieve the user's River Bitcoin wallet balances.
     * Returns both confirmed on-chain satoshis and Lightning channel balance.
     */
    @GET("api/v1/account")
    suspend fun getAccount(
        @Header("Authorization") bearerToken: String
    ): RiverAccountResponse

    /**
     * Retrieve recent transaction history for the Lightning wallet.
     */
    @GET("api/v1/payments")
    suspend fun getPayments(
        @Header("Authorization") bearerToken: String
    ): RiverPaymentsResponse

    /**
     * Create a Lightning invoice to receive Bitcoin.
     */
    @POST("api/v1/invoices")
    suspend fun createInvoice(
        @Header("Authorization") bearerToken: String,
        @Body request: RiverCreateInvoiceRequest
    ): RiverInvoiceResponse
}

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class RiverAccountResponse(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "email") val email: String = "",
    @param:Json(name = "bitcoin_address") val bitcoinAddress: String = "",
    @param:Json(name = "balances") val balances: RiverBalancesDto
)

@JsonClass(generateAdapter = true)
data class RiverBalancesDto(
    /** On-chain confirmed Bitcoin balance in satoshis */
    @param:Json(name = "bitcoin") val bitcoinSats: Long = 0L,
    /** Lightning Network spendable balance in satoshis */
    @param:Json(name = "lightning") val lightningSats: Long = 0L,
    /** Total satoshis (on-chain + lightning) */
    @param:Json(name = "total") val totalSats: Long = 0L
) {
    val totalBtc: Double get() = totalSats / 100_000_000.0
    val lightningBtc: Double get() = lightningSats / 100_000_000.0
}

@JsonClass(generateAdapter = true)
data class RiverPaymentsResponse(
    @param:Json(name = "payments") val payments: List<RiverPaymentDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RiverPaymentDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "type") val type: String,          // "lightning" | "bitcoin"
    @param:Json(name = "status") val status: String,      // "completed" | "pending" | "failed"
    @param:Json(name = "amount_sats") val amountSats: Long,
    @param:Json(name = "fee_sats") val feeSats: Long = 0L,
    @param:Json(name = "description") val description: String = "",
    @param:Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class RiverCreateInvoiceRequest(
    @param:Json(name = "amount_sats") val amountSats: Long,
    @param:Json(name = "description") val description: String = "Kipita travel payment"
)

@JsonClass(generateAdapter = true)
data class RiverInvoiceResponse(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "payment_request") val paymentRequest: String, // BOLT-11 invoice string
    @param:Json(name = "amount_sats") val amountSats: Long,
    @param:Json(name = "expires_at") val expiresAt: String
)
