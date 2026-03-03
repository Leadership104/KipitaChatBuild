package com.kipita.presentation.places

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kipita.BuildConfig
import com.kipita.data.api.PlaceCategory
import com.kipita.data.repository.NearbyPlace
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary

// Emergency categories that open Google Maps in the in-app WebView on tap
private val emergencyCategories = setOf(
    PlaceCategory.SAFETY,
    PlaceCategory.URGENT_CARE,
    PlaceCategory.PHARMACIES,
    PlaceCategory.FITNESS
)

@Composable
fun PlacesCategoryResultScreen(
    category: PlaceCategory,
    onBack: () -> Unit,
    onOpenDetail: (NearbyPlace) -> Unit = {},
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> },
    viewModel: PlacesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var expandedPlaceId by rememberSaveable(category) { mutableStateOf<String?>(null) }

    // Trigger GPS and fetch on launch
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            resolveLocationAndFetch(context, viewModel, category)
        }
    }

    LaunchedEffect(category) {
        visible = true
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPerm) {
            resolveLocationAndFetch(context, viewModel, category)
        } else {
            gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            // Fallback: fetch with default Tokyo coords from ViewModel
            viewModel.selectCategory(category)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C)))
                )
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onBack)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    category.emoji,
                    fontSize = 28.sp
                )
                Text(
                    category.label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (!state.isLoading) {
                    Text(
                        "${state.filteredPlaces.size} results nearby",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }
            }

            // Emergency badge
            if (category in emergencyCategories) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFB71C1C).copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "🚨 Emergency",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // ── Content ────────────────────────────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Loading state
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = KipitaRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Finding ${category.label} near you...",
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextSecondary
                            )
                        }
                    }
                }
            }

            // Error
            if (state.error != null && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.error ?: "Could not load places",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaRed,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Empty
            if (!state.isLoading && state.filteredPlaces.isEmpty() && state.error == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(category.emoji, fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No ${category.label} found nearby",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Try enabling location access or check your Google Places API key in local.properties.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextSecondary,
                                modifier = Modifier.padding(top = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Place result cards
            if (!state.isLoading && state.filteredPlaces.isNotEmpty()) {
                itemsIndexed(state.filteredPlaces, key = { _, p -> p.id }) { index, place ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(80 + index * 35)) + slideInVertically(tween(80 + index * 35)) { 20 }
                    ) {
                        PlaceResultCard(
                            place = place,
                            isEmergency = category in emergencyCategories,
                            isExpanded = expandedPlaceId == place.id,
                            onCall = {
                                if (place.phone.isNotBlank()) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
                                        )
                                    }
                                }
                            },
                            onDirections = {
                                val query = Uri.encode("${place.name} ${place.address}")
                                onOpenWebView(
                                    "https://www.google.com/maps/search/$query",
                                    "Find ${place.name}"
                                )
                            },
                            onMoreInfo = {
                                if (category in emergencyCategories) {
                                    val query = Uri.encode("${place.name} ${place.address}")
                                    onOpenWebView(
                                        "https://www.google.com/maps/search/$query",
                                        "Find ${place.name}"
                                    )
                                } else {
                                    expandedPlaceId = if (expandedPlaceId == place.id) null else place.id
                                }
                            },
                            onTap = {
                                if (category in emergencyCategories) {
                                    val query = Uri.encode("${place.name} ${place.address}")
                                    onOpenWebView(
                                        "https://www.google.com/maps/search/$query",
                                        "Find ${place.name}"
                                    )
                                } else {
                                    expandedPlaceId = if (expandedPlaceId == place.id) null else place.id
                                }
                            },
                            onOpenDetail = { onOpenDetail(place) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Place result card — round curves + shadow, expand in-place for non-emergency
// ---------------------------------------------------------------------------
@Composable
private fun PlaceResultCard(
    place: NearbyPlace,
    isEmergency: Boolean = false,
    isExpanded: Boolean = false,
    onTap: () -> Unit = {},
    onCall: () -> Unit = {},
    onDirections: () -> Unit = {},
    onMoreInfo: () -> Unit = {},
    onOpenDetail: () -> Unit = {}
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
                    .background(if (isEmergency) Color(0xFFFFEBEE) else KipitaCardBg),
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
                Text(
                    place.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = KipitaOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(place.category.label, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text(
                        "${"%.1f".format(place.rating)} (${place.reviewCount})    $    ${"%.2f".format(place.distanceKm)}mi Away",
                        style = MaterialTheme.typography.bodySmall,
                        color = KipitaTextSecondary
                    )
                }
                Text(
                    place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary,
                    maxLines = if (isExpanded) 3 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                if (place.isOpen) "OPEN" else "CLOSED",
                color = if (place.isOpen) KipitaGreenAccent else KipitaRed,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.wrapContentWidth()
            )
        }

        HorizontalDivider(color = Color(0xFFE6E6E6))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F7F7))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionLabel("CALL", Icons.Default.Phone, onCall)
            ActionLabel("DIRECTIONS", Icons.Default.LocationOn, onDirections)
            ActionLabel(if (isEmergency) "MAP" else "MORE INFO", Icons.Default.Info, onMoreInfo)
        }

        AnimatedVisibility(visible = isExpanded && !isEmergency) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text("Phone: ${if (place.phone.isBlank()) "Unavailable" else place.phone}", color = KipitaTextSecondary, style = MaterialTheme.typography.bodySmall)
                Text("Category: ${place.category.label}", color = KipitaTextSecondary, style = MaterialTheme.typography.bodySmall)
                if (place.website.isNotBlank()) {
                    Text("Website available in details", color = KipitaTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Open full details",
                    color = KipitaRed,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable(onClick = onOpenDetail)
                )
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
private fun ActionLabel(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = KipitaRed, modifier = Modifier.size(17.dp))
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
    }
}

// ---------------------------------------------------------------------------
// Helper — resolve device GPS and trigger category fetch in ViewModel
// ---------------------------------------------------------------------------
@android.annotation.SuppressLint("MissingPermission")
private fun resolveLocationAndFetch(
    context: Context,
    viewModel: PlacesViewModel,
    category: PlaceCategory
) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (loc != null) {
            viewModel.updateLocation(loc.latitude, loc.longitude)
        } else {
            viewModel.selectCategory(category)
        }
    } catch (_: Exception) {
        viewModel.selectCategory(category)
    }
}
