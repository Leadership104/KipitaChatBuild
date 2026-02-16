package com.kipita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nomad_places")
data class NomadPlaceEntity(
    @PrimaryKey val placeId: String,
    val city: String,
    val country: String,
    val costOfLivingUsd: Double,
    val internetMbps: Double,
    val safetyScore: Double,
    val walkabilityScore: Double,
    val weatherSummary: String,
    val timezone: String,
    val updatedAtEpochMillis: Long
)
