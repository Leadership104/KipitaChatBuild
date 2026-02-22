package com.kipita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val avatarUrl: String = "",
    val email: String = "",
    val createdAtEpochMillis: Long
)
