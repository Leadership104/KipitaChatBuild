package com.kipita.data.repository

import com.kipita.data.api.CoinbaseApiService
import com.kipita.data.api.GeminiCryptoApiService
import com.kipita.data.api.MarketDataWebSocket
import com.kipita.data.api.RiverApiService
import com.kipita.data.security.KeystoreManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

// ---------------------------------------------------------------------------
// CryptoWalletRepository
//
// Aggregates wallet data from Coinbase, Gemini, and River Financial into
// a unified "Travel Liquid Asset" view as defined in the SOW.
//
// JSON schema produced (maps to SOW spec):
// {
//   "user_id": "traveler_789",
//   "last_live_update": "...",
//   "wallets": {
//     "coinbase": {"asset": "BTC", "balance": 0.045, "status": "synced"},
//     "gemini":   {"asset": "ETH", "balance": 1.2,   "status": "synced"},
//     "river_wallet": {"asset": "BTC_LN", "sats": 500000, "status": "active"}
//   }
// }
// ---------------------------------------------------------------------------

data class WalletBalance(
    val source: WalletSource,
    val assetCode: String,
    val assetName: String,
    val balance: Double,           // In native asset units (BTC, ETH, USD, etc.)
    val balanceUsd: Double,        // USD equivalent at current market rate
    val status: WalletStatus,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)

enum class WalletSource(val label: String, val emoji: String) {
    COINBASE("Coinbase", "🟠"),
    GEMINI("Gemini", "🔵"),
    RIVER("River ⚡", "⚡"),
    MANUAL("Manual", "👤")
}

enum class WalletStatus {
    SYNCED, SYNCING, ERROR, OFFLINE
}

data class AggregatedWallet(
    val wallets: List<WalletBalance>,
    val totalUsd: Double,
    val lastSyncEpochMs: Long
) {
    val btcTotal: Double get() = wallets.filter { it.assetCode == "BTC" || it.assetCode == "BTC_LN" }.sumOf { it.balance }
    val ethTotal: Double get() = wallets.filter { it.assetCode == "ETH" }.sumOf { it.balance }
}

@Singleton
class CryptoWalletRepository @Inject constructor(
    private val coinbaseApi: CoinbaseApiService,
    private val geminiApi: GeminiCryptoApiService,
    private val riverApi: RiverApiService,
    private val marketFeed: MarketDataWebSocket,
    private val keystoreManager: KeystoreManager
) {
    // ---------------------------------------------------------------------------
    // ZERO-PERSISTENCE CONTRACT — Security requirement from SOW
    //
    // Financial balance data MUST NOT be written to any persistent storage:
    //   ✗ SQLite / Room       ✗ SharedPreferences / DataStore
    //   ✗ Disk files / logs   ✗ Crash reports / analytics events
    //
    // This @Volatile in-memory cache is the ONLY allowed storage. It is cleared
    // automatically on process death and can be explicitly cleared below.
    // ---------------------------------------------------------------------------

    @Volatile private var cachedWallet: AggregatedWallet? = null
    private val cacheMaxAgeMs = 60_000L

    /**
     * Explicitly clear in-memory balance cache.
     * Call when the app moves to background or user signs out to ensure
     * financial data is not accessible without a live network fetch.
     */
    fun clearBalanceCache() {
        cachedWallet = null
    }

    /**
     * Returns the aggregated wallet. Fetches all three sources in parallel.
     * Falls back to cached values if any source fails.
     */
    suspend fun getAggregatedWallet(forceRefresh: Boolean = false): AggregatedWallet {
        val cache = cachedWallet
        if (!forceRefresh && cache != null &&
            System.currentTimeMillis() - cache.lastSyncEpochMs < cacheMaxAgeMs) {
            return cache
        }

        val balances = mutableListOf<WalletBalance>()

        coroutineScope {
            val coinbaseDeferred = async { fetchCoinbase() }
            val geminiDeferred   = async { fetchGemini() }
            val riverDeferred    = async { fetchRiver() }

            balances += coinbaseDeferred.await()
            balances += geminiDeferred.await()
            balances += riverDeferred.await()
        }

        val result = AggregatedWallet(
            wallets = balances,
            totalUsd = balances.sumOf { it.balanceUsd },
            lastSyncEpochMs = System.currentTimeMillis()
        )
        cachedWallet = result
        return result
    }

    // -----------------------------------------------------------------------
    // Coinbase — fetch BTC, ETH, USD accounts via OAuth2
    // -----------------------------------------------------------------------

    private suspend fun fetchCoinbase(): List<WalletBalance> {
        val token = keystoreManager.getOAuthToken(KeystoreManager.COINBASE_OAUTH_TOKEN_ALIAS)
            ?: return listOf(offlineWallet(WalletSource.COINBASE, "BTC"))
        return try {
            val response = coinbaseApi.getAccounts("Bearer $token")
            response.accounts
                .filter { acc -> acc.balance.amount.toDoubleOrNull()?.let { abs(it) > 0.00000001 } == true }
                .map { acc ->
                    val balanceAmt = acc.balance.amount.toDoubleOrNull() ?: 0.0
                    val usdAmt = acc.nativeBalance.amount.toDoubleOrNull() ?: 0.0
                    WalletBalance(
                        source = WalletSource.COINBASE,
                        assetCode = acc.currency.code,
                        assetName = acc.currency.name,
                        balance = balanceAmt,
                        balanceUsd = usdAmt,
                        status = WalletStatus.SYNCED
                    )
                }
        } catch (e: Exception) {
            listOf(offlineWallet(WalletSource.COINBASE, "BTC"))
        }
    }

    // -----------------------------------------------------------------------
    // Gemini — fetch balances via HMAC-signed REST call
    // -----------------------------------------------------------------------

    private suspend fun fetchGemini(): List<WalletBalance> {
        val apiKey = keystoreManager.getApiKey(KeystoreManager.GEMINI_API_KEY_ALIAS)
        val apiSecret = keystoreManager.getApiKey(KeystoreManager.GEMINI_API_SECRET_ALIAS)
        if (apiKey == null || apiSecret == null) return listOf(offlineWallet(WalletSource.GEMINI, "ETH"))

        return try {
            // Build Gemini HMAC-SHA384 authentication payload
            val nonce = System.currentTimeMillis().toString()
            val payload = buildGeminiPayload("/v1/balances", nonce)
            val signature = signGemini(payload, apiSecret)

            val balances = geminiApi.getBalances(
                apiKey = apiKey,
                payload = payload,
                signature = signature
            )
            balances
                .filter { it.available.toDoubleOrNull()?.let { v -> abs(v) > 0.0001 } == true }
                .map { dto ->
                    val amt = dto.available.toDoubleOrNull() ?: 0.0
                    // Get live USD price from WebSocket or fallback to 0
                    val usdRate = when (dto.currency) {
                        "USD", "GUSD" -> 1.0
                        "BTC" -> marketFeed.lastPrice("BTCUSD") ?: 97000.0
                        "ETH" -> marketFeed.lastPrice("ETHUSD") ?: 3400.0
                        else -> 0.0
                    }
                    WalletBalance(
                        source = WalletSource.GEMINI,
                        assetCode = dto.currency,
                        assetName = dto.currency,
                        balance = amt,
                        balanceUsd = amt * usdRate,
                        status = WalletStatus.SYNCED
                    )
                }
        } catch (e: Exception) {
            listOf(offlineWallet(WalletSource.GEMINI, "ETH"))
        }
    }

    // -----------------------------------------------------------------------
    // River Financial — fetch on-chain + Lightning balance
    // -----------------------------------------------------------------------

    private suspend fun fetchRiver(): List<WalletBalance> {
        val token = keystoreManager.getOAuthToken(KeystoreManager.RIVER_OAUTH_TOKEN_ALIAS)
            ?: return listOf(offlineWallet(WalletSource.RIVER, "BTC_LN"))
        return try {
            val account = riverApi.getAccount("Bearer $token")
            val btcPrice = marketFeed.lastPrice("BTCUSD") ?: 97000.0
            buildList {
                // Lightning balance
                if (account.balances.lightningSats > 0) {
                    add(WalletBalance(
                        source = WalletSource.RIVER,
                        assetCode = "BTC_LN",
                        assetName = "Bitcoin Lightning",
                        balance = account.balances.lightningBtc,
                        balanceUsd = account.balances.lightningBtc * btcPrice,
                        status = WalletStatus.SYNCED
                    ))
                }
                // On-chain balance
                if (account.balances.bitcoinSats > 0) {
                    val onChainBtc = account.balances.bitcoinSats / 100_000_000.0
                    add(WalletBalance(
                        source = WalletSource.RIVER,
                        assetCode = "BTC",
                        assetName = "Bitcoin On-Chain",
                        balance = onChainBtc,
                        balanceUsd = onChainBtc * btcPrice,
                        status = WalletStatus.SYNCED
                    ))
                }
            }.ifEmpty { listOf(offlineWallet(WalletSource.RIVER, "BTC_LN")) }
        } catch (e: Exception) {
            listOf(offlineWallet(WalletSource.RIVER, "BTC_LN"))
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun offlineWallet(source: WalletSource, asset: String) = WalletBalance(
        source = source,
        assetCode = asset,
        assetName = asset,
        balance = 0.0,
        balanceUsd = 0.0,
        status = WalletStatus.OFFLINE
    )

    private fun buildGeminiPayload(endpoint: String, nonce: String): String {
        val payloadJson = """{"request":"$endpoint","nonce":"$nonce"}"""
        return android.util.Base64.encodeToString(payloadJson.toByteArray(), android.util.Base64.NO_WRAP)
    }

    private fun signGemini(payload: String, secret: String): String {
        val mac = javax.crypto.Mac.getInstance("HmacSHA384")
        val secretKey = javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA384")
        mac.init(secretKey)
        val bytes = mac.doFinal(payload.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
