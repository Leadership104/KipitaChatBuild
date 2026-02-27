package com.kipita.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["usernameNormalized"], unique = true),
        Index(value = ["emailNormalized"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val username: String,
    val usernameNormalized: String,
    val avatarUrl: String = "",
    val email: String = "",
    val emailNormalized: String? = null,
    val emailVerified: Boolean = false,
    val passwordHash: String = "",
    val createdAtEpochMillis: Long
)
