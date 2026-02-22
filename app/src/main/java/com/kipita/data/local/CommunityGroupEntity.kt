package com.kipita.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "community_groups",
    indices = [Index("ownerUserId")]
)
data class CommunityGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val ownerUserId: String,
    val createdAtEpochMillis: Long,
    val isPrivate: Boolean = false
)
