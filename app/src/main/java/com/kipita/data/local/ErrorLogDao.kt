package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ErrorLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ErrorLogEntity)

    @Query("SELECT * FROM error_logs ORDER BY createdAtEpochMillis DESC")
    suspend fun getAll(): List<ErrorLogEntity>

    @Query("SELECT * FROM error_logs WHERE sent = 0 ORDER BY createdAtEpochMillis ASC")
    suspend fun getUnsent(): List<ErrorLogEntity>

    @Query("UPDATE error_logs SET sent = 1 WHERE id IN (:ids)")
    suspend fun markSent(ids: List<String>)
}
