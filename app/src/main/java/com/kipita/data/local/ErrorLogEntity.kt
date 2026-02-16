package com.kipita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey val id: String,
    val tag: String,
    val message: String,
    val stackTrace: String,
    val createdAtEpochMillis: Long,
    val sent: Boolean
)
