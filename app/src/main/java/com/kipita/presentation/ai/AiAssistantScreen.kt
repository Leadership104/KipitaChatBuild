package com.kipita.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat

@Composable
fun AiAssistantScreen(paddingValues: PaddingValues, viewModel: AiViewModel = hiltViewModel()) {
    val response = viewModel.response.collectAsStateWithLifecycleCompat().value
    var prompt by remember { mutableStateOf("Is this area safe and where are nearby bitcoin merchants?") }
    val alpha by animateFloatAsState(targetValue = if (response == null) 0.6f else 1f, animationSpec = spring(), label = "response-alpha")

    LaunchedEffect(Unit) { viewModel.analyze("global", prompt) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("AI Travel Intelligence", style = MaterialTheme.typography.headlineMedium)
        BasicTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Button(onClick = { viewModel.analyze("global", prompt) }) { Text("Ask OpenAI + Claude + Gemini") }
        AnimatedVisibility(visible = response != null) {
            Column(modifier = Modifier.graphicsLayer(alpha = alpha)) {
                Text(response?.naturalLanguage.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                Text("Model: ${response?.modelUsed}")
                Text("Citations: ${response?.citations?.joinToString()}")
                Text("Map Action: ${response?.mapAction}")
            }
        }
    }
}
