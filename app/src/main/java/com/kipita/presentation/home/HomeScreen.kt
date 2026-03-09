package com.kipita.presentation.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.domain.model.parsedInvites
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.trips.TripsViewModel
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
    QuickTool("🎁", "Deals"),
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
    onOpenAI: (String) -> Unit = {},
    onOpenSocial: () -> Unit = {},
    onOpenTranslate: () -> Unit = {},
    onOpenPerks: () -> Unit = {},
    openSosSignal: Int = 0,
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> },
    tripsViewModel: TripsViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    var visible by remember { mutableStateOf(false) }
    var showPackingList by remember { mutableStateOf(false) }
    var showWeather by remember { mutableStateOf(false) }
    var showSosSheet by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val tripsState by tripsViewModel.state.collectAsStateWithLifecycleCompat()
    val weatherState by weatherViewModel.state.collectAsStateWithLifecycleCompat()
    // Collect all invited emails from upcoming/active trips
    val sosEmails by remember(tripsState.upcomingTrips) {
        val emails = tripsState.upcomingTrips
            .flatMap { it.parsedInvites() }
            .filter { it.contains("@") }
            .distinct()
        androidx.compose.runtime.derivedStateOf { emails }
    }

    LaunchedEffect(Unit) { delay(80); visible = true }
    LaunchedEffect(showWeather) {
        if (showWeather) weatherViewModel.refresh()
    }
    LaunchedEffect(openSosSignal) {
        if (openSosSignal > 0) showSosSheet = true
    }

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
            contentPadding = PaddingValues(bottom = 168.dp)
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
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(.70f)
                            )
                            Text(
                                "Where to next?",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
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
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Search destinations, hotels, flights...",
                                    style = MaterialTheme.typography.titleSmall,
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
                                        modifier = Modifier.size(18.dp)
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
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            quickTools.chunked(3).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    row.forEach { tool ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            QuickToolPill(tool = tool) {
                                                when (tool.label) {
                                                    "Currency"     -> onOpenWallet()
                                                    "Maps"         -> onOpenMap()
                                                    "Translate"    -> onOpenTranslate()
                                                    "Deals"        -> onOpenPerks()
                                                    "Packing List" -> showPackingList = true
                                                    "Weather"      -> showWeather = true
                                                }
                                            }
                                        }
                                    }
                                    if (row.size < 3) {
                                        repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
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
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        val transports = listOf(
                                Triple("✈️", "Flights",   "https://expedia.com/affiliate/eA2cKky"),
                                Triple("🏨", "Hotels",    "https://www.hotels.com/affiliate/RrZ7bmg"),
                                Triple("🚗", "Car Rental","https://expedia.com/affiliate/eA2cKky"),
                                Triple("🚢", "Cruise",    "https://expedia.com/affiliate/eA2cKky"),
                                Triple("🚕", "Uber",      "https://uber.com"),
                                Triple("🚕", "Lyft",      "https://lyft.com")
                            )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            transports.chunked(3).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    row.forEach { (emoji, label, url) ->
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White)
                                                .border(1.dp, KipitaBorder, RoundedCornerShape(14.dp))
                                                .clickable { onOpenWebView(url, label) }
                                                .padding(horizontal = 12.dp, vertical = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(emoji, fontSize = 26.sp)
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = KipitaOnSurface,
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

        }

        // ── Bottom action bar (few-click flow) ──────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = (paddingValues.calculateBottomPadding() + 10.dp)),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeActionButton(
                modifier = Modifier.weight(1f),
                emoji = "✨",
                label = "Kipita AI",
                subtitle = "Plan fast",
                background = KipitaRed,
                onClick = { onOpenAI("Help me plan my next trip") }
            )
            HomeActionButton(
                modifier = Modifier.weight(1f),
                emoji = "👥",
                label = "Socials",
                subtitle = "Travel network",
                background = Color(0xFF1A1A2E),
                onClick = onOpenSocial
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
            PackingListSheet(
                onClose = { showPackingList = false },
                onOpenWebView = onOpenWebView
            )
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
            WeatherSheet(
                state = weatherState,
                onRefresh = { weatherViewModel.refresh() },
                onClose = { showWeather = false }
            )
        }
    }

    // ── SOS Emergency Sheet ──────────────────────────────────────────────────
    if (showSosSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSosSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            SosSheet(
                emails = sosEmails,
                context = context,
                onClose = { showSosSheet = false },
                onOpenWebView = { url, title ->
                    showSosSheet = false
                    onOpenWebView(url, title)
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Quick Tool Pill
// ---------------------------------------------------------------------------
@Composable
private fun HomeActionButton(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    subtitle: String,
    background: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

// ---------------------------------------------------------------------------
// Quick Tool Pill
// ---------------------------------------------------------------------------
@Composable
private fun QuickToolPill(tool: QuickTool, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tool.emoji, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                tool.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Packing List Bottom Sheet — with manual items, USPS mail link, Visa Tips
// ---------------------------------------------------------------------------
@Composable
private fun PackingListSheet(
    onClose: () -> Unit,
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var checkedItems by remember { mutableStateOf(setOf<Int>()) }
    val customItems = remember { mutableStateListOf<PackingItem>() }
    var newItemText by remember { mutableStateOf("") }
    var activeSection by remember { mutableStateOf(0) } // 0=Checklist 1=Visa Tips

    // Combined list: defaults + custom
    val allItems = defaultPackingItems + customItems
    var nextId by remember { mutableStateOf(100) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Dark navy header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C))))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    "Packing List 🧳",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    "${checkedItems.size} / ${allItems.size} items packed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Progress bar
        item {
            LinearProgressIndicator(
                progress = {
                    if (allItems.isEmpty()) 0f
                    else checkedItems.size.toFloat() / allItems.size
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .padding(bottom = 10.dp),
                color = KipitaRed,
                trackColor = KipitaCardBg
            )
        }

        // Section toggle tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(KipitaCardBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Checklist", "Visa Tips").forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeSection == i) Color.White else Color.Transparent)
                            .clickable { activeSection = i }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (activeSection == i) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (activeSection == i) KipitaRed else KipitaTextSecondary
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (activeSection == 0) {
            // USPS mail hold link
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF8E1))
                        .clickable {
                            onOpenWebView("https://holdmail.usps.com/holdmail/", "USPS Mail Hold")
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📬", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Put USPS Mail on Hold",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface
                            )
                            Text(
                                "Pause mail delivery while traveling — holdmail.usps.com",
                                style = MaterialTheme.typography.labelSmall,
                                color = KipitaTextSecondary
                            )
                        }
                        Text("→", color = KipitaRed, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Checklist items
            items(allItems) { item ->
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
                        color = if (isChecked) KipitaTextTertiary else KipitaOnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (customItems.any { it.id == item.id }) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = KipitaTextTertiary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { customItems.removeIf { it.id == item.id } }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Add custom item row
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KipitaCardBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = KipitaTextTertiary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = KipitaOnSurface),
                        cursorBrush = SolidColor(KipitaRed),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (newItemText.isEmpty()) Text(
                                "Add custom item...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KipitaTextTertiary
                            ) else inner()
                        }
                    )
                    if (newItemText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(KipitaRed)
                                .clickable {
                                    customItems.add(PackingItem(nextId++, newItemText.trim()))
                                    newItemText = ""
                                }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        } else {
            // Visa Tips section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Visa Tips 🛂",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KipitaOnSurface
                    )
                    Text(
                        "Always check entry requirements well before your trip.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KipitaTextSecondary
                    )

                    val visaTips = listOf(
                        Triple("🛂", "Check Visa on Arrival", "Many countries offer VOA for select passport holders — verify eligibility at your destination embassy or IATA Travel Centre."),
                        Triple("📅", "Application Lead Time", "Apply 6–12 weeks before travel for visa-required destinations. Some countries (India e-Visa, Schengen) take 2–4 weeks minimum."),
                        Triple("💉", "Vaccination Requirements", "Yellow Fever, Meningitis, and Malaria prophylaxis may be required. Check CDC traveler health notices before your trip."),
                        Triple("📋", "Documents Checklist", "Valid passport (6+ months validity), return ticket, proof of accommodation, travel insurance, bank statements showing sufficient funds."),
                        Triple("💰", "Visa Fees", "Budget \$20–\$200 USD for most visas. US B1/B2 = \$185, Schengen = €90, UK Standard = £115. Pay only through official government portals."),
                        Triple("🌐", "Official Resources", "U.S. passports: travel.state.gov | EU/Schengen: ec.europa.eu | UK: gov.uk/visas-immigration"),
                        Triple("🔒", "Digital Nomad Visas", "Portugal D8, Spain Digital Nomad, Indonesia KITAS, Thailand LTR, UAE Freelance — great for remote workers staying 90+ days.")
                    )

                    visaTips.forEach { (emoji, title, desc) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = KipitaOnSurface
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
    } // end outer Column
}

// ---------------------------------------------------------------------------
// Weather Bottom Sheet (placeholder — wire OpenWeatherMap API key in Settings)
// ---------------------------------------------------------------------------
@Composable
private fun WeatherSheet(
    state: WeatherUiState,
    onRefresh: () -> Unit,
    onClose: () -> Unit
) {

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
                    "Live weather data",
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
                    Text(state.current?.emoji ?: "🌤️", fontSize = 48.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            state.current?.let { "${it.temperatureC.toInt()}°C" } ?: "--°C",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            state.current?.description ?: "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(.80f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf(
                        "💧 ${state.current?.humidity ?: 0}%",
                        "💨 ${state.current?.windKmh?.toInt() ?: 0} km/h",
                        if (state.loading) "⏳ Refreshing" else "✅ Live"
                    ).forEach { stat ->
                        Text(stat, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.75f))
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (state.error.isNullOrBlank()) "Current Conditions" else state.error ?: "Weather unavailable",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(KipitaRedLight)
                    .clickable { onRefresh() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = KipitaRed)
                } else {
                    Text("Refresh", style = MaterialTheme.typography.labelSmall, color = KipitaRed)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ---------------------------------------------------------------------------
// SOS Emergency Bottom Sheet
// ---------------------------------------------------------------------------
@Composable
private fun SosSheet(
    emails: List<String>,
    context: android.content.Context,
    onClose: () -> Unit,
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "SOS Emergency 🆘",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFB71C1C)
                )
                Text(
                    "Alert your trip members and get help fast",
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

        // Emergency banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFB71C1C), Color(0xFFD32F2F))))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚨", fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Emergency Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        "Use the buttons below to alert your group or get to safety",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Alert trip members section
        Text(
            "Trip Members",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = KipitaTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (emails.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KipitaCardBg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                emails.forEach { email ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            email,
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaOnSurface
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // Alert all button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFB71C1C))
                    .clickable {
                        runCatching {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
                                putExtra(Intent.EXTRA_SUBJECT, "🆘 SOS Emergency Alert — Kipita")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "This is an emergency alert sent from the Kipita travel app.\n\n" +
                                        "A trip member has triggered an SOS and may need assistance.\n" +
                                        "Please check in with them immediately.\n\n" +
                                        "Stay safe."
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Send SOS Alert"))
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📧", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Alert All Members (${emails.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KipitaCardBg)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No trip members to alert.\nAdd people to a trip to enable group alerts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Navigation actions
        Text(
            "Get Help Now",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = KipitaTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Navigate to hospital — iframed in in-app WebView
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF3E0))
                    .clickable {
                        onOpenWebView(
                            "https://www.google.com/maps/search/nearest+hospital",
                            "Nearest Hospital"
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏥", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Navigate to Hospital",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = KipitaOnSurface
                    )
                    Text(
                        "Opens in-app map to nearest hospital",
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextSecondary
                    )
                }
                Text("→", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
            }

            // Navigate to fire station — iframed in in-app WebView
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFCE4EC))
                    .clickable {
                        onOpenWebView(
                            "https://www.google.com/maps/search/nearest+fire+station",
                            "Nearest Fire Station"
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🚒", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Navigate to Fire Station",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = KipitaOnSurface
                    )
                    Text(
                        "Opens in-app map to nearest fire station",
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextSecondary
                    )
                }
                Text("→", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
            }

            // Call emergency services
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F5E9))
                    .clickable {
                        runCatching {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                            context.startActivity(dialIntent)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📞", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Call Emergency Services",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = KipitaOnSurface
                    )
                    Text(
                        "Dial 911 (US) — adjust for local emergency number",
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextSecondary
                    )
                }
                Text("→", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
