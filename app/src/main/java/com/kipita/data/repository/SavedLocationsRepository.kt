package com.kipita.data.repository

import com.kipita.data.local.SavedLocationDao
import com.kipita.data.local.SavedLocationEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedLocationsRepository @Inject constructor(
    private val dao: SavedLocationDao
) {
    fun observeAll(): Flow<List<SavedLocationEntity>> = dao.observeAll()

    suspend fun getAll(): List<SavedLocationEntity> = dao.getAll()

    suspend fun isSaved(placeId: String): Boolean = dao.isSaved(placeId) > 0

    suspend fun save(
        id: String,
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        rating: Double,
        priceLevel: String,
        types: List<String>,
        photoUrl: String
    ) {
        dao.save(
            SavedLocationEntity(
                id = id,
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude,
                rating = rating,
                priceLevel = priceLevel,
                types = types.joinToString(","),
                photoUrl = photoUrl,
                savedAtEpochMillis = Instant.now().toEpochMilli()
            )
        )
    }

    suspend fun remove(placeId: String) = dao.remove(placeId)

    suspend fun toggle(
        id: String,
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        rating: Double,
        priceLevel: String,
        types: List<String>,
        photoUrl: String
    ): Boolean {
        return if (isSaved(id)) {
            remove(id)
            false
        } else {
            save(id, name, address, latitude, longitude, rating, priceLevel, types, photoUrl)
            true
        }
    }
}
