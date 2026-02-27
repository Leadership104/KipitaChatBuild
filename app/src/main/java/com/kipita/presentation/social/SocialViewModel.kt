package com.kipita.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.local.DirectMessageEntity
import com.kipita.data.repository.OfflineMessagingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val messagingRepository: OfflineMessagingRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<DirectMessageEntity>>(emptyList())
    val messages: StateFlow<List<DirectMessageEntity>> = _messages.asStateFlow()

    private val _sendingMessage = MutableStateFlow(false)
    val sendingMessage: StateFlow<Boolean> = _sendingMessage.asStateFlow()
    private val _messageError = MutableStateFlow<String?>(null)
    val messageError: StateFlow<String?> = _messageError.asStateFlow()

    private val confirmedConversationIds = mutableSetOf(
        "dm-alex",
        "dm-sofia",
        "dm-james"
    )

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            messagingRepository.seedSampleMessages(conversationId)
            messagingRepository.observeMessages(conversationId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _sendingMessage.value = true
            _messageError.value = null
            runCatching {
                if (!confirmedConversationIds.contains(conversationId)) {
                    error("Messaging is locked until the email invite is accepted.")
                }
                messagingRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = "current-user",
                    senderName = "You",
                    content = content,
                    isOffline = true
                )
            }.onFailure {
                _messageError.value = it.message
            }
            _sendingMessage.value = false
        }
    }

    fun markRead(conversationId: String) {
        viewModelScope.launch { messagingRepository.markRead(conversationId) }
    }

    fun clearMessageError() {
        _messageError.value = null
    }
}
