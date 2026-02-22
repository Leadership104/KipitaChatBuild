package com.kipita.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "userId"],
    indices = [Index("groupId"), Index("userId")]
)
data class GroupMemberEntity(
    val groupId: String,
    val userId: String,
    val role: String = "member", // owner, admin, member
    val joinedAtEpochMillis: Long
)
