package com.kipita.presentation.trips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.domain.model.SampleData
import com.kipita.domain.model.Trip
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay

@Composable
fun MyTripsScreen(paddingValues: PaddingValues) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Header
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                        Text(
                            text = "My Trips",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KipitaOnSurface
                            )
                        )
                        Text(
                            text = "Plan your next adventure",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KipitaTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Upcoming Trips section
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 30 }) {
                    Column {
                        SectionHeader("Upcoming Trips", "${SampleData.upcomingTrips.size} trips")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(SampleData.upcomingTrips) { index, trip ->
                                TripCard(trip = trip, index = index)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }

            // Past Trips section
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 50 }) {
                    SectionHeader("Past Trips", "${SampleData.pastTrips.size} trips")
                }
            }

            itemsIndexed(SampleData.pastTrips) { index, trip ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { 40 + index * 20 }
                ) {
                    PastTripRow(trip = trip, modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp))
                }
            }

            item { Spacer(Modifier.height(28.dp)) }

            // Quick Tools
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 60 }) {
                    Column {
                        SectionHeader("Quick Tools", "")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickToolCard(
                                icon = Icons.Default.SwapHoriz,
                                label = "Currency",
                                modifier = Modifier.weight(1f)
                            )
                            QuickToolCard(
                                icon = Icons.Default.Map,
                                label = "Offline Maps",
                                modifier = Modifier.weight(1f)
                            )
                            QuickToolCard(
                                icon = Icons.Default.Language,
                                label = "Translate",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = (paddingValues.calculateBottomPadding() + 16.dp)),
            containerColor = KipitaRed,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Trip")
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = KipitaOnSurface
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = KipitaTextSecondary
            )
        }
    }
}

@Composable
private fun TripCard(trip: Trip, index: Int) {
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "trip-elevation"
    )

    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(elevation, RoundedCornerShape(20.dp))
            .clickable { pressed = !pressed },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Dark photo-style cover
            val coverGradient = when (index % 4) {
                0 -> listOf(Color(0xFF1A237E), Color(0xFF4A148C))
                1 -> listOf(Color(0xFF880E4F), Color(0xFFBF360C))
                2 -> listOf(Color(0xFF1B5E20), Color(0xFF004D40))
                else -> listOf(Color(0xFF006064), Color(0xFF01579B))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Brush.linearGradient(colors = coverGradient))
            ) {
                // Dark scrim for text readability
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Black.copy(0.1f), Color.Black.copy(0.5f)))
                    )
                )

                // Countdown badge
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (trip.daysUntil > 0) "In ${trip.daysUntil}d" else "Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Country flag top-end
                Text(
                    text = trip.countryFlag,
                    fontSize = 28.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                )

                // Destination + title overlay at bottom
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                    Text(
                        text = trip.destination,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                }

                // Weather badge bottom-end
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = "${trip.weatherIcon} ${trip.weatherHighC}°",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }

            // Bottom info row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trip.country}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary
                )
                Text(
                    text = "${trip.durationDays} days",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaRed
                )
            }
        }
    }
}

@Composable
private fun PastTripRow(trip: Trip, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "past-trip-scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { pressed = !pressed }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF37474F), Color(0xFF263238))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(trip.countryFlag, fontSize = 20.sp)
                Text(trip.weatherIcon, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trip.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface
            )
            Text(
                text = "${trip.destination} · ${trip.startDate.year}",
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${trip.weatherIcon} ${trip.weatherHighC}°",
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary
            )
            Text(
                text = "${trip.durationDays}d",
                style = MaterialTheme.typography.labelSmall,
                color = KipitaTextTertiary
            )
        }
    }
}

@Composable
private fun QuickToolCard(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tool-scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { pressed = !pressed }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KipitaRedLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = KipitaRed, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = KipitaOnSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
