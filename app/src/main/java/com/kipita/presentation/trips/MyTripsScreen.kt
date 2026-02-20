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
            // Cover gradient placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            colors = when (index % 4) {
                                0 -> listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                1 -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
                                2 -> listOf(Color(0xFF43B89C), Color(0xFF3AAFA9))
                                else -> listOf(Color(0xFF4ECDC4), Color(0xFF44A6AC))
                            }
                        )
                    )
            ) {
                // Countdown badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = KipitaRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${trip.daysUntil}d",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = KipitaRed
                        )
                    }
                }

                // Flag + emoji
                Text(
                    text = trip.countryFlag,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface,
                    maxLines = 1
                )
                Text(
                    text = "${trip.destination}, ${trip.country}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Weather row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(trip.weatherIcon, fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${trip.weatherHighC}° / ${trip.weatherLowC}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary
                        )
                    }
                    Text(
                        text = "${trip.durationDays}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextTertiary
                    )
                }
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
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(trip.countryFlag, fontSize = 24.sp)
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
