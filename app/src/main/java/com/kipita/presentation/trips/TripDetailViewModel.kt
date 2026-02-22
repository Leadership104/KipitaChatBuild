package com.kipita.presentation.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.TripEntity
import com.kipita.data.repository.TripRepository
import com.kipita.domain.model.ItineraryDay
import com.kipita.domain.model.parsedInvites
import com.kipita.domain.model.parsedItinerary
import com.kipita.domain.model.parsedTravelers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repo: TripRepository,
    private val logger: InHouseErrorLogger
) : ViewModel() {

    data class TripDetailUiState(
        val trip: TripEntity? = null,
        val itineraryDays: List<ItineraryDay> = emptyList(),
        val travelers: List<String> = emptyList(),
        val invites: List<String> = emptyList(),
        val notes: String = "",
        val notesSaved: Boolean = false,
        val inviteSent: Boolean = false,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _state = MutableStateFlow(TripDetailUiState())
    val state: StateFlow<TripDetailUiState> = _state.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            try {
                val trip = repo.getTripById(tripId)
                if (trip == null) {
                    _state.update { it.copy(isLoading = false, error = "Trip not found") }
                    return@launch
                }
                _state.update {
                    it.copy(
                        trip          = trip,
                        itineraryDays = trip.parsedItinerary(),
                        travelers     = trip.parsedTravelers(),
                        invites       = trip.parsedInvites(),
                        notes         = trip.notesText,
                        isLoading     = false
                    )
                }
            } catch (e: Exception) {
                logger.log("TripDetailViewModel.loadTrip", e)
                _state.update { it.copy(isLoading = false, error = "Failed to load trip details") }
            }
        }
    }

    /** Persists the user's notes and shows a brief "Saved ✓" confirmation. */
    fun saveNotes(tripId: String, notes: String) {
        viewModelScope.launch {
            try {
                repo.updateNotes(tripId, notes)
                _state.update { it.copy(notes = notes, notesSaved = true) }
                delay(2_000)
                _state.update { it.copy(notesSaved = false) }
            } catch (e: Exception) {
                logger.log("TripDetailViewModel.saveNotes", e)
            }
        }
    }

    /** Adds an invitee and updates local state optimistically. */
    fun inviteUser(tripId: String, emailOrUsername: String) {
        viewModelScope.launch {
            try {
                repo.inviteUser(tripId, emailOrUsername.trim())
                _state.update {
                    it.copy(
                        invites    = it.invites + emailOrUsername.trim(),
                        inviteSent = true
                    )
                }
                delay(2_000)
                _state.update { it.copy(inviteSent = false) }
            } catch (e: Exception) {
                logger.log("TripDetailViewModel.inviteUser", e)
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
}
