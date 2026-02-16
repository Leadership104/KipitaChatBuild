package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NomadPlaceDao {
    @Query("SELECT * FROM nomad_places")
    suspend fun getAll(): List<NomadPlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<NomadPlaceEntity>)
}
