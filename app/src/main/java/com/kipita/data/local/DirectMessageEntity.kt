package com.kipita.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "direct_messages",
    indices = [Index("conversationId"), Index("createdAtEpochMillis")]
)
data class DirectMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,   // userId for DMs, groupId for communities
    val senderId: String,
    val senderName: String,
    val senderAvatar: String = "",
    val content: String,
    val createdAtEpochMillis: Long,
    val isRead: Boolean = false,
    val isOffline: Boolean = false, // true = stored offline, not yet synced
    val attachmentUrl: String = "",
    val messageType: String = "text" // text, image, location
)
