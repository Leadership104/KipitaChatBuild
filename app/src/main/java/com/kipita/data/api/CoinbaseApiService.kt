package com.kipita.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

// ---------------------------------------------------------------------------
// Coinbase Advanced Trade API
// Base URL: https://api.coinbase.com/
// Auth: Bearer OAuth2 access token (stored in Android KeyStore)
// ---------------------------------------------------------------------------

interface CoinbaseApiService {

    /**
     * List all wallets / accounts for the authenticated user.
     * OAuth2 scope: wallet:accounts:read
     */
    @GET("v2/accounts")
    suspend fun getAccounts(
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = 100
    ): CoinbaseAccountsResponse

    /**
     * Get a single account's details, including current balance.
     */
    @GET("v2/accounts/{account_id}")
    suspend fun getAccount(
        @Header("Authorization") bearerToken: String,
        @Path("account_id") accountId: String
    ): CoinbaseAccountWrapper

    /**
     * Get current spot price for a currency pair (e.g. BTC-USD).
     */
    @GET("v2/prices/{currency_pair}/spot")
    suspend fun getSpotPrice(
        @Path("currency_pair") currencyPair: String
    ): CoinbaseSpotPriceWrapper
}

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class CoinbaseAccountsResponse(
    @param:Json(name = "data") val accounts: List<CoinbaseAccountDto> = emptyList(),
    @param:Json(name = "pagination") val pagination: CoinbasePaginationDto? = null
)

@JsonClass(generateAdapter = true)
data class CoinbaseAccountWrapper(
    @param:Json(name = "data") val account: CoinbaseAccountDto
)

@JsonClass(generateAdapter = true)
data class CoinbaseAccountDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "primary") val isPrimary: Boolean = false,
    @param:Json(name = "type") val type: String = "",
    @param:Json(name = "currency") val currency: CoinbaseCurrencyDto,
    @param:Json(name = "balance") val balance: CoinbaseMoneyDto,
    @param:Json(name = "native_balance") val nativeBalance: CoinbaseMoneyDto,
    @param:Json(name = "updated_at") val updatedAt: String = ""
)

@JsonClass(generateAdapter = true)
data class CoinbaseCurrencyDto(
    @param:Json(name = "code") val code: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "color") val color: String = ""
)

@JsonClass(generateAdapter = true)
data class CoinbaseMoneyDto(
    @param:Json(name = "amount") val amount: String,
    @param:Json(name = "currency") val currency: String
)

@JsonClass(generateAdapter = true)
data class CoinbasePaginationDto(
    @param:Json(name = "next_uri") val nextUri: String? = null,
    @param:Json(name = "previous_uri") val previousUri: String? = null
)

@JsonClass(generateAdapter = true)
data class CoinbaseSpotPriceWrapper(
    @param:Json(name = "data") val data: CoinbaseSpotPriceDto
)

@JsonClass(generateAdapter = true)
data class CoinbaseSpotPriceDto(
    @param:Json(name = "base") val base: String,
    @param:Json(name = "currency") val currency: String,
    @param:Json(name = "amount") val amount: String
)
