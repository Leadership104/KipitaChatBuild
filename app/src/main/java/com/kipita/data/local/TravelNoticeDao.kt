package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TravelNoticeDao {
    @Query("SELECT * FROM travel_notices WHERE category = :category")
    suspend fun findByCategory(category: String): List<TravelNoticeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notices: List<TravelNoticeEntity>)
}
