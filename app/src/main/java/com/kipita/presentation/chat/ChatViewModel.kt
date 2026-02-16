package com.kipita.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.TripChatRepository
import com.kipita.domain.model.TripMessage
import com.kipita.domain.usecase.AiOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val tripChatRepository: TripChatRepository,
    private val aiOrchestrator: AiOrchestrator,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    fun load(tripId: String) {
        viewModelScope.launch {
            runCatching { tripChatRepository.getMessages(tripId) }
                .onSuccess { _state.value = _state.value.copy(messages = it) }
                .onFailure {
                    _state.value = _state.value.copy(error = it.message)
                    errorLogger.log("ChatViewModel.load", it)
                }
        }
    }

    fun send(tripId: String, senderId: String, senderName: String, content: String, participants: List<String>) {
        viewModelScope.launch {
            runCatching {
                tripChatRepository.enforceParticipantLimit(participants)
                tripChatRepository.sendMessage(tripId, senderId, senderName, content)
                val aiResponse = aiOrchestrator.assistTripChat(tripId, participants, content)
                Triple(tripChatRepository.getMessages(tripId), aiResponse, null)
            }.onSuccess { result ->
                _state.value = _state.value.copy(
                    messages = result.first,
                    latestAiSuggestion = result.second,
                    error = null
                )
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
                errorLogger.log("ChatViewModel.send", it)
            }
        }
    }
}

data class ChatUiState(
    val messages: List<TripMessage> = emptyList(),
    val latestAiSuggestion: String = "",
    val error: String? = null
)
