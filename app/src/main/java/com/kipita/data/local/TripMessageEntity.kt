package com.kipita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_messages")
data class TripMessageEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val createdAtEpochMillis: Long,
    val isAi: Boolean
)
