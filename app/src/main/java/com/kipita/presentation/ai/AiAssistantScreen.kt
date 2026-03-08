package com.kipita.presentation.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.trips.TripsViewModel
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay
import java.util.Locale

private data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val prompt: String,
    val color: Color
)

private val quickActions = listOf(
    QuickAction(Icons.Default.Map, "Plan Trip", "AI-powered itinerary",
        "Help me plan a 7-day trip to Tokyo as a digital nomad with a Bitcoin budget. Include hotels, coworking spaces, restaurants and transport.", Color(0xFF4CAF50)),
    QuickAction(Icons.Default.FlightTakeoff, "Find Flights", "Best routes & prices",
        "What are the cheapest flight routes for a nomad traveling from the US to Southeast Asia? Include tips on budget airlines and layovers.", Color(0xFF2196F3)),
    QuickAction(Icons.Default.LocationOn, "Nearby Places", "Hotels, dining & transport",
        "Suggest the best hotels, restaurants, cafes and public transport options for a digital nomad. Include places that accept Bitcoin.", Color(0xFF9C27B0)),
    QuickAction(Icons.Default.TravelExplore, "Travel Advisories", "Live safety reports",
        "What are the current travel safety advisories, entry requirements and visa tips for the top digital nomad destinations in 2026?", Color(0xFFFF5722))
)

@Composable
fun AiAssistantScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit = {},
    onTripClick: (tripId: String) -> Unit = {},
    viewModel: AiViewModel = hiltViewModel(),
    tripsViewModel: TripsViewModel = hiltViewModel(),
    preFillPrompt: String = ""
) {
    val response by viewModel.response.collectAsStateWithLifecycleCompat()
    val isAiTyping by viewModel.isAiTyping.collectAsStateWithLifecycleCompat()
    val lastPlanDestination by viewModel.lastPlanDestination.collectAsStateWithLifecycleCompat()
    val lastPlanDays by viewModel.lastPlanDays.collectAsStateWithLifecycleCompat()
    var prompt by remember { mutableStateOf(preFillPrompt) }
    var visible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    // Speech recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                prompt = spokenText
                loading = true
                viewModel.chat(spokenText)
            }
        }
    }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Kipita AI anything about travel...")
            }
            runCatching {
                isListening = true
                speechLauncher.launch(speechIntent)
            }.onFailure { isListening = false }
        }
    }

    // Auto-fire when pre-filled from another screen
    LaunchedEffect(preFillPrompt) {
        if (preFillPrompt.isNotBlank()) {
            prompt = preFillPrompt
            delay(300)
            loading = true
            viewModel.chat(preFillPrompt)
        }
    }

    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    LaunchedEffect(response) {
        if (response != null) loading = false
    }

    LaunchedEffect(isAiTyping) {
        if (!isAiTyping && loading) loading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Dark navy header — Places-style
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C))))
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Column {
                                Text(
                                    "Kipita AI ✨",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    if (loading || isAiTyping) "Thinking..." else "How can I help you today?",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModelBadge("Gemini", Color(0xFF4285F4))
                        }
                    }
                }
            }

            // Quick action cards (staggered entrance)
            if (response == null && !loading && !isAiTyping) {
                item {
                    AnimatedVisibility(visible = visible, enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { 30 }) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                quickActions.chunked(2).forEachIndexed { rowIndex, rowActions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowActions.forEachIndexed { colIndex, action ->
                                            val delayMs = (rowIndex * 2 + colIndex) * 60
                                            AnimatedVisibility(
                                                visible = visible,
                                                enter = fadeIn(tween(200 + delayMs)) + slideInVertically(tween(200 + delayMs)) { 20 },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                QuickActionCard(
                                                    action = action,
                                                    onClick = {
                                                        loading = true
                                                        if (action.title == "Plan Trip") {
                                                            viewModel.planTrip("Tokyo")
                                                        } else {
                                                            prompt = action.prompt
                                                            viewModel.chat(action.prompt)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Suggested prompts
                item {
                    AnimatedVisibility(visible = visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 30 }) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = "Try asking",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val suggestions = listOf(
                                    "Best digital nomad cities 2026",
                                    "Hotels near Shibuya that accept Bitcoin",
                                    "Is Bangkok safe right now?",
                                    "Where can I pay with Bitcoin nearby?",
                                    "Cheapest flights to Lisbon",
                                    "Visa requirements for Japan",
                                    "Best coworking spaces in Bali",
                                    "Car rental tips for road trips",
                                    "How to use Lightning Network abroad"
                                )
                                items(suggestions.size) { i ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(1.dp, KipitaBorder, RoundedCornerShape(20.dp))
                                            .clickable {
                                                prompt = suggestions[i]
                                                loading = true
                                                viewModel.chat(suggestions[i])
                                            },
                                        color = Color.White,
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = suggestions[i],
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = KipitaTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loading state
            if (loading || isAiTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PulsingDots()
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Kipita is thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary
                        )
                    }
                }
            }

            // Response
            if (response != null && !loading) {
                item {
                    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { 30 }) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Model used indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text("✨", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Response via Gemini",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = KipitaTextSecondary
                                )
                            }

                            // Main response
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = response.orEmpty(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = KipitaOnSurface
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Ask again button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(KipitaRedLight)
                                        .clickable {
                                            loading = true
                                            viewModel.chat(prompt)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Ask again",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = KipitaRed
                                    )
                                }

                                // "Add to Trips" — only shown after planTrip()
                                if (lastPlanDestination != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(KipitaRed)
                                            .clickable {
                                                val dest = lastPlanDestination ?: return@clickable
                                                tripsViewModel.acceptAiTrip(
                                                    destination = dest,
                                                    country = "",
                                                    countryFlag = "🌍",
                                                    durationDays = lastPlanDays,
                                                    aiSummary = response.orEmpty().take(500)
                                                ) { tripId ->
                                                    viewModel.clearLastPlan()
                                                    onTripClick(tripId)
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "➕ Add to Trips",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }

        // Floating input bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = (paddingValues.calculateBottomPadding() + 12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isListening) KipitaRed.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Speak",
                        tint = if (isListening) KipitaRed else KipitaTextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = KipitaOnSurface),
                    cursorBrush = SolidColor(KipitaRed),
                    decorationBox = { inner ->
                        if (prompt.isEmpty()) Text(
                            "Ask about destinations, safety, flights...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KipitaTextTertiary
                        )
                        else inner()
                    }
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (prompt.isNotBlank()) KipitaRed else KipitaCardBg)
                        .clickable(enabled = prompt.isNotBlank()) {
                            loading = true
                            viewModel.chat(prompt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Send",
                            tint = if (prompt.isNotBlank()) Color.White else KipitaTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelBadge(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

@Composable
private fun PulsingDots() {
    val transition = rememberInfiniteTransition(label = "typing")
    val delays = listOf(0, 200, 400)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        delays.forEach { delayMs ->
            val scale by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = delayMs, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot-$delayMs"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(KipitaRed)
            )
        }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "action-scale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, KipitaBorder, RoundedCornerShape(16.dp))
            .clickable { pressed = !pressed; onClick() }
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(action.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, contentDescription = null, tint = action.color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = action.title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = KipitaOnSurface
        )
        Text(
            text = action.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = KipitaTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
