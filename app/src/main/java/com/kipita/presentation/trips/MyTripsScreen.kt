package com.kipita.presentation.trips

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.data.local.TripEntity
import com.kipita.domain.model.SampleData
import com.kipita.domain.model.Trip
import com.kipita.domain.model.daysUntil
import com.kipita.domain.model.durationDays
import com.kipita.domain.model.startDate
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(
    paddingValues: PaddingValues,
    onAiSuggest: (String) -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onTripClick: (tripId: String) -> Unit = {},
    viewModel: TripsViewModel = hiltViewModel()
) {
    var visible by remember { mutableStateOf(false) }
    var showPlanSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

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
                        Spacer(Modifier.height(14.dp))
                        // AI Quick Actions row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val prompts = listOf(
                                "✈️ Plan a new trip" to "Help me plan my next international trip as a digital nomad. What are the best destinations for Q2 2026?",
                                "🏨 Find hotels" to "What are the best hotels and accommodation options for digital nomads?",
                                "📋 Packing list" to "Create a comprehensive packing list for a 3-month digital nomad trip to Southeast Asia",
                                "💡 Visa tips" to "What are visa requirements and tips for long-term travel as a digital nomad?"
                            )
                            items(prompts.size) { i ->
                                val (label, aiPrompt) = prompts[i]
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = KipitaRedLight
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier
                                            .clickable { onAiSuggest(aiPrompt) }
                                            .padding(horizontal = 12.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = KipitaRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Upcoming Trips section
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 30 }) {
                    Column {
                        SectionHeader("Upcoming Trips", "${state.upcomingTrips.size} trips")
                        if (state.upcomingTrips.isEmpty() && !state.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .clickable { showPlanSheet = true }
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✈️", fontSize = 32.sp)
                                    Text(
                                        "No upcoming trips yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = KipitaTextSecondary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = KipitaRedLight
                                    ) {
                                        Text(
                                            "+ Plan a trip",
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = KipitaRed
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(state.upcomingTrips) { index, trip ->
                                    TripEntityCard(
                                        trip = trip,
                                        index = index,
                                        onClick = { onTripClick(trip.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }

            // Past Trips section — no weather icons
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 50 }) {
                    SectionHeader("Past Trips", "${state.pastTrips.size} trips")
                }
            }

            itemsIndexed(state.pastTrips) { index, trip ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { 40 + index * 20 }
                ) {
                    PastTripEntityRow(
                        trip = trip,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
                        onClick = { onTripClick(trip.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(28.dp)) }

            // Quick Tools — with functional links
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
                                modifier = Modifier.weight(1f),
                                onClick = { onOpenWallet() }
                            )
                            QuickToolCard(
                                icon = Icons.Default.Map,
                                label = "Offline Maps",
                                modifier = Modifier.weight(1f),
                                onClick = { onOpenMap() }
                            )
                            QuickToolCard(
                                icon = Icons.Default.Language,
                                label = "Translate",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    runCatching {
                                        uriHandler.openUri("https://translate.google.com")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }

            // Transport Options section
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 70 }) {
                    Column {
                        SectionHeader("Book Transport", "")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val transports = listOf(
                                Triple(Icons.Default.FlightTakeoff, "Flights", "https://www.google.com/flights"),
                                Triple(Icons.Default.Hotel, "Hotels", "https://www.booking.com"),
                                Triple(Icons.Default.DirectionsCar, "Car Rental", "https://www.rentalcars.com"),
                                Triple(Icons.Default.LocalTaxi, "Uber", "uber://"),
                                Triple(Icons.Default.LocalTaxi, "Lyft", "lyft://"),
                                Triple(Icons.Default.Anchor, "Cruise", "https://www.cruisecritic.com")
                            )
                            items(transports.size) { i ->
                                val (icon, label, deepLink) = transports[i]
                                TransportChip(
                                    icon = icon,
                                    label = label,
                                    onClick = {
                                        runCatching {
                                            // Try app deep-link first, fall back to web
                                            if (deepLink.startsWith("uber://") || deepLink.startsWith("lyft://")) {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                } else {
                                                    val webUrl = if (label == "Uber") "https://uber.com" else "https://lyft.com"
                                                    uriHandler.openUri(webUrl)
                                                }
                                            } else {
                                                uriHandler.openUri(deepLink)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }

        // FAB — opens Plan Trip sheet
        FloatingActionButton(
            onClick = { showPlanSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = (paddingValues.calculateBottomPadding() + 16.dp)),
            containerColor = KipitaRed,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Plan New Trip")
        }
    }

    // Plan New Trip Bottom Sheet
    if (showPlanSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPlanSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PlanTripSheet(
                onClose = { showPlanSheet = false },
                onAiPlan = { prompt ->
                    showPlanSheet = false
                    onAiSuggest(prompt)
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Plan New Trip Bottom Sheet
// ---------------------------------------------------------------------------
@Composable
private fun PlanTripSheet(onClose: () -> Unit, onAiPlan: (String) -> Unit) {
    var destination by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("7") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Plan New Trip",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = KipitaOnSurface
            )
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(KipitaCardBg).clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = KipitaTextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("Destination") },
            placeholder = { Text("Tokyo, Bali, Lisbon...", color = KipitaTextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KipitaRed,
                unfocusedBorderColor = KipitaBorder
            )
        )

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (days)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KipitaRed,
                unfocusedBorderColor = KipitaBorder
            )
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes / Preferences") },
            placeholder = { Text("Budget, travel style, must-sees...", color = KipitaTextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KipitaRed,
                unfocusedBorderColor = KipitaBorder
            )
        )

        // Plan with AI button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (destination.isNotBlank()) KipitaRed else KipitaCardBg)
                .clickable(enabled = destination.isNotBlank()) {
                    val prompt = buildString {
                        append("Help me plan a ${duration}-day trip to $destination")
                        if (notes.isNotBlank()) append(". Notes: $notes")
                        append(". Include day-by-day itinerary, hotels, restaurants, co-working spaces, transport options (flights, car rental, Uber/Lyft), and any Bitcoin-friendly venues.")
                    }
                    onAiPlan(prompt)
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Mic,
                    null,
                    tint = if (destination.isNotBlank()) Color.White else KipitaTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Plan with AI",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (destination.isNotBlank()) Color.White else KipitaTextTertiary
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------
// Section Header
// ---------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------
// Upcoming Trip card (TripEntity) — taps navigate to TripDetailScreen
// ---------------------------------------------------------------------------
@Composable
private fun TripEntityCard(trip: TripEntity, index: Int, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "trip-elevation"
    )
    val coverGradient = when (index % 4) {
        0 -> listOf(Color(0xFF1A237E), Color(0xFF4A148C))
        1 -> listOf(Color(0xFF880E4F), Color(0xFFBF360C))
        2 -> listOf(Color(0xFF1B5E20), Color(0xFF004D40))
        else -> listOf(Color(0xFF006064), Color(0xFF01579B))
    }
    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(elevation, RoundedCornerShape(20.dp))
            .clickable { pressed = !pressed; onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Brush.linearGradient(colors = coverGradient))
            ) {
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Black.copy(0.1f), Color.Black.copy(0.5f)))
                ))
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FlightTakeoff, null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (trip.daysUntil > 0) "In ${trip.daysUntil}d" else "Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                Text(trip.countryFlag, fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                    Text(
                        trip.destination,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        trip.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                }
                // Weather badge for upcoming trips
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        "${trip.weatherIcon} ${trip.weatherHighC}°",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(trip.country, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                Text("${trip.durationDays} days", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaRed)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Past Trip row (TripEntity) — NO weather icons
// ---------------------------------------------------------------------------
@Composable
private fun PastTripEntityRow(trip: TripEntity, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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
            .clickable { pressed = !pressed; onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF37474F), Color(0xFF263238)))),
            contentAlignment = Alignment.Center
        ) {
            Text(trip.countryFlag, fontSize = 24.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                trip.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface
            )
            Text(
                "${trip.destination} · ${trip.startDate.year}",
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${trip.durationDays}d", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaTextSecondary)
            Text(trip.country, style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
        }
    }
}

// ---------------------------------------------------------------------------
// Legacy Upcoming Trip card (Trip domain model) — kept for SampleData fallback
// ---------------------------------------------------------------------------
@Composable
private fun TripCard(trip: Trip, index: Int, onClick: () -> Unit) {
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
            .clickable { pressed = !pressed; onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
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
                        Icon(Icons.Default.FlightTakeoff, null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (trip.daysUntil > 0) "In ${trip.daysUntil}d" else "Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Country flag
                Text(
                    text = trip.countryFlag,
                    fontSize = 28.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                )

                // Destination + title
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

                // Weather badge — only for upcoming trips
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.country,
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

// ---------------------------------------------------------------------------
// Past Trip row — NO weather icons
// ---------------------------------------------------------------------------
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
            // Only flag — no weather icon for past trips
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
                text = "${trip.durationDays}d",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaTextSecondary
            )
            Text(
                text = trip.country,
                style = MaterialTheme.typography.labelSmall,
                color = KipitaTextTertiary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Quick Tool card — with onClick
// ---------------------------------------------------------------------------
@Composable
private fun QuickToolCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
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
            .clickable { pressed = !pressed; onClick() }
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

// ---------------------------------------------------------------------------
// Transport Chip
// ---------------------------------------------------------------------------
@Composable
private fun TransportChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "transport-scale"
    )
    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { pressed = !pressed; onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = KipitaOnSurface, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = KipitaOnSurface
        )
    }
}
