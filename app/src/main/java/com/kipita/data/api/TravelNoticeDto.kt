package com.kipita.data.api

data class TravelNoticeDto(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val severity: String,
    val sourceName: String,
    val sourceUrl: String,
    val lastUpdatedEpochMillis: Long
)
