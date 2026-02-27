package com.kipita.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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

@Composable
fun MapScreen(
    paddingValues: PaddingValues,
    viewModel: MapViewModel = hiltViewModel(),
    onAiSuggest: (String) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    var bottomSheetExpanded by remember { mutableStateOf(true) }
    var visible by remember { mutableStateOf(false) }
    var selectedPlaceFilter by remember { mutableStateOf("BTC") }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Default to user position when available, otherwise New York as placeholder
    val initialLat = if (state.userLat != 0.0) state.userLat else 40.7128
    val initialLng = if (state.userLng != 0.0) state.userLng else -74.0060

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 13f)
    }

    LaunchedEffect(Unit) {
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        val loc = if (hasLocationPermission) {
            @Suppress("MissingPermission")
            lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
        } else null
        val lat = loc?.latitude ?: 40.7128
        val lng = loc?.longitude ?: -74.0060
        viewModel.load("global", lat, lng)
        visible = true
    }

    // Move camera when user location arrives
    LaunchedEffect(state.userLat, state.userLng) {
        if (state.userLat != 0.0 && state.userLng != 0.0) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(state.userLat, state.userLng), 13f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF3F9))
            .padding(paddingValues)
    ) {
        // ── Google Map fills the background ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = com.google.maps.android.compose.MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = hasLocationPermission,
                    mapToolbarEnabled = true
                )
            ) {
                // ── BTC Merchant markers (orange) ─────────────────────────
                if (state.activeOverlays.contains(OverlayType.BTC_MERCHANTS)) {
                    state.merchants.forEach { merchant ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(merchant.latitude, merchant.longitude)
                            ),
                            title = merchant.name,
                            snippet = buildString {
                                if (merchant.acceptsLightning) append("⚡ Lightning  ")
                                if (merchant.acceptsOnchainBtc) append("₿ On-chain  ")
                                if (merchant.acceptsCashApp) append("💵 CashApp")
                            }.trim(),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                    }
                }
            }

            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
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
                    label = if (bottomSheetExpanded) "Collapse" else "Layers",
                    onClick = { bottomSheetExpanded = !bottomSheetExpanded }
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
                    onClick = {
                        onAiSuggest("Give me turn-by-turn transit and walking directions from my current location to the best nearby Bitcoin-friendly place.")
                    }
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
                                listOf("₿ BTC", "🍜 Food", "☕ Cafe").forEach { cat ->
                                    PlaceCategoryPill(
                                        label = cat,
                                        selected = selectedPlaceFilter == cat,
                                        onClick = { selectedPlaceFilter = cat }
                                    )
                                }
                            }
                        }
                    }

                    if (bottomSheetExpanded) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when (selectedPlaceFilter) {
                                "₿ BTC" -> {
                                    // Bitcoin merchants from BTCMap
                                    if (state.merchants.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(KipitaCardBg)
                                                    .padding(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "₿ Toggle the orange BTCMap button to load Bitcoin merchants",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = KipitaTextSecondary,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        items(state.merchants.take(5)) { merchant ->
                                            NearbyPlaceCard(
                                                emoji = "₿",
                                                name = merchant.name,
                                                subtitle = if (merchant.acceptsLightning) "⚡ Lightning + On-Chain" else "On-Chain BTC",
                                                rating = 4.2f,
                                                isFree = false,
                                                distance = "0.3 km",
                                                hasWifi = true,
                                                verified = merchant.source
                                            )
                                        }
                                    }
                                }
                                "🍜 Food" -> {
                                    items(state.nomadPlaces.take(4)) { place ->
                                        NearbyPlaceCard(
                                            emoji = "🍜",
                                            name = "${place.city} — Local Dining",
                                            subtitle = "Cuisine nearby · ${place.country}",
                                            rating = 4.1f,
                                            isFree = false,
                                            distance = "< 1 km",
                                            hasWifi = false,
                                            verified = "Yelp"
                                        )
                                    }
                                    if (state.nomadPlaces.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(KipitaCardBg)
                                                    .padding(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "🍜 Add Yelp API key in Settings to see nearby restaurants",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = KipitaTextSecondary,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                                "☕ Cafe" -> {
                                    items(state.nomadPlaces.take(3)) { place ->
                                        NearbyPlaceCard(
                                            emoji = "☕",
                                            name = "${place.city} Café",
                                            subtitle = "WiFi ${place.internetMbps} Mbps · Nomad-friendly",
                                            rating = (place.safetyScore / 2).toFloat(),
                                            isFree = false,
                                            distance = "0.5 km",
                                            hasWifi = true,
                                            verified = "Nomad List"
                                        )
                                    }
                                    if (state.nomadPlaces.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(KipitaCardBg)
                                                    .padding(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "☕ Add Yelp API key in Settings to see nearby cafés",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = KipitaTextSecondary,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            // Travel notices always shown at the bottom
                            items(state.notices.take(2)) { notice ->
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
private fun PlaceCategoryPill(label: String, selected: Boolean, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
