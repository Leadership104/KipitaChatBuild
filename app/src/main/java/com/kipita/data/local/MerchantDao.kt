package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchant_locations")
    suspend fun getAll(): List<MerchantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(merchants: List<MerchantEntity>)
}
