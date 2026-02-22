package com.kipita.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.domain.model.NomadPlaceInfo
import com.kipita.domain.model.TravelNotice
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import com.kipita.presentation.theme.KipitaWarning
import kotlinx.coroutines.launch

private data class LandmarkPin(
    val name: String,
    val emoji: String,
    val distanceKm: Float,
    val rating: Float,
    val xFraction: Float,
    val yFraction: Float,
    val pinColor: Color
)

private val tokyoLandmarks = listOf(
    LandmarkPin("Tokyo Tower", "🗼", 0.3f, 4.6f, 0.55f, 0.28f, Color(0xFFE53935)),
    LandmarkPin("Senso-ji", "⛩️", 1.2f, 4.8f, 0.22f, 0.42f, Color(0xFFFF6F00)),
    LandmarkPin("Shibuya Crossing", "🚦", 0.8f, 4.5f, 0.68f, 0.58f, Color(0xFF1565C0)),
    LandmarkPin("Fab Cafe ₿", "☕", 0.4f, 4.7f, 0.40f, 0.52f, Color(0xFF2E7D32)),
    LandmarkPin("Meiji Shrine", "🌳", 2.1f, 4.9f, 0.16f, 0.22f, Color(0xFF00695C))
)

@Composable
fun MapScreen(
    paddingValues: PaddingValues,
    viewModel: MapViewModel = hiltViewModel(),
    onAiSuggest: (String) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    var selected by remember { mutableStateOf<String?>(null) }
    var mapScale by remember { mutableFloatStateOf(1f) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }
    val markerAlpha = remember { Animatable(0f) }
    var bottomSheetExpanded by remember { mutableStateOf(true) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load("global")
        launch { markerAlpha.animateTo(1f, spring(stiffness = Spring.StiffnessLow)) }
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF3F9))
            .padding(paddingValues)
    ) {
        // Map canvas fills the background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        mapScale = (mapScale * zoom).coerceIn(0.8f, 4f)
                        mapOffset += pan
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color(0xFFD4E3F7))
            ) {
                drawMapGrid(mapScale, mapOffset)
                val merchants = if (state.activeOverlays.contains(OverlayType.BTC_MERCHANTS)) state.merchants else emptyList()
                val nomads = if (state.activeOverlays.contains(OverlayType.NOMAD)) state.nomadPlaces else emptyList()
                merchants.forEachIndexed { index, _ ->
                    val x = (size.width * ((index % 6) + 1) / 7f) * mapScale + mapOffset.x
                    val y = (size.height * ((index / 6) + 1) / 4f) * mapScale + mapOffset.y
                    drawCircle(Color(0xFFF57C00), radius = 9.dp.toPx() * mapScale, center = Offset(x, y), alpha = markerAlpha.value)
                    drawCircle(Color.White, radius = 4.dp.toPx() * mapScale, center = Offset(x, y), alpha = markerAlpha.value)
                }
                nomads.forEachIndexed { index, _ ->
                    val x = (size.width * ((index % 4) + 1) / 5f) * mapScale + mapOffset.x
                    val y = (size.height * ((index / 4) + 1) / 3f) * mapScale + mapOffset.y
                    drawCircle(Color(0xFF4CAF50), radius = 7.dp.toPx() * mapScale, center = Offset(x, y), alpha = markerAlpha.value)
                    drawCircle(Color.White, radius = 3.dp.toPx() * mapScale, center = Offset(x, y), alpha = markerAlpha.value)
                }
            }

            // Landmark pins overlay (composable, for rich labeled pins)
            tokyoLandmarks.forEach { pin ->
                LandmarkPinCard(
                    pin = pin,
                    alpha = markerAlpha.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .wrapContentSize(Alignment.TopStart)
                        .offset(
                            x = (pin.xFraction * 380).dp,
                            y = (pin.yFraction * 360).dp
                        )
                )
            }

            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(32.dp),
                    color = KipitaRed,
                    strokeWidth = 2.dp
                )
            }
        }

        // Glass morphism top controls
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -20 },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassButton(
                    icon = Icons.Default.Layers,
                    label = "Layers",
                    onClick = {}
                )
                GlassButton(
                    icon = Icons.Default.CloudDownload,
                    label = if (state.offlineReady) "Cached" else "Offline",
                    onClick = { viewModel.cacheRegionOffline("global") },
                    tint = if (state.offlineReady) KipitaGreenAccent else KipitaOnSurface
                )
                GlassButton(
                    icon = Icons.Default.Navigation,
                    label = "Navigate",
                    onClick = {}
                )

                Spacer(Modifier.weight(1f))

                // Overlay toggles
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(OverlayType.entries) { overlay ->
                        val active = state.activeOverlays.contains(overlay)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleOverlay(overlay) },
                            color = if (active) KipitaRed else Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (overlay) {
                                    OverlayType.BTC_MERCHANTS -> "₿"
                                    OverlayType.SAFETY -> "🛡"
                                    OverlayType.HEALTH -> "❤️"
                                    OverlayType.INFRASTRUCTURE -> "🏗"
                                    OverlayType.NOMAD -> "💻"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                fontSize = 13.sp,
                                color = if (active) Color.White else KipitaOnSurface
                            )
                        }
                    }
                }
            }
        }

        // Orange BTCMap toggle button — prominent, above AI button
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { 40 },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 160.dp)
        ) {
            val btcActive = state.activeOverlays.contains(OverlayType.BTC_MERCHANTS)
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (btcActive) Color(0xFFF57C00) else Color.White)
                    .border(
                        width = 2.dp,
                        color = Color(0xFFF57C00),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { viewModel.toggleOverlay(OverlayType.BTC_MERCHANTS) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "₿",
                        fontSize = 16.sp,
                        color = if (btcActive) Color.White else Color(0xFFF57C00)
                    )
                    Text(
                        "BTCMap",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (btcActive) Color.White else Color(0xFFF57C00)
                    )
                }
            }
        }

        // Floating AI assistant button — above the bottom sheet
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { 40 },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(KipitaRed)
                    .clickable {
                        onAiSuggest(
                            "What Bitcoin-friendly restaurants, cafes, co-working spaces and shops are in this area? Show me the best places for a digital nomad traveler."
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Ask AI about this area",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom sheet: Nearby Places
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { 100 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val sheetHeight by animateDpAsState(
                targetValue = if (bottomSheetExpanded) 340.dp else 80.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "sheet-height"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drag handle + header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { bottomSheetExpanded = !bottomSheetExpanded }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(KipitaBorder)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nearby Places",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PlaceCategoryPill("₿ BTC", true)
                                PlaceCategoryPill("🍜 Food", false)
                                PlaceCategoryPill("☕ Cafe", false)
                            }
                        }
                    }

                    if (bottomSheetExpanded) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // BTC merchants
                            items(state.merchants.take(3)) { merchant ->
                                NearbyPlaceCard(
                                    emoji = "₿",
                                    name = merchant.name,
                                    subtitle = if (merchant.acceptsLightning) "Lightning + On-Chain" else "On-Chain BTC",
                                    rating = 4.2f,
                                    isFree = false,
                                    distance = "0.3 km",
                                    hasWifi = true,
                                    verified = merchant.source
                                )
                            }
                            // Nomad places
                            items(state.nomadPlaces.take(2)) { place ->
                                NearbyPlaceCard(
                                    emoji = "💻",
                                    name = "${place.city}, ${place.country}",
                                    subtitle = "Internet ${place.internetMbps} Mbps · Safety ${place.safetyScore}",
                                    rating = (place.safetyScore / 2).toFloat(),
                                    isFree = false,
                                    distance = "Nomad hub",
                                    hasWifi = true,
                                    verified = "Nomad List"
                                )
                            }
                            // Travel notices
                            items(state.notices.take(3)) { notice ->
                                TravelNoticeCard(notice = notice)
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandmarkPinCard(pin: LandmarkPin, alpha: Float, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pin-scale"
    )
    Column(
        modifier = modifier
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = (alpha * 0.96f).coerceIn(0f, 1f)))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(pin.emoji, fontSize = 13.sp)
            Text(
                pin.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface
            )
        }
        if (expanded) {
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(10.dp))
                Text("%.1f".format(pin.rating), style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                Text("·", color = KipitaTextTertiary, style = MaterialTheme.typography.labelSmall)
                Text("%.1f km".format(pin.distanceKm), style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
            }
        }
    }
    // Pin stem
    Box(
        modifier = Modifier
            .size(width = 2.dp, height = 8.dp)
            .background(pin.pinColor.copy(alpha = alpha.coerceIn(0f, 1f)))
    )
}

private fun DrawScope.drawMapGrid(scale: Float, offset: Offset) {
    val gridColor = Color(0xFFBDD7F0)
    val step = 40.dp.toPx() * scale
    var x = offset.x % step
    while (x < size.width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5f)
        x += step
    }
    var y = offset.y % step
    while (y < size.height) {
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
        y += step
    }
}

@Composable
private fun GlassButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = KipitaOnSurface
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
        }
    }
}

@Composable
private fun PlaceCategoryPill(label: String, selected: Boolean) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable {},
        color = if (selected) KipitaRedLight else KipitaCardBg,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
            color = if (selected) KipitaRed else KipitaTextSecondary
        )
    }
}

@Composable
private fun NearbyPlaceCard(
    emoji: String,
    name: String,
    subtitle: String,
    rating: Float,
    isFree: Boolean,
    distance: String,
    hasWifi: Boolean,
    verified: String
) {
    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (expanded) 1f else 0.98f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "place-scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(KipitaCardBg)
            .clickable { expanded = !expanded }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                    Text(
                        text = "%.1f".format(rating),
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextSecondary
                    )
                }
                // Free/paid
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isFree) Color(0xFFE8F5E9) else KipitaCardBg
                ) {
                    Text(
                        text = if (isFree) "Free" else "Paid",
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFree) KipitaGreenAccent else KipitaTextSecondary
                    )
                }
                // WiFi
                if (hasWifi) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = KipitaGreenAccent, modifier = Modifier.size(11.dp))
                }
                // Distance
                Text(
                    text = distance,
                    style = MaterialTheme.typography.labelSmall,
                    color = KipitaTextTertiary
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {},
            color = KipitaRedLight
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(3.dp))
                Text("Go", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = KipitaRed)
            }
        }
    }
}

@Composable
private fun TravelNoticeCard(notice: TravelNotice) {
    var expanded by remember { mutableStateOf(false) }

    val severityColor = when (notice.severity.name) {
        "CRITICAL" -> Color(0xFFD32F2F)
        "HIGH" -> KipitaWarning
        "MEDIUM" -> Color(0xFFF9A825)
        else -> KipitaGreenAccent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, severityColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(severityColor)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = KipitaOnSurface,
                        maxLines = if (expanded) Int.MAX_VALUE else 1
                    )
                    Text(
                        text = notice.sourceName,
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextSecondary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = severityColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = notice.severity.name,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = severityColor
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text(
                    text = notice.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Verified: ${if (notice.verified) "✓" else "Unverified"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (notice.verified) KipitaGreenAccent else KipitaTextTertiary
                    )
                    Text(
                        text = "Updated: ${notice.lastUpdated}",
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextTertiary
                    )
                }
            }
        }
    }
}
