package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TripMessageDao {
    @Query("SELECT * FROM trip_messages WHERE tripId = :tripId ORDER BY createdAtEpochMillis ASC")
    suspend fun getByTrip(tripId: String): List<TripMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: TripMessageEntity)
}
