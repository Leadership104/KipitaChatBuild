package com.kipita.presentation.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat

@Composable
fun ChatScreen(paddingValues: PaddingValues, viewModel: ChatViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycleCompat().value
    var messageText by remember { mutableStateOf("Let's plan our Tokyo itinerary") }
    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (expanded) 1f else 0.97f, animationSpec = spring(), label = "chat-scale")
    val participants = remember { (1..5).map { "user$it" } }

    LaunchedEffect(Unit) { viewModel.load("trip-demo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("Group Trip Chat (max 10)", style = MaterialTheme.typography.headlineMedium)
        Text("Participants: ${participants.size}/10")
        BasicTextField(value = messageText, onValueChange = { messageText = it }, modifier = Modifier.padding(vertical = 12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable {
                    expanded = !expanded
                    viewModel.send("trip-demo", "host", "Trip Host", messageText, participants)
                }
        ) {
            Text("Send + Ask AI Planner", modifier = Modifier.padding(12.dp))
        }

        AnimatedVisibility(visible = state.error != null) {
            Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }

        AnimatedVisibility(visible = state.latestAiSuggestion.isNotBlank()) {
            Text("AI Planner: ${state.latestAiSuggestion}", modifier = Modifier.padding(vertical = 10.dp))
        }

        LazyColumn {
            items(state.messages) { msg ->
                Text("${msg.senderName}: ${msg.content}", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
