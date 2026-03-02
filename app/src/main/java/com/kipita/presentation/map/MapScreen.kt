package com.kipita.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MapScreen(paddingValues: PaddingValues, viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    var selected by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.load("global") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OverlayType.entries.forEach { overlay ->
                AssistChip(onClick = { viewModel.toggleOverlay(overlay) }, label = { Text(overlay.name) })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (state.offlineReady) "Offline map ready" else "Offline map not cached")
            Button(onClick = { viewModel.cacheRegionOffline("global") }) { Text("Cache offline") }
        }

        AnimatedVisibility(visible = state.loading) { CircularProgressIndicator() }

        Text("Travel notices", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(state.notices) { notice ->
                val key = "notice-${notice.hashCode()}"
                val isSelected = selected == key
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = if (isSelected) null else key }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(notice.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.size(4.dp))
                        Text(notice.description)
                        AnimatedVisibility(isSelected) {
                            Column {
                                Text("Verified: ${notice.verified}")
                                Text("Source: ${notice.sourceName}")
                                Text("Severity: ${notice.severity}")
                            }
                        }
                    }
                }
            }
        }
    }
}
