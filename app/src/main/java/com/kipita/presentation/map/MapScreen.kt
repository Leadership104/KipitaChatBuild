package com.kipita.presentation.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kipita.BuildConfig
import com.kipita.data.repository.NearbyPlace
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextSecondary
import java.util.Locale

@Composable
fun MapScreen(paddingValues: PaddingValues, viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val lm = context.getSystemService(LocationManager::class.java)
            val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) viewModel.updateLocation(loc.latitude, loc.longitude)
        }
    }

    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            val lm = context.getSystemService(LocationManager::class.java)
            val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) viewModel.updateLocation(loc.latitude, loc.longitude)
            else viewModel.load("global")
        } else {
            gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            viewModel.load("global")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SearchBar(
            query = state.searchQuery,
            onQuery = viewModel::updateSearchQuery,
            onSearch = {
                runCatching {
                    if (Geocoder.isPresent() && state.searchQuery.isNotBlank()) {
                        val geo = Geocoder(context, Locale.getDefault())
                        val matches = geo.getFromLocationName(state.searchQuery, 1)
                        val first = matches?.firstOrNull()
                        if (first != null) {
                            viewModel.applySearchCenter(
                                state.searchQuery,
                                first.latitude,
                                first.longitude
                            )
                        }
                    }
                }
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OverlayType.entries.forEach { overlay ->
                AssistChip(
                    onClick = { viewModel.toggleOverlay(overlay) },
                    label = { Text(overlay.label) }
                )
            }
        }

        if (OverlayType.BTC_MERCHANTS in state.activeOverlays) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BtcSourceFilter.entries.forEach { filter ->
                    AssistChip(
                        onClick = { viewModel.setBtcSourceFilter(filter) },
                        label = { Text(filter.name.replace('_', ' ')) }
                    )
                }
            }
            Text(
                text = when (state.btcSourceFilter) {
                    BtcSourceFilter.BTCMAP -> "Showing BTCMap merchants"
                    BtcSourceFilter.CASH_APP -> "Showing Cash App merchants"
                    BtcSourceFilter.BOTH -> "Showing BTCMap + Cash App merchants"
                },
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MarkerType.entries.forEach { markerType ->
                AssistChip(
                    onClick = { viewModel.setMarkerType(markerType) },
                    label = { Text(markerType.label) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (state.offlineReady) "Offline map ready" else "Offline map not cached")
            Button(onClick = { viewModel.cacheRegionOffline("global") }) { Text("Cache offline") }
        }

        MapIframe(
            url = if (state.embeddedMapQuery.isNotBlank()) {
                "https://www.google.com/maps/search/${Uri.encode(state.embeddedMapQuery)}"
            } else {
                "https://www.google.com/maps?q=${state.currentLat},${state.currentLon}"
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Safety", "Urgent Care", "Pharmacies", "Fitness").forEach { label ->
                AssistChip(
                    onClick = { viewModel.showEmbeddedMapSearch(label) },
                    label = { Text(label) }
                )
            }
        }

        AnimatedVisibility(visible = state.loading) { CircularProgressIndicator() }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(state.places, key = { it.id }) { place ->
                MapPlaceCard(
                    place = place,
                    context = context,
                    isExpanded = state.expandedPlaceId == place.id,
                    onTap = { viewModel.toggleExpanded(place.id) },
                    onDirections = {
                        viewModel.showEmbeddedMapSearch("${place.name} ${place.address}")
                    },
                    onMoreInfo = { viewModel.toggleExpanded(place.id) }
                )
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQuery: (String) -> Unit, onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = KipitaTextSecondary)
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = KipitaOnSurface),
            decorationBox = { inner -> if (query.isBlank()) Text("Search city or address") ; inner() }
        )
        Text(
            "Go",
            modifier = Modifier.clickable(onClick = onSearch),
            color = KipitaRed,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun MapIframe(url: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        AndroidView(factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        }, update = { it.loadUrl(url) }, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun MapPlaceCard(
    place: NearbyPlace,
    context: Context,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onDirections: () -> Unit,
    onMoreInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onTap)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .border(1.dp, KipitaRed.copy(alpha = 0.45f), CircleShape)
                    .background(KipitaCardBg),
                contentAlignment = Alignment.Center
            ) {
                val photoUrl = placePhotoUrl(place)
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = place.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(place.emoji, fontSize = 26.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(place.category.label, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text("${"%.1f".format(place.rating)} (${place.reviewCount})    $    ${"%.2f".format(place.distanceKm)}mi Away", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                }
                Text(place.address, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary, maxLines = if (isExpanded) 3 else 2, overflow = TextOverflow.Ellipsis)
            }
            Text(if (place.isOpen) "OPEN" else "CLOSED", color = if (place.isOpen) KipitaGreenAccent else KipitaRed, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
        HorizontalDivider(color = Color(0xFFE6E6E6))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F7F7))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            MiniAction("CALL", Icons.Default.Phone) {
                if (place.phone.isNotBlank()) {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
                        )
                    }
                }
            }
            MiniAction("DIRECTIONS", Icons.Default.LocationOn, onDirections)
            MiniAction("MORE INFO", Icons.Default.Info, onMoreInfo)
        }
        AnimatedVisibility(isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text("Phone: ${if (place.phone.isBlank()) "Unavailable" else place.phone}", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                Text("Distance: ${"%.2f".format(place.distanceKm)} mi", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
            }
        }
    }
}

private fun placePhotoUrl(place: NearbyPlace): String? {
    if (place.photoRef.isBlank()) return null
    return "https://places.googleapis.com/v1/${place.photoRef}/media" +
        "?key=${BuildConfig.GOOGLE_PLACES_API_KEY}&maxWidthPx=128"
}

@Composable
private fun MiniAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = KipitaRed, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
