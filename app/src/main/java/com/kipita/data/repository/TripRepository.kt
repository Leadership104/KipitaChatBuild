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

    /** Adds a single invitee email/username and persists. */
    suspend fun inviteUser(tripId: String, emailOrUsername: String) {
        try {
            val trip = dao.getById(tripId) ?: return
            val arr = runCatching { JSONArray(trip.inviteListJson) }.getOrElse { JSONArray() }
            arr.put(emailOrUsername.trim())
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
     * Seeds Room from SampleData on first launch so the app is never blank.
     * API_READY: Remove this function once production data is connected.
     */
    suspend fun seedIfEmpty() {
        try {
            if (dao.count() > 0) return

            val tokyoItinerary = buildTokyoItinerary()
            val baliItinerary  = buildBaliItinerary()

            SampleData.upcomingTrips.forEachIndexed { i, trip ->
                dao.upsert(
                    TripEntity(
                        id = trip.id,
                        title = trip.title,
                        destination = trip.destination,
                        country = trip.country,
                        countryFlag = trip.countryFlag,
                        startDateEpoch = trip.startDate.toEpochDay(),
                        endDateEpoch   = trip.endDate.toEpochDay(),
                        weatherHighC   = trip.weatherHighC,
                        weatherLowC    = trip.weatherLowC,
                        weatherIcon    = trip.weatherIcon,
                        status         = "UPCOMING",
                        travelersJson  = """["Me"]""",
                        flightNumber   = if (i == 0) "JL 061" else "GA 406",
                        hotelName      = if (i == 0) "Park Hyatt Tokyo" else "COMO Uma Ubud",
                        itineraryJson  = if (i == 0) tokyoItinerary else baliItinerary,
                        isAiGenerated  = false
                    )
                )
            }

            SampleData.pastTrips.forEach { trip ->
                dao.upsert(
                    TripEntity(
                        id = trip.id,
                        title = trip.title,
                        destination = trip.destination,
                        country = trip.country,
                        countryFlag = trip.countryFlag,
                        startDateEpoch = trip.startDate.toEpochDay(),
                        endDateEpoch   = trip.endDate.toEpochDay(),
                        weatherHighC   = trip.weatherHighC,
                        weatherLowC    = trip.weatherLowC,
                        weatherIcon    = trip.weatherIcon,
                        status         = "PAST",
                        travelersJson  = """["Me"]""",
                        isAiGenerated  = false
                    )
                )
            }
        } catch (e: Exception) {
            logger.log("TripRepository.seedIfEmpty", e)
        }
    }

    // ── Sample itinerary helpers ──────────────────────────────────────────────

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

    private fun buildBaliItinerary() = """
[
  {"day":1,"label":"Arrival & Chill","items":[
    {"time":"13:00","emoji":"✈️","title":"Arrive at Ngurah Rai Airport (DPS)","desc":"Collect luggage, grab a GrabCar to Ubud.","loc":"Ngurah Rai International Airport"},
    {"time":"16:00","emoji":"🏨","title":"Villa Check-in","desc":"COMO Uma Ubud. Infinity pool with jungle views.","loc":"Ubud, Bali"},
    {"time":"19:00","emoji":"🌅","title":"Sunset at Tegallalang Rice Terraces","desc":"Iconic terraced rice paddies. Golden hour magic.","loc":"Tegallalang, Ubud"},
    {"time":"21:00","emoji":"🍹","title":"Dinner at Locavore","desc":"Award-winning farm-to-table Balinese cuisine.","loc":"Ubud"}
  ]},
  {"day":2,"label":"Temples & Surf","items":[
    {"time":"06:00","emoji":"🌄","title":"Sunrise at Mount Batur","desc":"3-hour guided sunrise trek. Bring warm clothes.","loc":"Kintamani, Bali"},
    {"time":"11:00","emoji":"⛩","title":"Tanah Lot Sea Temple","desc":"Dramatic sea temple at high tide. Iconic Bali shot.","loc":"Tabanan"},
    {"time":"15:00","emoji":"🏄","title":"Surf Lesson at Kuta Beach","desc":"Beginner-friendly waves. Boards and instructor included.","loc":"Kuta Beach"},
    {"time":"20:00","emoji":"🥥","title":"BBQ Dinner on the Beach","desc":"Fresh seafood BBQ with live gamelan music.","loc":"Seminyak Beach"}
  ]}
]
""".trimIndent()
}
