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
            messagingRepository.sendMessage(
                conversationId = conversationId,
                senderId = "current-user",
                senderName = "You",
                content = content,
                isOffline = true
            )
            _sendingMessage.value = false
        }
    }

    fun markRead(conversationId: String) {
        viewModelScope.launch { messagingRepository.markRead(conversationId) }
    }
}
