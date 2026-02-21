package com.kipita.data.repository

import com.kipita.data.local.DirectMessageDao
import com.kipita.data.local.DirectMessageEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class OfflineMessagingRepository(private val dao: DirectMessageDao) {

    fun observeMessages(conversationId: String): Flow<List<DirectMessageEntity>> =
        dao.observeMessages(conversationId)

    suspend fun getMessages(conversationId: String): List<DirectMessageEntity> =
        dao.getMessages(conversationId)

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        content: String,
        isOffline: Boolean = true
    ): DirectMessageEntity {
        val msg = DirectMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            createdAtEpochMillis = System.currentTimeMillis(),
            isOffline = isOffline
        )
        dao.upsert(msg)
        return msg
    }

    suspend fun markRead(conversationId: String) = dao.markAllRead(conversationId)

    suspend fun getUnreadCount(conversationId: String): Int = dao.getUnreadCount(conversationId)

    suspend fun getPendingSync(): List<DirectMessageEntity> = dao.getPendingSyncMessages()

    suspend fun markSynced(messageId: String) = dao.markSynced(messageId)

    // Pre-populate with sample community messages for demo
    suspend fun seedSampleMessages(conversationId: String) {
        val existing = dao.getMessages(conversationId)
        if (existing.isNotEmpty()) return

        val samples = listOf(
            DirectMessageEntity(
                id = "seed-1-$conversationId",
                conversationId = conversationId,
                senderId = "user-alex",
                senderName = "Alex Chen",
                content = "Anyone know good coworking spots near Shibuya? Looking for fast WiFi",
                createdAtEpochMillis = System.currentTimeMillis() - 3600_000L * 2,
                isRead = true
            ),
            DirectMessageEntity(
                id = "seed-2-$conversationId",
                conversationId = conversationId,
                senderId = "user-sofia",
                senderName = "Sofia M.",
                content = "Wework Shibuya is solid — 200 Mbps and great coffee. ~¥3000/day",
                createdAtEpochMillis = System.currentTimeMillis() - 3600_000L,
                isRead = true
            ),
            DirectMessageEntity(
                id = "seed-3-$conversationId",
                conversationId = conversationId,
                senderId = "user-james",
                senderName = "James Park",
                content = "Also Fab Cafe in Shibuya has great vibe and accepts BTC",
                createdAtEpochMillis = System.currentTimeMillis() - 1800_000L,
                isRead = true
            )
        )
        dao.upsertAll(samples)
    }
}
