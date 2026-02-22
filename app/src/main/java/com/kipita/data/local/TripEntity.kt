package com.kipita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for user-created trips.
 * itineraryJson / travelersJson / inviteListJson are JSON-encoded arrays so that
 * the schema stays simple — a single table covers the full trip object.
 *
 * API_READY: Replace Room calls in TripRepository with REST calls pointing to your
 * production backend; zero UI changes required.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val title: String,
    val destination: String,
    val country: String,
    val countryFlag: String,

    // Dates stored as LocalDate.toEpochDay() longs for easy querying
    val startDateEpoch: Long,
    val endDateEpoch: Long,

    // Weather
    val weatherHighC: Int = 25,
    val weatherLowC: Int = 15,
    val weatherIcon: String = "🌤",

    // Notes — plain text, user-editable
    val notesText: String = "",

    // JSON arrays — itinerary items, travelers, invitees
    // Format for itinerary: see TripDetailScreen for the parse logic
    val itineraryJson: String = "[]",
    val travelersJson: String = """["Me"]""",
    val inviteListJson: String = "[]",

    // Logistics
    val flightNumber: String = "",
    val hotelName: String = "",
    val hotelConfirmation: String = "",

    // Meta
    val createdBy: String = "",
    val isAiGenerated: Boolean = false,

    // Lifecycle status: "UPCOMING" | "ACTIVE" | "PAST"
    val status: String = "UPCOMING"
)
