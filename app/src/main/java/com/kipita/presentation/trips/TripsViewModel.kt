package com.kipita.presentation.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.TripEntity
import com.kipita.data.repository.TripRepository
import com.kipita.domain.model.parsedInvites
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repo: TripRepository,
    private val logger: InHouseErrorLogger
) : ViewModel() {

    data class TripsUiState(
        val upcomingTrips: List<TripEntity> = emptyList(),
        val pastTrips: List<TripEntity> = emptyList(),
        val cancelledTrips: List<TripEntity> = emptyList(),
        val isLoading: Boolean = true,
        val isOffline: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(TripsUiState())
    val state: StateFlow<TripsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Ensure sample data is present on first run
            repo.seedIfEmpty()
            // Age-out any trips whose end date has passed
            repo.tickExpiredTrips()
            // Subscribe to live streams
            launch {
                repo.upcomingTrips().collect { trips ->
                    _state.update { it.copy(upcomingTrips = trips, isLoading = false) }
                }
            }
            launch {
                repo.pastTrips().collect { trips ->
                    _state.update { it.copy(pastTrips = trips) }
                }
            }
            launch {
                repo.cancelledTrips().collect { trips ->
                    _state.update { it.copy(cancelledTrips = trips) }
                }
            }
        }
    }

    /**
     * Manual Mode: saves a fully-specified trip to Room.
     * Returns the new tripId so the caller can immediately open TripDetailScreen.
     */
    fun createManualTrip(
        destination: String,
        country: String,
        countryFlag: String,
        startDate: LocalDate,
        endDate: LocalDate,
        flightNumber: String,
        hotelName: String,
        hotelConfirmation: String,
        notes: String,
        onCreated: (tripId: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val id = UUID.randomUUID().toString()
                val entity = TripEntity(
                    id                = id,
                    title             = "$destination Trip",
                    destination       = destination,
                    country           = country,
                    countryFlag       = countryFlag,
                    startDateEpoch    = startDate.toEpochDay(),
                    endDateEpoch      = endDate.toEpochDay(),
                    flightNumber      = flightNumber,
                    hotelName         = hotelName,
                    hotelConfirmation = hotelConfirmation,
                    notesText         = notes,
                    travelersJson     = """["Me"]""",
                    status            = if (startDate.isAfter(LocalDate.now())) "UPCOMING" else "ACTIVE",
                    isAiGenerated     = false
                )
                repo.saveTrip(entity)
                repo.deleteSampleTripsIfUserHasRealTrip()
                onCreated(id)
            } catch (e: Exception) {
                logger.log("TripsViewModel.createManualTrip", e)
                _state.update { it.copy(error = "Could not save trip. Please try again.") }
            }
        }
    }

    /**
     * AI Mode: saves an AI-generated trip skeleton so it lands in Upcoming Trips.
     * The itinerary can be populated later via TripDetailViewModel.
     */
    fun acceptAiTrip(
        destination: String,
        country: String,
        countryFlag: String,
        durationDays: Int,
        aiSummary: String,
        onCreated: (tripId: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val id = UUID.randomUUID().toString()
                val entity = TripEntity(
                    id            = id,
                    title         = "$destination Trip (AI Planned)",
                    destination   = destination,
                    country       = country,
                    countryFlag   = countryFlag,
                    startDateEpoch = today.plusDays(14).toEpochDay(),
                    endDateEpoch   = today.plusDays((14 + durationDays - 1).toLong()).toEpochDay(),
                    notesText     = aiSummary,
                    travelersJson = """["Me"]""",
                    status        = "UPCOMING",
                    isAiGenerated = true
                )
                repo.saveTrip(entity)
                repo.deleteSampleTripsIfUserHasRealTrip()
                onCreated(id)
            } catch (e: Exception) {
                logger.log("TripsViewModel.acceptAiTrip", e)
                _state.update { it.copy(error = "Could not save AI trip. Please try again.") }
            }
        }
    }

    /**
     * Marks a trip as CANCELLED. The caller is responsible for showing the
     * confirmation dialog before calling this.
     *
     * @param onCancelled Called with the list of invite emails so the UI can
     *                    fire a mailto Intent notifying group members.
     */
    fun cancelTrip(
        tripId: String,
        reason: String = "",
        onCancelled: (inviteEmails: List<String>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val trip = repo.getTripById(tripId)
                val emails = trip?.parsedInvites() ?: emptyList()
                repo.cancelTrip(tripId, reason)
                onCancelled(emails)
            } catch (e: Exception) {
                logger.log("TripsViewModel.cancelTrip", e)
                _state.update { it.copy(error = "Could not cancel trip. Please try again.") }
            }
        }
    }

    /**
     * Recreates a cancelled trip as a new UPCOMING trip, cloning all details
     * except status/cancellation fields. Used from the Cancelled Trips section.
     */
    fun recreateTrip(
        trip: TripEntity,
        onCreated: (tripId: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val newId = UUID.randomUUID().toString()
                val today = LocalDate.now()
                val newTrip = trip.copy(
                    id                  = newId,
                    status              = "UPCOMING",
                    cancelledAt         = 0L,
                    cancellationReason  = "",
                    isSample            = false,
                    startDateEpoch      = today.plusDays(14).toEpochDay(),
                    endDateEpoch        = today.plusDays(14 + (trip.endDateEpoch - trip.startDateEpoch)).toEpochDay()
                )
                repo.saveTrip(newTrip)
                onCreated(newId)
            } catch (e: Exception) {
                logger.log("TripsViewModel.recreateTrip", e)
                _state.update { it.copy(error = "Could not recreate trip. Please try again.") }
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
}
