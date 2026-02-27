package com.kipita.data.repository

import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.TripDao
import com.kipita.data.local.TripEntity
import com.kipita.domain.model.SampleData
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central Data Manager for all trip persistence.
 *
 * All UI screens source trip data exclusively through this class.
 * API_READY: Replace each suspend fun body with a REST/GraphQL call;
 * the Flow contracts and method signatures stay the same — zero UI changes needed.
 */
@Singleton
class TripRepository @Inject constructor(
    private val dao: TripDao,
    private val logger: InHouseErrorLogger
) {
    // ── Live streams (UI observes these) ──────────────────────────────────────

    fun upcomingTrips(): Flow<List<TripEntity>> = dao.observeUpcomingTrips()

    fun pastTrips(): Flow<List<TripEntity>> = dao.observePastTrips()

    // ── Point read ────────────────────────────────────────────────────────────

    suspend fun getTripById(id: String): TripEntity? = try {
        dao.getById(id)
    } catch (e: Exception) {
        logger.log("TripRepository.getTripById", e); null
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun saveTrip(trip: TripEntity) = try {
        dao.upsert(trip)
    } catch (e: Exception) {
        logger.log("TripRepository.saveTrip", e)
    }

    suspend fun deleteTrip(trip: TripEntity) = try {
        dao.delete(trip)
    } catch (e: Exception) {
        logger.log("TripRepository.deleteTrip", e)
    }

    suspend fun updateNotes(tripId: String, notes: String) = try {
        dao.updateNotes(tripId, notes)
    } catch (e: Exception) {
        logger.log("TripRepository.updateNotes", e)
    }

    suspend fun updateItinerary(tripId: String, itineraryJson: String) = try {
        dao.updateItinerary(tripId, itineraryJson)
    } catch (e: Exception) {
        logger.log("TripRepository.updateItinerary", e)
    }

    /** Adds a single invitee email and persists. Messaging unlocks after invite acceptance. */
    suspend fun inviteUser(tripId: String, inviteEmail: String) {
        try {
            val normalizedEmail = inviteEmail.trim().lowercase()
            if (!normalizedEmail.contains("@")) return
            val trip = dao.getById(tripId) ?: return
            val arr = runCatching { JSONArray(trip.inviteListJson) }.getOrElse { JSONArray() }
            val existing = (0 until arr.length()).map { arr.getString(it).lowercase() }.toSet()
            if (normalizedEmail in existing) return
            arr.put(normalizedEmail)
            dao.updateInvites(tripId, arr.toString())
        } catch (e: Exception) {
            logger.log("TripRepository.inviteUser", e)
        }
    }

    // ── Maintenance ───────────────────────────────────────────────────────────

    /** Promotes any trip whose end date has passed to PAST status. */
    suspend fun tickExpiredTrips() = try {
        dao.markExpiredTripsAsPast(LocalDate.now().toEpochDay())
    } catch (e: Exception) {
        logger.log("TripRepository.tickExpiredTrips", e)
    }

    /**
     * Seeds a single example/onboarding trip on first launch so the app is
     * never blank. Marked with isSample=true so it can be removed once the
     * user confirms their first real trip.
     * API_READY: Remove once production data is connected.
     */
    suspend fun seedIfEmpty() {
        try {
            if (dao.count() > 0) return
            val sample = SampleData.upcomingTrips.firstOrNull() ?: return
            dao.upsert(
                TripEntity(
                    id            = sample.id,
                    title         = "📍 Example Trip — Tokyo",
                    destination   = sample.destination,
                    country       = sample.country,
                    countryFlag   = sample.countryFlag,
                    startDateEpoch = sample.startDate.toEpochDay(),
                    endDateEpoch   = sample.endDate.toEpochDay(),
                    weatherHighC   = sample.weatherHighC,
                    weatherLowC    = sample.weatherLowC,
                    weatherIcon    = sample.weatherIcon,
                    status         = "UPCOMING",
                    travelersJson  = """["Me"]""",
                    flightNumber   = "JL 061",
                    hotelName      = "Park Hyatt Tokyo",
                    itineraryJson  = buildTokyoItinerary(),
                    notesText      = "This is an example trip to show you how Kipita works.\n" +
                                     "Tap any day to expand activities. Use 'Plan with AI' to\n" +
                                     "generate your own itinerary. Tap + to create your first trip.",
                    isAiGenerated  = false,
                    isSample       = true
                )
            )
        } catch (e: Exception) {
            logger.log("TripRepository.seedIfEmpty", e)
        }
    }

    /** Removes the onboarding sample trip once the user saves their first real trip. */
    suspend fun deleteSampleTripsIfUserHasRealTrip() {
        try {
            if (dao.countRealTrips() > 0) dao.deleteSampleTrips()
        } catch (e: Exception) {
            logger.log("TripRepository.deleteSampleTrips", e)
        }
    }

    // ── Sample itinerary builder ─────────────────────────────────────────────

    private fun buildTokyoItinerary() = """
[
  {"day":1,"label":"Arrival Day","items":[
    {"time":"15:00","emoji":"✈️","title":"Arrive at Narita Airport (NRT)","desc":"Collect luggage, clear customs. Pick up pocket Wi-Fi.","loc":"Narita International Airport"},
    {"time":"17:30","emoji":"🚅","title":"Narita Express → Shinjuku","desc":"Take the N'EX express train. 90-minute scenic ride.","loc":"Narita → Shinjuku"},
    {"time":"19:30","emoji":"🏨","title":"Hotel Check-in","desc":"Park Hyatt Tokyo. Freshen up and enjoy the city-view lobby.","loc":"Shinjuku, Tokyo"},
    {"time":"21:00","emoji":"🍜","title":"Ramen at Ichiran Shinjuku","desc":"Solo ramen booths — an iconic Tokyo experience.","loc":"Shinjuku"}
  ]},
  {"day":2,"label":"Culture & Food","items":[
    {"time":"09:00","emoji":"⛩","title":"Senso-ji Temple","desc":"Tokyo's oldest temple. Visit early to beat the crowds.","loc":"Asakusa"},
    {"time":"12:00","emoji":"🍣","title":"Lunch at Tsukiji Outer Market","desc":"Fresh sushi and tamagoyaki street food.","loc":"Tsukiji"},
    {"time":"15:00","emoji":"💻","title":"Co-working: WeWork Roppongi","desc":"Fast fiber internet. Great networking spot for nomads.","loc":"Roppongi Hills"},
    {"time":"19:00","emoji":"🗼","title":"Tokyo Tower at Sunset","desc":"Panoramic city views. Spectacular after dark.","loc":"Minato, Tokyo"}
  ]},
  {"day":3,"label":"Tech & Wellness","items":[
    {"time":"10:00","emoji":"🤖","title":"teamLab Borderless","desc":"Immersive digital art — book tickets in advance!","loc":"Odaiba"},
    {"time":"14:00","emoji":"🎮","title":"Akihabara Electronics District","desc":"Tech shops, anime, gadgets, retro arcades.","loc":"Akihabara"},
    {"time":"19:00","emoji":"♨️","title":"Onsen: Oedo Onsen Monogatari","desc":"Traditional Japanese onsen. Bring a towel.","loc":"Odaiba"}
  ]}
]
""".trimIndent()

}
