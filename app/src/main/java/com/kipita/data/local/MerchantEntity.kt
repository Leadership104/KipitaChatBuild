package com.kipita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_locations")
data class MerchantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val acceptsOnchainBtc: Boolean,
    val acceptsLightning: Boolean,
    val acceptsCashApp: Boolean,
    val source: String,
    val lastVerifiedEpochMillis: Long,
    val metadataJson: String
)
