package com.kipita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_notices")
data class TravelNoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val severity: String,
    val sourceName: String,
    val sourceUrl: String,
    val verified: Boolean,
    val lastUpdatedEpochMillis: Long,
    val retrievedAtEpochMillis: Long
)
