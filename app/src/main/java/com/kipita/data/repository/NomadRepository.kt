package com.kipita.data.repository

import com.kipita.data.api.NomadApiService
import com.kipita.data.local.NomadPlaceDao
import com.kipita.data.local.NomadPlaceEntity
import com.kipita.domain.model.NomadPlaceInfo
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NomadRepository(
    private val api: NomadApiService,
    private val dao: NomadPlaceDao
) {
    suspend fun refresh(country: String? = null): List<NomadPlaceInfo> = withContext(Dispatchers.IO) {
        val remote = runCatching { api.getPlaces(country) }.getOrDefault(emptyList())
        if (remote.isNotEmpty()) {
            val mapped = remote.map {
                NomadPlaceInfo(
                    placeId = it.id,
                    city = it.city,
                    country = it.country,
                    costOfLivingUsd = it.costOfLivingUsd,
                    internetMbps = it.internetMbps,
                    safetyScore = it.safetyScore,
                    walkabilityScore = it.walkabilityScore,
                    weatherSummary = it.weatherSummary,
                    timezone = it.timezone,
                    updatedAt = Instant.ofEpochMilli(it.updatedAtEpochMillis)
                )
            }
            dao.upsertAll(mapped.map(NomadPlaceInfo::toEntity))
            return@withContext mapped
        }
        dao.getAll().map(NomadPlaceEntity::toDomain)
    }
}

private fun NomadPlaceInfo.toEntity() = NomadPlaceEntity(
    placeId,
    city,
    country,
    costOfLivingUsd,
    internetMbps,
    safetyScore,
    walkabilityScore,
    weatherSummary,
    timezone,
    updatedAt.toEpochMilli()
)

private fun NomadPlaceEntity.toDomain() = NomadPlaceInfo(
    placeId,
    city,
    country,
    costOfLivingUsd,
    internetMbps,
    safetyScore,
    walkabilityScore,
    weatherSummary,
    timezone,
    Instant.ofEpochMilli(updatedAtEpochMillis)
)
