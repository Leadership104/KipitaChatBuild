package com.kipita.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat

@Composable
fun AiAssistantScreen(paddingValues: PaddingValues, viewModel: AiViewModel = hiltViewModel()) {
    val insight = viewModel.insight.collectAsStateWithLifecycleCompat().value
    LaunchedEffect(Unit) { viewModel.analyze("global") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("AI Travel Intelligence", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { viewModel.analyze("global") }) { Text("Refresh") }
        AnimatedVisibility(visible = insight != null) {
            Column {
                Text(insight?.summary.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                Text("Confidence: ${insight?.confidence}")
                Text("Timestamp: ${insight?.timestamp}")
                Text("Sources: ${insight?.citedSources?.joinToString()}")
            }
        }
    }
}
