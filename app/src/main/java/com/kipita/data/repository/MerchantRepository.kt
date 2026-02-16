package com.kipita.data.repository

import com.kipita.data.api.BtcMerchantApiService
import com.kipita.data.local.MerchantDao
import com.kipita.data.local.MerchantEntity
import com.kipita.domain.model.MerchantLocation
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MerchantRepository(
    private val apiService: BtcMerchantApiService,
    private val merchantDao: MerchantDao
) {
    suspend fun refresh(cashAppToken: String?): List<MerchantLocation> = withContext(Dispatchers.IO) {
        val btcMap = runCatching { apiService.getBtcMapMerchants() }.getOrDefault(emptyList()).map {
            MerchantLocation(
                id = "btcmap-${it.id}",
                name = it.name,
                latitude = it.lat,
                longitude = it.lon,
                acceptsOnchainBtc = it.onchain ?: true,
                acceptsLightning = it.lightning ?: false,
                acceptsCashApp = false,
                source = "btcmap.org",
                lastVerified = Instant.ofEpochMilli(it.updatedAt ?: Instant.now().toEpochMilli()),
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
