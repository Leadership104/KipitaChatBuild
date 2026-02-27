package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE usernameNormalized = :usernameNormalized LIMIT 1")
    suspend fun getByUsername(usernameNormalized: String): UserEntity?

    @Query("SELECT * FROM users WHERE emailNormalized = :emailNormalized LIMIT 1")
    suspend fun getByEmail(emailNormalized: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE usernameNormalized = :usernameNormalized")
    suspend fun usernameCount(usernameNormalized: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE emailNormalized = :emailNormalized")
    suspend fun emailCount(emailNormalized: String): Int

    @Query("SELECT * FROM users ORDER BY displayName ASC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
