package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectMessageDao {
    @Query("SELECT * FROM direct_messages WHERE conversationId = :conversationId ORDER BY createdAtEpochMillis ASC")
    fun observeMessages(conversationId: String): Flow<List<DirectMessageEntity>>

    @Query("SELECT * FROM direct_messages WHERE conversationId = :conversationId ORDER BY createdAtEpochMillis ASC")
    suspend fun getMessages(conversationId: String): List<DirectMessageEntity>

    @Query("SELECT * FROM direct_messages WHERE isOffline = 1 ORDER BY createdAtEpochMillis ASC")
    suspend fun getPendingSyncMessages(): List<DirectMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: DirectMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<DirectMessageEntity>)

    @Query("UPDATE direct_messages SET isRead = 1 WHERE conversationId = :conversationId")
    suspend fun markAllRead(conversationId: String)

    @Query("UPDATE direct_messages SET isOffline = 0 WHERE id = :messageId")
    suspend fun markSynced(messageId: String)

    @Query("SELECT COUNT(*) FROM direct_messages WHERE conversationId = :conversationId AND isRead = 0")
    suspend fun getUnreadCount(conversationId: String): Int

    @Query("""
        SELECT conversationId, MAX(createdAtEpochMillis) as lastTime
        FROM direct_messages
        GROUP BY conversationId
        ORDER BY lastTime DESC
    """)
    suspend fun getConversationIds(): List<ConversationSummary>

    @Query("DELETE FROM direct_messages WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)
}

data class ConversationSummary(
    val conversationId: String,
    val lastTime: Long
)
