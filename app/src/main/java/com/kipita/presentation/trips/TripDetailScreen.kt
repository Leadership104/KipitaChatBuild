package com.kipita.presentation.trips

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kipita.domain.model.ItineraryDay
import com.kipita.domain.model.daysUntil
import com.kipita.domain.model.durationDays
import com.kipita.domain.model.endDate
import com.kipita.domain.model.startDate
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import java.time.format.DateTimeFormatter

/**
 * Master Trip Detail Page — the "source of truth" for the traveler.
 *
 * Sections:
 *  1. Hero photo + trip name / dates
 *  2. Status + logistics bar (flight, hotel)
 *  3. Day-by-day itinerary (expandable)
 *  4. Notes (editable, auto-saved)
 *  5. Who's Going + Invite system
 *  6. Book transport row
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    paddingValues: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    onAiSuggest: (String) -> Unit = {},
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    var notesText by remember { mutableStateOf("") }
    var notesEditing by remember { mutableStateOf(false) }
    var showInviteSheet by remember { mutableStateOf(false) }
    var inviteInput by remember { mutableStateOf("") }

    LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

    // Sync notes field from loaded state
    LaunchedEffect(state.notes) {
        if (!notesEditing) notesText = state.notes
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = KipitaRed)
        }
        return
    }

    val trip = state.trip ?: return

    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // ── Hero photo ────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                // Destination photo (Picsum seeded by destination)
                val seed = trip.destination.lowercase().replace(" ", "-")
                AsyncImage(
                    model = "https://picsum.photos/seed/$seed/900/500",
                    contentDescription = "${trip.destination} photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(.08f), Color.Black.copy(.72f))
                        )
                    )
                )
                // Back button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(.40f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                // Status badge
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = when (trip.status) {
                        "ACTIVE" -> Color(0xFF1B5E20)
                        "PAST"   -> Color(0xFF37474F)
                        else     -> KipitaRed
                    }
                ) {
                    Text(
                        text = when (trip.status) {
                            "ACTIVE" -> "🟢 Active"
                            "PAST"   -> "✓ Completed"
                            else     -> if (trip.daysUntil > 0) "In ${trip.daysUntil} days" else "Soon"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                // Title overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${trip.countryFlag}  ${trip.destination}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = "${trip.startDate.format(dateFormatter)} → ${trip.endDate.format(dateFormatter)}  ·  ${trip.durationDays} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(.85f)
                    )
                }
                // AI badge
                if (trip.isAiGenerated) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A2E)
                    ) {
                        Text(
                            "✨ AI Planned",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }

        // ── Book & Manage bar (5 options: Flight · Hotel · Car · Uber · Lyft) ──
        item {
            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { 20 }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        "BOOK & MANAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextTertiary,
                        modifier = Modifier.padding(horizontal = 16.dp, bottom = 8.dp),
                        letterSpacing = 1.sp
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ✈️ Flights
                        item {
                            LogisticChip(
                                icon = Icons.Default.FlightTakeoff,
                                label = "Flights",
                                value = trip.flightNumber.ifBlank { "Search" },
                                onClick = {
                                    val url = if (trip.flightNumber.isNotBlank())
                                        "https://www.google.com/flights?hl=en#flt=${trip.flightNumber}"
                                    else
                                        "https://www.google.com/flights"
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                }
                            )
                        }
                        // 🏨 Hotels
                        item {
                            LogisticChip(
                                icon = Icons.Default.Hotel,
                                label = "Hotels",
                                value = trip.hotelName.ifBlank { "Search" },
                                onClick = {
                                    val q = trip.hotelName.ifBlank { trip.destination }
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://www.booking.com/searchresults.html?ss=${Uri.encode(q)}"))
                                        )
                                    }
                                }
                            )
                        }
                        // 🚗 Car Rental
                        item {
                            LogisticChip(
                                icon = Icons.Default.DirectionsCar,
                                label = "Car Rental",
                                value = "Search",
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://www.rentalcars.com/en/searchresults/?dropoff=${Uri.encode(trip.destination)}"))
                                        )
                                    }
                                }
                            )
                        }
                        // 🚕 Uber
                        item {
                            LogisticChip(
                                icon = Icons.Default.LocalTaxi,
                                label = "Uber",
                                value = "Request",
                                onClick = {
                                    runCatching {
                                        val deep = Intent(Intent.ACTION_VIEW, Uri.parse("uber://"))
                                        if (deep.resolveActivity(context.packageManager) != null)
                                            context.startActivity(deep)
                                        else
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://uber.com")))
                                    }
                                }
                            )
                        }
                        // 🟣 Lyft
                        item {
                            LogisticChip(
                                icon = Icons.Default.LocalTaxi,
                                label = "Lyft",
                                value = "Request",
                                onClick = {
                                    runCatching {
                                        val deep = Intent(Intent.ACTION_VIEW, Uri.parse("lyft://ridetype?id=lyft"))
                                        if (deep.resolveActivity(context.packageManager) != null)
                                            context.startActivity(deep)
                                        else
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://lyft.com")))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Itinerary ─────────────────────────────────────────────────────────
        item {
            SectionTitle(
                title = "Itinerary",
                action = if (trip.status != "PAST") "✨ Ask AI" else null,
                onAction = {
                    onAiSuggest("Improve the day-by-day itinerary for my ${trip.durationDays}-day trip to ${trip.destination}. Include hidden gems, local food, and bitcoin-friendly spots.")
                }
            )
        }

        if (state.itineraryDays.isEmpty()) {
            item {
                EmptyStateCard(
                    emoji = "🗓",
                    message = "No itinerary yet",
                    action = "Generate with AI",
                    onAction = {
                        onAiSuggest("Create a detailed day-by-day itinerary for a ${trip.durationDays}-day trip to ${trip.destination}. Include morning, afternoon, and evening activities, local food spots, co-working options, and bitcoin-friendly venues.")
                    }
                )
            }
        } else {
            itemsIndexed(state.itineraryDays) { _, day ->
                ItineraryDayCard(day = day)
            }
        }

        // ── Notes ─────────────────────────────────────────────────────────────
        item {
            SectionTitle(
                title = "Notes",
                action = if (notesEditing) null else "Edit",
                onAction = { notesEditing = true }
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, if (notesEditing) KipitaRed else KipitaBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .animateContentSize()
            ) {
                if (notesEditing) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Add reminders, packing notes, visa info…", color = KipitaTextTertiary) },
                        minLines = 4,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = KipitaRed,
                            unfocusedBorderColor = KipitaBorder
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KipitaCardBg)
                                .clickable { notesEditing = false; notesText = state.notes }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelMedium, color = KipitaTextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KipitaRed)
                                .clickable {
                                    notesEditing = false
                                    viewModel.saveNotes(tripId, notesText)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save Notes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            }
                        }
                    }
                } else {
                    if (state.notesSaved) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = KipitaGreenAccent, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Saved!", style = MaterialTheme.typography.labelSmall, color = KipitaGreenAccent)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    if (state.notes.isBlank()) {
                        Row(
                            modifier = Modifier.clickable { notesEditing = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, null, tint = KipitaTextTertiary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tap to add notes, reminders, or packing items…",
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextTertiary)
                        }
                    } else {
                        Text(state.notes, style = MaterialTheme.typography.bodySmall, color = KipitaOnSurface)
                    }
                }
            }
        }

        // ── Who's Going ───────────────────────────────────────────────────────
        item {
            SectionTitle(
                title = "Who's Going",
                action = "Invite",
                onAction = { showInviteSheet = true }
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, KipitaBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Confirmed travelers
                state.travelers.forEach { name ->
                    TravelerRow(name = name, status = "Going ✓", isConfirmed = true)
                }
                // Pending invites
                state.invites.forEach { email ->
                    TravelerRow(name = email, status = "Invited · Pending", isConfirmed = false)
                }
                if (state.travelers.isEmpty() && state.invites.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showInviteSheet = true }
                    ) {
                        Icon(Icons.Default.PersonAdd, null, tint = KipitaTextTertiary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Invite travelers to this trip", style = MaterialTheme.typography.bodySmall, color = KipitaTextTertiary)
                    }
                }
                // Invite button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(KipitaRedLight)
                        .clickable { showInviteSheet = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = KipitaRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Invite Someone", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaRed)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }

    // ── Invite Bottom Sheet ────────────────────────────────────────────────────
    if (showInviteSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showInviteSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Invite to Trip",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = KipitaOnSurface
                    )
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(KipitaCardBg)
                            .clickable { showInviteSheet = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, tint = KipitaTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                OutlinedTextField(
                    value = inviteInput,
                    onValueChange = { inviteInput = it },
                    label = { Text("Email or @username") },
                    placeholder = { Text("friend@example.com", color = KipitaTextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = KipitaRed,
                        unfocusedBorderColor = KipitaBorder
                    )
                )

                if (state.inviteSent) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, null, tint = KipitaGreenAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Invite sent!", style = MaterialTheme.typography.labelMedium, color = KipitaGreenAccent)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (inviteInput.isNotBlank()) KipitaRed else KipitaCardBg)
                        .clickable(enabled = inviteInput.isNotBlank()) {
                            viewModel.inviteUser(tripId, inviteInput)
                            inviteInput = ""
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Send Invite",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (inviteInput.isNotBlank()) Color.White else KipitaTextTertiary
                    )
                }

                Text(
                    "Invited travelers will receive a link to join this trip. " +
                    "They can view the itinerary and add notes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextTertiary
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun SectionTitle(
    title: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = KipitaOnSurface
        )
        if (action != null) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onAction),
                color = KipitaRedLight,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    action,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaRed
                )
            }
        }
    }
}

@Composable
private fun LogisticChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KipitaCardBg)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = KipitaRed, modifier = Modifier.size(20.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = KipitaTextTertiary
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (value.startsWith("—")) KipitaTextTertiary else KipitaOnSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun ItineraryDayCard(day: ItineraryDay) {
    var expanded by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, KipitaBorder, RoundedCornerShape(14.dp))
            .animateContentSize()
    ) {
        // Day header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = KipitaRed) {
                    Text(
                        "Day ${day.dayNumber}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    day.label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface
                )
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null,
                tint = KipitaTextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                day.activities.forEach { activity ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Time badge
                        if (activity.time.isNotBlank()) {
                            Surface(shape = RoundedCornerShape(6.dp), color = KipitaCardBg) {
                                Text(
                                    activity.time,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KipitaTextTertiary
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(activity.emoji, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                activity.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface
                            )
                            if (activity.desc.isNotBlank()) {
                                Text(
                                    activity.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KipitaTextSecondary
                                )
                            }
                            if (activity.location.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Text("📍", fontSize = 10.sp)
                                    Spacer(Modifier.width(2.dp))
                                    Text(activity.location, style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TravelerRow(name: String, status: String, isConfirmed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isConfirmed) KipitaRedLight else KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isConfirmed) KipitaRed else KipitaTextSecondary
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = KipitaOnSurface)
            Text(status, style = MaterialTheme.typography.labelSmall, color = if (isConfirmed) KipitaGreenAccent else KipitaTextTertiary)
        }
    }
}

@Composable
private fun EmptyStateCard(
    emoji: String,
    message: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, KipitaBorder, RoundedCornerShape(14.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(emoji, fontSize = 32.sp)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = KipitaTextSecondary)
            if (action != null) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onAction),
                    color = Color(0xFF1A1A2E),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            action,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
