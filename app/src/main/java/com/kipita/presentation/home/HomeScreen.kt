package com.kipita.presentation.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.calculateBottomPadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "morning"
    in 12..17 -> "afternoon"
    else -> "evening"
}

private data class QuickTool(val emoji: String, val label: String)

private val quickTools = listOf(
    QuickTool("💱", "Currency"),
    QuickTool("🗺️", "Maps"),
    QuickTool("🌐", "Translate"),
    QuickTool("🧳", "Packing List"),
    QuickTool("🌤️", "Weather")
)

private data class PackingItem(val id: Int, val label: String)

private val defaultPackingItems = listOf(
    PackingItem(1, "Passport & ID"),
    PackingItem(2, "Phone charger + adapter"),
    PackingItem(3, "Travel insurance docs"),
    PackingItem(4, "Laptop + peripherals"),
    PackingItem(5, "Medications & prescriptions"),
    PackingItem(6, "Comfortable walking shoes"),
    PackingItem(7, "Clothing (7-day rule)"),
    PackingItem(8, "Toiletries bag"),
    PackingItem(9, "Download offline maps"),
    PackingItem(10, "Notify bank of travel"),
    PackingItem(11, "VPN app installed"),
    PackingItem(12, "Emergency contacts list")
)

// ---------------------------------------------------------------------------
// HomeScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    onOpenWallet: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenAI: (String) -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    var showPackingList by remember { mutableStateOf(false) }
    var showWeather by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) { delay(80); visible = true }

    // Speech recognition result
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) onOpenAI(spoken)
        }
    }

    // Mic permission → launch speech recognizer
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            runCatching {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Where would you like to go?")
                }
                isListening = true
                speechLauncher.launch(intent)
            }.onFailure { isListening = false }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Hero banner ──────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))))
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                    ) {
                        Column {
                            Text(
                                "Good ${greeting()} ✈️",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(.70f)
                            )
                            Text(
                                "Where to next?",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                            )
                            // Search / AI prompt bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(.12f))
                                    .clickable { onOpenAI("Help me plan my next trip") }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(.60f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Search destinations, hotels, flights...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(.55f),
                                    modifier = Modifier.weight(1f)
                                )
                                // Inline mic button inside search bar
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(.15f))
                                        .clickable {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = "Voice search",
                                        tint = if (isListening) KipitaRed else Color.White.copy(.70f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Quick Tools pills ────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { 20 }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            "Quick Tools",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickTools.size) { i ->
                                val tool = quickTools[i]
                                QuickToolPill(tool = tool) {
                                    when (tool.label) {
                                        "Currency"     -> onOpenWallet()
                                        "Maps"         -> onOpenMap()
                                        "Translate"    -> runCatching { uriHandler.openUri("https://translate.google.com") }
                                        "Packing List" -> showPackingList = true
                                        "Weather"      -> showWeather = true
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Book Transport row ───────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 30 }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            "Book Transport",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val transports = listOf(
                                Triple("✈️", "Flights",   "https://www.google.com/flights"),
                                Triple("🏨", "Hotels",    "https://www.booking.com"),
                                Triple("🚗", "Car Rental","https://www.rentalcars.com"),
                                Triple("🚢", "Cruise",    "https://www.cruisecritic.com"),
                                Triple("🚕", "Uber",      "https://uber.com"),
                                Triple("🚕", "Lyft",      "https://lyft.com")
                            )
                            items(transports.size) { i ->
                                val (emoji, label, url) = transports[i]
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White)
                                        .border(1.dp, KipitaBorder, RoundedCornerShape(14.dp))
                                        .clickable { runCatching { uriHandler.openUri(url) } }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(emoji, fontSize = 22.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = KipitaOnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── AI Quick Prompts ─────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { 40 }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            "Ask Kipita AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1A1A2E))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val prompts = listOf(
                                "✈️ Plan my next trip",
                                "₿ Find Bitcoin-friendly spots",
                                "🛡️ Travel safety report",
                                "💰 Best nomad cities 2026"
                            )
                            prompts.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { label ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White.copy(.10f))
                                                .clickable { onOpenAI(label) }
                                                .padding(horizontal = 10.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Nomad Tips ───────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 50 }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            "Nomad Tips",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1A1A2E))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            listOf(
                                "📶 Test WiFi speed before booking co-working",
                                "₿ Use BTCMap to find Bitcoin merchants nearby",
                                "🛡️ Get travel insurance before every international trip",
                                "💱 Convert currency at ECB rates — avoid airport kiosks",
                                "📵 Download offline maps before you lose signal"
                            ).forEach { tip ->
                                Text(
                                    tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(.80f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Floating Mic FAB ─────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = paddingValues.calculateBottomPadding() + 16.dp),
            containerColor = KipitaRed,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = "Voice search"
            )
        }
    }

    // ── Packing List Modal ───────────────────────────────────────────────────
    if (showPackingList) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPackingList = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PackingListSheet(onClose = { showPackingList = false })
        }
    }

    // ── Weather Modal ────────────────────────────────────────────────────────
    if (showWeather) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showWeather = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            WeatherSheet(onClose = { showWeather = false })
        }
    }
}

// ---------------------------------------------------------------------------
// Quick Tool Pill
// ---------------------------------------------------------------------------
@Composable
private fun QuickToolPill(tool: QuickTool, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(tool.emoji, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                tool.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = KipitaOnSurface
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Packing List Bottom Sheet
// ---------------------------------------------------------------------------
@Composable
private fun PackingListSheet(onClose: () -> Unit) {
    var checkedItems by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Packing List 🧳",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = KipitaOnSurface
                )
                Text(
                    "${checkedItems.size} / ${defaultPackingItems.size} items packed",
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(KipitaCardBg)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = KipitaTextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Progress bar
        LinearProgressIndicator(
            progress = {
                if (defaultPackingItems.isEmpty()) 0f
                else checkedItems.size.toFloat() / defaultPackingItems.size
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .padding(bottom = 14.dp),
            color = KipitaRed,
            trackColor = KipitaCardBg
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            defaultPackingItems.forEach { item ->
                val isChecked = item.id in checkedItems
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isChecked) KipitaCardBg else Color.White)
                        .clickable {
                            checkedItems = if (isChecked) checkedItems - item.id else checkedItems + item.id
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isChecked) KipitaRed else Color.Transparent)
                            .border(2.dp, if (isChecked) KipitaRed else KipitaBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isChecked) KipitaTextTertiary else KipitaOnSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ---------------------------------------------------------------------------
// Weather Bottom Sheet (placeholder — wire OpenWeatherMap API key in Settings)
// ---------------------------------------------------------------------------
@Composable
private fun WeatherSheet(onClose: () -> Unit) {
    val forecast = listOf(
        Triple("🌤️", "Today",      "24°C · Partly Cloudy"),
        Triple("🌧️", "Tomorrow",   "19°C · Light Rain"),
        Triple("☀️", "Wednesday",  "27°C · Sunny"),
        Triple("⛅", "Thursday",   "22°C · Cloudy"),
        Triple("🌤️", "Friday",     "25°C · Partly Cloudy")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Weather 🌤️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = KipitaOnSurface
                )
                Text(
                    "Add OpenWeatherMap API key in Settings for live data",
                    style = MaterialTheme.typography.labelSmall,
                    color = KipitaTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(KipitaCardBg)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = KipitaTextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Featured weather card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF0D47A1))))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "📍 Current Location",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(.70f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                ) {
                    Text("🌤️", fontSize = 48.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "24°C",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            "Partly Cloudy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(.80f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf("💧 68%", "💨 12 km/h", "👁 10 km vis").forEach { stat ->
                        Text(stat, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.75f))
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "5-Day Forecast",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = KipitaTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            forecast.forEach { (icon, day, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KipitaCardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icon, fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        day,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = KipitaOnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = KipitaTextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
