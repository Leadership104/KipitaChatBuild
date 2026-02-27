package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * A place the user has saved/favorited from the Explore screen.
 * Backed by the Room `saved_locations` table (DB v10+).
 */
@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey val id: String,          // Google Places placeId
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val priceLevel: String,
    val types: String,                   // comma-separated place types
    val photoUrl: String,
    val savedAtEpochMillis: Long
)

@Dao
interface SavedLocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY savedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations ORDER BY savedAtEpochMillis DESC")
    suspend fun getAll(): List<SavedLocationEntity>

    @Query("SELECT COUNT(*) FROM saved_locations WHERE id = :placeId")
    suspend fun isSaved(placeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(location: SavedLocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :placeId")
    suspend fun remove(placeId: String)

    @Delete
    suspend fun delete(location: SavedLocationEntity)
}
