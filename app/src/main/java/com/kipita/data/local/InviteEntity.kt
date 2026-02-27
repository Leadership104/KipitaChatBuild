package com.kipita.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invites",
    indices = [
        Index("senderUserId"),
        Index("recipientEmailNormalized"),
        Index("status"),
        Index(value = ["senderUserId", "recipientEmailNormalized", "contextType", "contextId"], unique = true)
    ]
)
data class InviteEntity(
    @PrimaryKey val id: String,
    val senderUserId: String,
    val recipientEmail: String,
    val recipientEmailNormalized: String,
    val recipientUserId: String? = null,
    val contextType: String, // DIRECT_MESSAGE | TRIP | GROUP
    val contextId: String,
    val status: String = "PENDING", // PENDING | ACCEPTED | REJECTED
    val createdAtEpochMillis: Long,
    val respondedAtEpochMillis: Long? = null
)
