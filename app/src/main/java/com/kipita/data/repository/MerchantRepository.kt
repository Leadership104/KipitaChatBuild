package com.kipita.data.repository

import com.kipita.data.api.BtcMerchantApiService
import com.kipita.data.api.acceptsLightning
import com.kipita.data.api.acceptsOnchain
import com.kipita.data.api.isDeleted
import com.kipita.data.api.latitude
import com.kipita.data.api.longitude
import com.kipita.data.api.name
import com.kipita.data.local.MerchantDao
import com.kipita.data.local.MerchantEntity
import com.kipita.domain.model.MerchantLocation
import java.time.Instant
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MerchantRepository(
    private val apiService: BtcMerchantApiService,
    private val merchantDao: MerchantDao
) {
    /**
     * Refresh BTC merchant list from BTCMap v2 + optional CashApp.
     * Filters to a ~50 km bounding box around [userLat]/[userLng] before
     * persisting to Room, so we don't bloat local storage with global data.
     * Falls back to cached Room data on network failure.
     */
    suspend fun refresh(
        cashAppToken: String?,
        userLat: Double = 0.0,
        userLng: Double = 0.0
    ): List<MerchantLocation> = withContext(Dispatchers.IO) {
        val btcMap = runCatching { apiService.getBtcMapMerchants() }
            .getOrDefault(emptyList())
            .filter { !it.isDeleted && it.latitude != null && it.longitude != null }
            .filter { dto ->
                // ~0.45° ≈ 50 km bounding box (skipped when userLat==0)
                if (userLat == 0.0 && userLng == 0.0) true
                else abs(dto.latitude!! - userLat) < 0.45 && abs(dto.longitude!! - userLng) < 0.45
            }
            .map { dto ->
                MerchantLocation(
                    id = "btcmap-${dto.id}",
                    name = dto.name,
                    latitude = dto.latitude!!,
                    longitude = dto.longitude!!,
                    acceptsOnchainBtc = dto.acceptsOnchain,
                    acceptsLightning = dto.acceptsLightning,
                    acceptsCashApp = false,
                    source = "btcmap.org",
                    lastVerified = Instant.now(),
                    metadata = mapOf("source_type" to "open_data")
                )
            }
        val cashApp = if (cashAppToken.isNullOrBlank()) emptyList() else runCatching {
            apiService.getCashAppMerchants("Bearer $cashAppToken")
        }.getOrDefault(emptyList()).map {
            MerchantLocation(
                id = "cashapp-${it.id}",
                name = it.displayName,
                latitude = it.latitude,
                longitude = it.longitude,
                acceptsOnchainBtc = false,
                acceptsLightning = false,
                acceptsCashApp = it.acceptsCashAppPay,
                source = "cash.app",
                lastVerified = Instant.ofEpochMilli(it.updatedAt),
                metadata = mapOf("source_type" to "official_api")
            )
        }

        val merged = (btcMap + cashApp).distinctBy { it.id }
        if (merged.isNotEmpty()) {
            merchantDao.upsertAll(merged.map(MerchantLocation::toEntity))
            merged
        } else {
            merchantDao.getAll().map(MerchantEntity::toDomain)
        }
    }

    suspend fun getCachedMerchants(): List<MerchantLocation> = merchantDao.getAll().map(MerchantEntity::toDomain)
}

private fun MerchantLocation.toEntity(): MerchantEntity = MerchantEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    acceptsOnchainBtc = acceptsOnchainBtc,
    acceptsLightning = acceptsLightning,
    acceptsCashApp = acceptsCashApp,
    source = source,
    lastVerifiedEpochMillis = lastVerified.toEpochMilli(),
    metadataJson = metadata.entries.joinToString(";") { "${it.key}=${it.value}" }
)

private fun MerchantEntity.toDomain(): MerchantLocation = MerchantLocation(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    acceptsOnchainBtc = acceptsOnchainBtc,
    acceptsLightning = acceptsLightning,
    acceptsCashApp = acceptsCashApp,
    source = source,
    lastVerified = Instant.ofEpochMilli(lastVerifiedEpochMillis),
    metadata = metadataJson.split(';').mapNotNull {
        val parts = it.split('=')
        if (parts.size == 2) parts[0] to parts[1] else null
    }.toMap()
)
