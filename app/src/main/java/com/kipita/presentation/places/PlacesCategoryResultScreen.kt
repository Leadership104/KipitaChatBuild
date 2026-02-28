package com.kipita.presentation.places

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> },
    viewModel: PlacesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

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
                            onTap = {
                                if (category in emergencyCategories) {
                                    val query = Uri.encode("${place.name} ${place.address}")
                                    onOpenWebView(
                                        "https://www.google.com/maps/search/$query",
                                        "Find ${place.name}"
                                    )
                                }
                                // Non-emergency: expand handled inside the card
                            }
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
    onTap: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable {
                if (isEmergency) onTap()
                else expanded = !expanded
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Emoji badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isEmergency) Color(0xFFFFEBEE) else KipitaCardBg
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(place.emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Main info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (place.address.isNotBlank()) {
                    Text(
                        place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = KipitaTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    if (place.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                "${"%.1f".format(place.rating)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = KipitaTextSecondary
                            )
                        }
                    }
                    // Open badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (place.isOpen) KipitaGreenAccent.copy(alpha = 0.15f)
                                else KipitaRed.copy(alpha = 0.10f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (place.isOpen) "Open" else "Closed",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (place.isOpen) KipitaGreenAccent else KipitaRed
                        )
                    }
                }
            }

            // Right column: distance + emergency indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${"%.1f".format(place.distanceKm)} km",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface
                )
                if (isEmergency) {
                    Text(
                        "View →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = KipitaRed,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Expanded detail — only for non-emergency categories
        if (expanded && !isEmergency) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KipitaCardBg)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (place.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = KipitaTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                place.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaOnSurface
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${"%.2f".format(place.distanceKm)} km away",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary
                        )
                    }
                    if (place.reviewCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${"%.1f".format(place.rating)} from ${place.reviewCount} reviews",
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
