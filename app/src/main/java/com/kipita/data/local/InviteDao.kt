package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InviteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(invite: InviteEntity)

    @Query("SELECT * FROM invites WHERE senderUserId = :senderUserId ORDER BY createdAtEpochMillis DESC")
    fun observeSent(senderUserId: String): Flow<List<InviteEntity>>

    @Query(
        """
        SELECT * FROM invites
        WHERE recipientEmailNormalized = :recipientEmailNormalized
        ORDER BY createdAtEpochMillis DESC
        """
    )
    fun observeReceived(recipientEmailNormalized: String): Flow<List<InviteEntity>>

    @Query(
        """
        UPDATE invites
        SET status = 'ACCEPTED',
            recipientUserId = :recipientUserId,
            respondedAtEpochMillis = :respondedAtEpochMillis
        WHERE id = :inviteId
        """
    )
    suspend fun acceptInvite(inviteId: String, recipientUserId: String, respondedAtEpochMillis: Long)

    @Query(
        """
        SELECT COUNT(*) FROM invites
        WHERE contextType = 'DIRECT_MESSAGE'
          AND status = 'ACCEPTED'
          AND (
            (senderUserId = :userA AND recipientUserId = :userB) OR
            (senderUserId = :userB AND recipientUserId = :userA)
          )
        """
    )
    suspend fun acceptedDirectPermissionCount(userA: String, userB: String): Int
}
