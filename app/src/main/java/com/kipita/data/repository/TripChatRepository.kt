package com.kipita.data.repository

import com.kipita.data.local.TripMessageDao
import com.kipita.data.local.TripMessageEntity
import com.kipita.domain.model.TripMessage
import java.time.Instant
import java.util.UUID

class TripChatRepository(private val dao: TripMessageDao) {
    suspend fun getMessages(tripId: String): List<TripMessage> = dao.getByTrip(tripId).map(TripMessageEntity::toDomain)

    suspend fun sendMessage(
        tripId: String,
        senderId: String,
        senderName: String,
        content: String,
        isAi: Boolean = false
    ): TripMessage {
        val message = TripMessage(
            id = UUID.randomUUID().toString(),
            tripId = tripId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            createdAt = Instant.now(),
            isAi = isAi
        )
        dao.upsert(message.toEntity())
        return message
    }

    fun enforceParticipantLimit(participants: List<String>) {
        require(participants.size <= 10) { "Trip chat supports up to 10 people." }
    }
}

private fun TripMessage.toEntity() = TripMessageEntity(id, tripId, senderId, senderName, content, createdAt.toEpochMilli(), isAi)
private fun TripMessageEntity.toDomain() = TripMessage(id, tripId, senderId, senderName, content, Instant.ofEpochMilli(createdAtEpochMillis), isAi)
