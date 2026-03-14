package com.kipita.presentation.advisory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kipita.BuildConfig
import com.kipita.data.api.DwaatAdvisorySection
import com.kipita.domain.model.NoticeCategory
import com.kipita.domain.model.SeverityLevel
import com.kipita.domain.model.TravelNotice
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ---------------------------------------------------------------------------
// Level colors
// ---------------------------------------------------------------------------
private fun levelColor(level: String?) = when (level?.lowercase()) {
    "danger"  -> Color(0xFFC62828)
    "caution" -> Color(0xFFEF6C00)
    "safe"    -> Color(0xFF2E7D32)
    else      -> Color(0xFF1565C0)
}

// ---------------------------------------------------------------------------
// Sample nearby locations (fallback when GPS data unavailable)
// ---------------------------------------------------------------------------
private data class SampleLocation(
    val name: String,
    val subtitle: String,
    val address: String,
    val rating: Double,
    val reviewCount: Int,
    val distanceMi: Double,
    val isOpen: Boolean,
    val phone: String,
    val emoji: String
)

private val sampleSafetyLocations = listOf(
    SampleLocation("Central Police Station", "Police Station", "Nearest precinct to your location", 3.9, 142, 0.6, true, "", "🛡️"),
    SampleLocation("City Fire Station", "Fire & Emergency Services", "Fire & Rescue services", 4.5, 67, 0.9, true, "", "🚒"),
    SampleLocation("Emergency Management Center", "Emergency Management", "City emergency coordination", 4.1, 38, 1.2, true, "", "🚨")
)

private val sampleHealthLocations = listOf(
    SampleLocation("Nearest Hospital", "Hospital · Urgent Care", "24-hr Emergency Department", 4.3, 892, 1.4, true, "", "🏥"),
    SampleLocation("Pharmacy", "Pharmacy", "Prescription & over-the-counter", 4.0, 215, 0.3, true, "", "💊"),
    SampleLocation("Urgent Care Clinic", "Walk-in Clinic", "No appointment needed", 4.2, 478, 2.1, true, "", "🩺")
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------
@Composable
fun AdvisoryScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit = {},
    viewModel: AdvisoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val tabs = listOf(NoticeCategory.ADVISORY, NoticeCategory.SAFETY, NoticeCategory.HEALTH)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userLat by remember { mutableDoubleStateOf(0.0) }
    var userLng by remember { mutableDoubleStateOf(0.0) }
    var detectedCountry by remember { mutableStateOf("") }

    // ── GPS permission ──────────────────────────────────────────────────────
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                loadLocation(context, viewModel) { lat, lng, country ->
                    userLat = lat; userLng = lng; detectedCountry = country
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            scope.launch {
                loadLocation(context, viewModel) { lat, lng, country ->
                    userLat = lat; userLng = lng; detectedCountry = country
                }
            }
        } else {
            gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(paddingValues)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C))))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column {
                        Text(
                            "Travel Advisory",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            if (detectedCountry.isNotBlank()) "Showing data for $detectedCountry"
                            else "Safety · Health · Alerts",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
                // Refresh button
                if (userLat != 0.0) {
                    IconButton(onClick = {
                        viewModel.refreshAiInsight(detectedCountry.ifBlank { "global" }, userLat, userLng)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // Weather line (from real-time data)
            if (state.weatherLine.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(state.weatherLine, style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }
        }

        // ── Tab buttons ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tabs.forEach { tab ->
                AdvisoryTabButton(
                    modifier = Modifier.weight(1f),
                    label = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = tab == state.selectedTab,
                    onClick = { viewModel.selectTab(tab) }
                )
            }
        }

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── AI Safety Insight (Gemini) ────────────────────────────────────
            item {
                AiSafetyInsightCard(
                    insight = state.aiInsight,
                    loading = state.aiInsightLoading,
                    overallLevel = state.safetyReport?.overallLevel ?: state.safetyLevel
                )
            }

            // ── Dwaat real-time advisory sections ──────────────────────────
            if (state.dwaatSections.isNotEmpty() && state.selectedTab == NoticeCategory.ADVISORY) {
                item {
                    Text(
                        "Live Advisory Sections",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = KipitaOnSurface,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }
                items(state.dwaatSections) { section ->
                    DwaatSectionCard(section)
                }
            }

            // ── Domain-layer TravelNotice cards ──────────────────────────
            items(state.tabbedNotices) { notice ->
                AdvisoryNoticeCard(notice = notice)
            }

            // ── Nearby safety/health fallback ────────────────────────────
            if (state.selectedTab == NoticeCategory.SAFETY) {
                item {
                    SectionHeader("Nearby Safety Resources")
                }
                items(sampleSafetyLocations) { loc ->
                    SampleNearbyCard(location = loc)
                }
            }

            if (state.selectedTab == NoticeCategory.HEALTH) {
                item {
                    SectionHeader("Nearby Health Resources")
                }
                items(sampleHealthLocations) { loc ->
                    SampleNearbyCard(location = loc)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// AI Safety Insight card
// ---------------------------------------------------------------------------
@Composable
private fun AiSafetyInsightCard(insight: String?, loading: Boolean, overallLevel: Int) {
    val levelColor = when (overallLevel) {
        4    -> Color(0xFFC62828)
        3    -> Color(0xFFEF6C00)
        2    -> Color(0xFFF9A825)
        else -> Color(0xFF2E7D32)
    }
    val levelLabel = when (overallLevel) {
        4    -> "Do Not Travel"
        3    -> "Reconsider Travel"
        2    -> "Exercise Caution"
        else -> "Normal Precautions"
    }
    val levelEmoji = when (overallLevel) {
        4 -> "🚫"; 3 -> "⚠️"; 2 -> "🟡"; else -> "✅"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D1B2A))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✨", fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "AI Safety Analysis",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            // Level badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(levelColor.copy(alpha = 0.20f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "$levelEmoji $levelLabel",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = levelColor
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (loading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = Color(0xFF4285F4), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Analyzing real-time safety data...", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        } else if (!insight.isNullOrBlank()) {
            Text(
                insight,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.90f),
                lineHeight = 20.sp
            )
        } else {
            Text(
                "Tap refresh in the header to load AI-powered safety insights for your location.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "Powered by Gemini · Verified Dwaat API data",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.35f)
        )
    }
}

// ---------------------------------------------------------------------------
// Dwaat section card
// ---------------------------------------------------------------------------
@Composable
private fun DwaatSectionCard(section: DwaatAdvisorySection) {
    val color = levelColor(section.level)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(section.icon ?: "📋", fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    section.title ?: "Advisory",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface,
                    modifier = Modifier.weight(1f)
                )
                if (section.level != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(color.copy(alpha = 0.14f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            section.level.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = color
                        )
                    }
                }
            }
            if (!section.content.isNullOrBlank()) {
                Text(
                    section.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section header helper
// ---------------------------------------------------------------------------
@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = KipitaOnSurface,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

// ---------------------------------------------------------------------------
// Nearby resource card
// ---------------------------------------------------------------------------
@Composable
private fun SampleNearbyCard(location: SampleLocation) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, KipitaBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(2.5.dp, Color(0xFFE91E63), CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(KipitaCardBg),
                contentAlignment = Alignment.Center
            ) {
                Text(location.emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        location.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = KipitaOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (location.isOpen) "OPEN" else "CLOSED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (location.isOpen) KipitaGreenAccent else KipitaRed,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Text(location.subtitle, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary, modifier = Modifier.padding(top = 2.dp))
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${"%.1f".format(location.rating)} (${location.reviewCount})", style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("${"%.2f".format(location.distanceMi)}mi Away", style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                }
                Text(location.address, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(KipitaBorder))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdvisoryActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Call,
                label = "CALL",
                bgColor = Color(0xFF4CAF50),
                onClick = {
                    if (location.phone.isNotBlank()) {
                        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${location.phone}"))) }
                    }
                }
            )
            AdvisoryActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsWalk,
                label = "DIRECTIONS",
                bgColor = Color(0xFF2196F3),
                onClick = {}
            )
            AdvisoryActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Info,
                label = "MORE INFO",
                bgColor = Color(0xFFFF5722),
                onClick = {}
            )
        }
    }
}

@Composable
private fun AdvisoryActionButton(modifier: Modifier = Modifier, icon: ImageVector, label: String, bgColor: Color, onClick: () -> Unit) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(bgColor).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AdvisoryTabButton(modifier: Modifier = Modifier, label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(if (selected) Color(0xFF1A1A2E) else Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) Color(0xFF1A1A2E) else Color(0xFFE2E2E2), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = if (selected) Color.White else Color(0xFF1A1A1A))
    }
}

@Composable
private fun AdvisoryNoticeCard(notice: TravelNotice) {
    val severityColor = when (notice.severity) {
        SeverityLevel.CRITICAL -> Color(0xFFC62828)
        SeverityLevel.HIGH     -> Color(0xFFEF6C00)
        SeverityLevel.MEDIUM   -> Color(0xFFF9A825)
        SeverityLevel.LOW      -> Color(0xFF2E7D32)
    }
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
    val timeText = notice.lastUpdated.atZone(ZoneId.systemDefault()).format(formatter)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFEAEAEA), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = staticMapImageUrl(notice),
            contentDescription = "${notice.title} location preview",
            modifier = Modifier.width(120.dp).height(100.dp).background(Color(0xFFF1F1F1), RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(notice.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666), maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(severityColor.copy(alpha = 0.14f), CircleShape).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(notice.severity.name, color = severityColor, style = MaterialTheme.typography.labelSmall)
                }
                Text("Updated $timeText", style = MaterialTheme.typography.labelSmall, color = Color(0xFF666666))
                if (notice.verified) Text("Verified", color = Color(0xFF2E7D32), fontSize = 11.sp)
            }
            Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF757575))
                Spacer(Modifier.width(4.dp))
                Text(notice.sourceName, style = MaterialTheme.typography.labelSmall, color = Color(0xFF757575), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
private fun staticMapImageUrl(notice: TravelNotice): String {
    val lat = notice.location.latitude
    val lon = notice.location.longitude
    return "https://maps.googleapis.com/maps/api/staticmap?center=$lat,$lon&zoom=11&size=320x240&markers=color:red|$lat,$lon&key=${BuildConfig.GOOGLE_PLACES_API_KEY}"
}

@android.annotation.SuppressLint("MissingPermission")
private suspend fun loadLocation(
    context: Context,
    viewModel: AdvisoryViewModel,
    onResolved: (lat: Double, lng: Double, country: String) -> Unit
) {
    try {
        val loc = withContext(Dispatchers.IO) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        if (loc != null) {
            val country = withContext(Dispatchers.IO) {
                runCatching {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(loc.latitude, loc.longitude, 1)
                        ?.firstOrNull()?.countryName ?: "global"
                }.getOrElse { "global" }
            }
            onResolved(loc.latitude, loc.longitude, country)
            viewModel.loadWithLocation(country, loc.latitude, loc.longitude)
        } else {
            viewModel.load("global")
        }
    } catch (_: Exception) {
        viewModel.load("global")
    }
}
