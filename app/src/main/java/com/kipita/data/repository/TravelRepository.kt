package com.kipita.data.repository

import com.kipita.data.api.GovernmentApiService
import com.kipita.data.local.TravelNoticeDao
import com.kipita.data.local.TravelNoticeEntity
import com.kipita.data.validation.DataValidationLayer
import com.kipita.domain.model.NoticeCategory
import com.kipita.domain.model.TravelNotice
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface TravelRepository {
    suspend fun fetch(region: String): List<TravelNotice>
}

class SafetyRepository(
    private val apiService: GovernmentApiService,
    private val dao: TravelNoticeDao,
    private val validator: DataValidationLayer
) : TravelRepository {
    override suspend fun fetch(region: String): List<TravelNotice> {
        return fetchCategory(region, NoticeCategory.SAFETY) { apiService.getSafetyNotices(region) }
    }

    private suspend fun fetchCategory(
        region: String,
        category: NoticeCategory,
        remoteFetch: suspend () -> List<com.kipita.data.api.TravelNoticeDto>
    ): List<TravelNotice> = withContext(Dispatchers.IO) {
        val notices = runCatching { remoteFetch().mapNotNull(validator::normalize) }
            .getOrElse {
                dao.findByCategory(category.name).map(TravelNoticeEntity::toDomain)
            }
        dao.upsertAll(notices.map { it.toEntity(category) })
        notices
    }
}

class HealthRepository(
    private val apiService: GovernmentApiService,
    private val dao: TravelNoticeDao,
    private val validator: DataValidationLayer
) : TravelRepository {
    override suspend fun fetch(region: String): List<TravelNotice> = withContext(Dispatchers.IO) {
        runCatching {
            apiService.getHealthNotices(region).mapNotNull(validator::normalize)
        }.getOrElse { dao.findByCategory(NoticeCategory.HEALTH.name).map(TravelNoticeEntity::toDomain) }
    }
}

class AdvisoryRepository(
    private val apiService: GovernmentApiService,
    private val dao: TravelNoticeDao,
    private val validator: DataValidationLayer
) : TravelRepository {
    override suspend fun fetch(region: String): List<TravelNotice> = withContext(Dispatchers.IO) {
        runCatching {
            apiService.getAdvisories(region).mapNotNull(validator::normalize)
        }.getOrElse { dao.findByCategory(NoticeCategory.ADVISORY.name).map(TravelNoticeEntity::toDomain) }
    }
}

private fun TravelNotice.toEntity(category: NoticeCategory): TravelNoticeEntity = TravelNoticeEntity(
    id = "$category-${UUID.randomUUID()}",
    title = title,
    description = description,
    latitude = location.latitude,
    longitude = location.longitude,
    category = category.name,
    severity = severity.name,
    sourceName = sourceName,
    sourceUrl = sourceUrl,
    verified = verified,
    lastUpdatedEpochMillis = lastUpdated.toEpochMilli(),
    retrievedAtEpochMillis = Instant.now().toEpochMilli()
)

private fun TravelNoticeEntity.toDomain(): TravelNotice = TravelNotice(
    title = title,
    description = description,
    location = com.kipita.domain.model.LatLng(latitude, longitude),
    category = NoticeCategory.valueOf(category),
    severity = com.kipita.domain.model.SeverityLevel.valueOf(severity),
    sourceName = sourceName,
    sourceUrl = sourceUrl,
    verified = verified,
    lastUpdated = Instant.ofEpochMilli(lastUpdatedEpochMillis)
)
