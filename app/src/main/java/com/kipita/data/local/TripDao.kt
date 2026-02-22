package com.kipita.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    // ── Live streams ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM trips WHERE status != 'PAST' ORDER BY startDateEpoch ASC")
    fun observeUpcomingTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status = 'PAST' ORDER BY startDateEpoch DESC")
    fun observePastTrips(): Flow<List<TripEntity>>

    // ── Point lookups ─────────────────────────────────────────────────────────

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getById(tripId: String): TripEntity?

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun count(): Int

    // ── Writes ────────────────────────────────────────────────────────────────

    @Upsert
    suspend fun upsert(trip: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)

    // ── Targeted field updates ────────────────────────────────────────────────

    @Query("UPDATE trips SET notesText = :notes WHERE id = :tripId")
    suspend fun updateNotes(tripId: String, notes: String)

    @Query("UPDATE trips SET itineraryJson = :json WHERE id = :tripId")
    suspend fun updateItinerary(tripId: String, json: String)

    @Query("UPDATE trips SET inviteListJson = :json WHERE id = :tripId")
    suspend fun updateInvites(tripId: String, json: String)

    @Query("UPDATE trips SET travelersJson = :json WHERE id = :tripId")
    suspend fun updateTravelers(tripId: String, json: String)

    // ── Maintenance ───────────────────────────────────────────────────────────

    @Query(
        "UPDATE trips SET status = 'PAST' " +
        "WHERE endDateEpoch < :todayEpoch AND status != 'PAST'"
    )
    suspend fun markExpiredTripsAsPast(todayEpoch: Long)
}
