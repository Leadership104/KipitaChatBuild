package com.kipita.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MapScreen(paddingValues: PaddingValues, viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    var selected by remember { mutableStateOf<Int?>(null) }

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
                AssistChip(
                    onClick = { viewModel.toggleOverlay(overlay) },
                    label = { Text(overlay.name) }
                )
            }
        }

        AnimatedVisibility(visible = state.loading) { CircularProgressIndicator() }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.notices) { notice ->
                val isSelected = selected == notice.hashCode()
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.96f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "tap-scale"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clickable { selected = notice.hashCode() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(notice.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(notice.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Verified Government Source: ${notice.verified}")
                        Text("Source: ${notice.sourceName}")
                        Text("Updated: ${notice.lastUpdated}")
                        AnimatedVisibility(visible = isSelected) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.size(8.dp))
                                Text("Severity ${notice.severity}")
                            }
                        }
                    }
                }
            }
        }
    }
}
