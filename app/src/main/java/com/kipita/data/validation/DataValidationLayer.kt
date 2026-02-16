package com.kipita.data.validation

import com.kipita.data.api.TravelNoticeDto
import com.kipita.domain.model.LatLng
import com.kipita.domain.model.NoticeCategory
import com.kipita.domain.model.SeverityLevel
import com.kipita.domain.model.TravelNotice
import java.time.Instant

class DataValidationLayer(
    private val sourceVerificationLayer: SourceVerificationLayer
) {
    fun normalize(dto: TravelNoticeDto): TravelNotice? {
        if (dto.title.isBlank() || dto.description.isBlank()) return null
        val category = runCatching { NoticeCategory.valueOf(dto.category.uppercase()) }.getOrNull() ?: return null
        val severity = runCatching { SeverityLevel.valueOf(dto.severity.uppercase()) }.getOrNull() ?: SeverityLevel.MEDIUM
        val verified = sourceVerificationLayer.isVerifiedSource(dto.sourceUrl)

        return TravelNotice(
            title = dto.title.trim(),
            description = dto.description.trim(),
            location = LatLng(dto.latitude, dto.longitude),
            category = category,
            severity = severity,
            sourceName = dto.sourceName,
            sourceUrl = dto.sourceUrl,
            verified = verified,
            lastUpdated = Instant.ofEpochMilli(dto.lastUpdatedEpochMillis)
        )
    }
}
