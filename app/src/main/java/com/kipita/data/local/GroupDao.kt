package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroup(group: CommunityGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMembership(membership: GroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMemberships(memberships: List<GroupMemberEntity>)

    @Query("SELECT * FROM community_groups ORDER BY createdAtEpochMillis DESC")
    fun observeGroups(): Flow<List<CommunityGroupEntity>>

    @Query(
        """
        SELECT g.* FROM community_groups g
        INNER JOIN group_members m ON m.groupId = g.id
        WHERE m.userId = :userId
        ORDER BY g.createdAtEpochMillis DESC
        """
    )
    fun observeGroupsForUser(userId: String): Flow<List<CommunityGroupEntity>>

    @Query("SELECT userId FROM group_members WHERE groupId = :groupId")
    suspend fun getMemberIds(groupId: String): List<String>

    @Transaction
    suspend fun replaceGroupMembers(groupId: String, members: List<GroupMemberEntity>) {
        deleteGroupMembers(groupId)
        if (members.isNotEmpty()) upsertMemberships(members)
    }

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteGroupMembers(groupId: String)
}
