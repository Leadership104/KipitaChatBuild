package com.kipita.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun MapScreen(paddingValues: PaddingValues, viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    var selected by remember { mutableStateOf<String?>(null) }
    var mapScale by remember { mutableFloatStateOf(1f) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }
    val markerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.load("global")
        launch { markerAlpha.animateTo(1f, spring(stiffness = Spring.StiffnessLow)) }
    }

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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        mapScale = (mapScale * zoom).coerceIn(0.8f, 4f)
                        mapOffset += pan
                    }
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFEFF3F9))) {
                val merchants = if (state.activeOverlays.contains(OverlayType.BTC_MERCHANTS)) state.merchants else emptyList()
                val nomads = if (state.activeOverlays.contains(OverlayType.NOMAD)) state.nomadPlaces else emptyList()
                merchants.forEachIndexed { index, _ ->
                    val x = (size.width * ((index % 6) + 1) / 7f) * mapScale + mapOffset.x
                    val y = (size.height * ((index / 6) + 1) / 4f) * mapScale + mapOffset.y
                    drawCircle(Color(0xFFFF9800), radius = 8.dp.toPx() * mapScale, center = Offset(x, y), alpha = markerAlpha.value)
                }
                nomads.forEachIndexed { index, _ ->
                    val x = (size.width * ((index % 4) + 1) / 5f) * mapScale + mapOffset.x
                    val y = (size.height * ((index / 4) + 1) / 3f) * mapScale + mapOffset.y
                    drawCircle(Color(0xFF4CAF50), radius = 6.dp.toPx() * mapScale, center = Offset(x, y), alpha = markerAlpha.value)
                }
            }
        }

        Text("Nomad place intelligence", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(state.nomadPlaces) { place ->
                val isSelected = selected == place.placeId
                val elevation by animateFloatAsState(if (isSelected) 1f else 0.96f, label = "nomad-scale")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = place.placeId }
                        .background(Color.White),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${place.city}, ${place.country}", style = MaterialTheme.typography.titleMedium)
                        Text("Cost USD ${place.costOfLivingUsd} • Internet ${place.internetMbps} Mbps • Safety ${place.safetyScore}")
                        Text("Walkability ${place.walkabilityScore} • ${place.weatherSummary} • ${place.timezone}")
                        Text("scale marker ${"%.2f".format(elevation)}")
                    }
                }
            }
            items(state.notices) { notice ->
                val key = "notice-${notice.hashCode()}"
                val isSelected = selected == key
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selected = key },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(notice.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.size(4.dp))
                        Text(notice.description)
                        Text("Verified: ${notice.verified} • ${notice.sourceName}")
                        AnimatedVisibility(isSelected) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Severity ${notice.severity}")
                            }
                        }
                    }
                }
            }
        }
    }
}
