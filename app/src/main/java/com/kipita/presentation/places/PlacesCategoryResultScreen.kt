package com.kipita.presentation.places

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import com.kipita.data.api.timeAwareEmoji
import com.kipita.data.repository.NearbyPlace
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextSecondary
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val emergencyCategories = setOf(
    PlaceCategory.SAFETY,
    PlaceCategory.URGENT_CARE,
    PlaceCategory.PHARMACIES,
    PlaceCategory.FITNESS
)

// ---------------------------------------------------------------------------
// Example placeholder places shown while real results are loading / unavailable
// ---------------------------------------------------------------------------
private data class ExamplePlace(val emoji: String, val name: String, val detail: String)

private val examplePlacesByCategory: Map<PlaceCategory, List<ExamplePlace>> = mapOf(
    PlaceCategory.SAFETY to listOf(
        ExamplePlace("🚓", "Central Police Station", "0.4 km · Emergency services"),
        ExamplePlace("🏥", "City General Hospital", "0.8 km · 24hr Emergency"),
        ExamplePlace("🚒", "Fire Station No. 3", "1.1 km · Fire & Rescue"),
        ExamplePlace("🚑", "Metro Ambulance Hub", "1.5 km · EMS Services")
    ),
    PlaceCategory.URGENT_CARE to listOf(
        ExamplePlace("🏥", "Metro Urgent Care Center", "0.3 km · Walk-ins welcome"),
        ExamplePlace("🏥", "CityMed 24hr Clinic", "0.7 km · Open now"),
        ExamplePlace("🏥", "QuickCare Medical", "1.2 km · No appointment needed"),
        ExamplePlace("🏥", "Riverside Emergency Clinic", "1.8 km · 24hr")
    ),
    PlaceCategory.PHARMACIES to listOf(
        ExamplePlace("💊", "CVS Pharmacy", "0.2 km · Open 24hrs"),
        ExamplePlace("💊", "Walgreens", "0.5 km · Drive-thru available"),
        ExamplePlace("💊", "Rite Aid", "0.9 km · Pharmacist on duty"),
        ExamplePlace("💊", "Community Pharmacy", "1.3 km · Local & independent")
    ),
    PlaceCategory.FITNESS to listOf(
        ExamplePlace("🏋️", "Planet Fitness", "0.3 km · Open 24hrs"),
        ExamplePlace("🏋️", "LA Fitness", "0.7 km · Pool & classes"),
        ExamplePlace("🧘", "YogaWorks Studio", "0.9 km · All levels"),
        ExamplePlace("🏃", "CrossFit Metro", "1.4 km · Group training")
    ),
    PlaceCategory.RESTAURANTS to listOf(
        ExamplePlace("🍕", "Slice of Heaven Pizzeria", "0.2 km · Italian · $$"),
        ExamplePlace("🍔", "The Burger Joint", "0.4 km · American · $"),
        ExamplePlace("🍜", "Pho Saigon Noodle House", "0.6 km · Vietnamese · $"),
        ExamplePlace("🥗", "Fresh Garden Bistro", "0.9 km · Healthy · $$")
    ),
    PlaceCategory.CAFES to listOf(
        ExamplePlace("☕", "Blue Bottle Coffee", "0.1 km · Specialty coffee"),
        ExamplePlace("☕", "The Daily Grind", "0.3 km · Pastries & espresso"),
        ExamplePlace("🧋", "Boba & Beyond", "0.5 km · Bubble tea"),
        ExamplePlace("🍰", "Crumbs Bakery Café", "0.8 km · Cakes & coffee")
    ),
    PlaceCategory.HOTELS to listOf(
        ExamplePlace("🏨", "Grand Hyatt Downtown", "0.5 km · ★★★★★"),
        ExamplePlace("🏨", "Marriott City Center", "0.8 km · ★★★★"),
        ExamplePlace("🏨", "Holiday Inn Express", "1.2 km · ★★★"),
        ExamplePlace("🏠", "Airbnb Loft Suite", "0.3 km · Entire place")
    ),
    PlaceCategory.SHOPPING to listOf(
        ExamplePlace("🛍️", "Westfield Mall", "0.6 km · 200+ stores"),
        ExamplePlace("👕", "H&M Fashion", "0.4 km · Clothing"),
        ExamplePlace("📱", "Apple Store", "0.7 km · Electronics"),
        ExamplePlace("📚", "Barnes & Noble", "1.0 km · Books & gifts")
    ),
    PlaceCategory.ENTERTAINMENT to listOf(
        ExamplePlace("🎬", "AMC Cinemas 16", "0.5 km · Movies"),
        ExamplePlace("🎳", "Bowlero Lanes", "0.8 km · Bowling"),
        ExamplePlace("🎭", "City Arts Center", "1.0 km · Live shows"),
        ExamplePlace("🎲", "Dave & Buster's", "1.3 km · Arcade & dining")
    ),
    PlaceCategory.PARKS to listOf(
        ExamplePlace("🌳", "Riverside City Park", "0.3 km · 50 acres"),
        ExamplePlace("🌿", "Botanical Gardens", "0.7 km · Free entry"),
        ExamplePlace("🥾", "Heritage Trail Head", "1.1 km · 5mi loop"),
        ExamplePlace("⛲", "Downtown Plaza", "0.2 km · Fountains & benches")
    ),
    PlaceCategory.TRANSPORT to listOf(
        ExamplePlace("🚆", "Central Train Station", "0.4 km · All lines"),
        ExamplePlace("🚌", "Metro Bus Terminal", "0.2 km · 12 routes"),
        ExamplePlace("✈️", "International Airport", "8.5 km · Terminal 1–4"),
        ExamplePlace("🚕", "Rideshare Pick-up Zone", "0.1 km · Uber & Lyft")
    )
)


// ---------------------------------------------------------------------------
// Filter mode for sorting the result list
// ---------------------------------------------------------------------------
private enum class PlacesFilter(val label: String) {
    ALL("All"),
    OPEN_NOW("Open Now"),
    BEST_RATED("Best Rated"),
    NEAREST("Nearest")
}

private fun List<NearbyPlace>.applyFilter(filter: PlacesFilter): List<NearbyPlace> = when (filter) {
    PlacesFilter.ALL        -> this
    PlacesFilter.OPEN_NOW   -> filter { it.isOpen }
    PlacesFilter.BEST_RATED -> sortedByDescending { it.rating }
    PlacesFilter.NEAREST    -> sortedBy { it.distanceKm }
}

// Sibling categories to show as horizontal chips for 1-tap switching
private fun siblingCategories(category: PlaceCategory): List<PlaceCategory> = when (category) {
    PlaceCategory.RESTAURANTS, PlaceCategory.CAFES, PlaceCategory.NIGHTLIFE ->
        listOf(PlaceCategory.RESTAURANTS, PlaceCategory.CAFES, PlaceCategory.NIGHTLIFE)
    PlaceCategory.HOTELS, PlaceCategory.VACATION_RENTALS, PlaceCategory.TOURS, PlaceCategory.AIRPORTS ->
        listOf(PlaceCategory.HOTELS, PlaceCategory.VACATION_RENTALS, PlaceCategory.TOURS, PlaceCategory.AIRPORTS)
    PlaceCategory.TRANSPORT, PlaceCategory.CAR_RENTAL, PlaceCategory.EV_CHARGING, PlaceCategory.GAS_STATIONS ->
        listOf(PlaceCategory.TRANSPORT, PlaceCategory.CAR_RENTAL, PlaceCategory.EV_CHARGING, PlaceCategory.GAS_STATIONS)
    PlaceCategory.SAFETY, PlaceCategory.URGENT_CARE, PlaceCategory.PHARMACIES, PlaceCategory.FITNESS ->
        listOf(PlaceCategory.SAFETY, PlaceCategory.URGENT_CARE, PlaceCategory.PHARMACIES, PlaceCategory.FITNESS)
    PlaceCategory.ARTS, PlaceCategory.SHOPPING, PlaceCategory.PARKS, PlaceCategory.ENTERTAINMENT ->
        listOf(PlaceCategory.ARTS, PlaceCategory.SHOPPING, PlaceCategory.PARKS, PlaceCategory.ENTERTAINMENT)
    else -> listOf(PlaceCategory.BANKS_ATMS)
}

@Composable
fun PlacesCategoryResultScreen(
    category: PlaceCategory,
    onBack: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(),
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> },
    viewModel: PlacesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    var currentCategory by rememberSaveable { mutableStateOf(category) }
    var activeFilter by rememberSaveable { mutableStateOf(PlacesFilter.ALL) }
    val uriHandler = LocalUriHandler.current
    val siblings = siblingCategories(currentCategory)
    val displayPlaces = state.filteredPlaces.applyFilter(activeFilter)
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { resolveLocationAndFetch(context, viewModel, currentCategory) }
        }
    }

    LaunchedEffect(category) {
        currentCategory = category
        visible = true
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            resolveLocationAndFetch(context, viewModel, currentCategory)
        } else {
            gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            viewModel.selectCategory(currentCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C))))
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onBack)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(currentCategory.timeAwareEmoji(currentHour), fontSize = 28.sp)
                Text(
                    currentCategory.label,
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

            if (currentCategory in emergencyCategories) {
                val pulse = rememberInfiniteTransition(label = "badge-pulse")
                val pulseAlpha by pulse.animateFloat(
                    initialValue = 0.75f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "badge-alpha"
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFB71C1C).copy(alpha = pulseAlpha))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        "⚠ Emergency",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                // ── Horizontal sibling-category chip strip ──────────────────
                if (siblings.size > 1) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(siblings) { sibling ->
                            val selected = sibling == currentCategory
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (selected) Color(0xFF1A1A2E) else Color.White)
                                    .border(1.dp, if (selected) Color(0xFF1A1A2E) else Color(0xFFE6E6E6), RoundedCornerShape(999.dp))
                                    .clickable {
                                        currentCategory = sibling
                                        activeFilter = PlacesFilter.ALL
                                        scope.launch { resolveLocationAndFetch(context, viewModel, sibling) }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sibling.timeAwareEmoji(currentHour), fontSize = 15.sp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    sibling.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (selected) Color.White else KipitaOnSurface
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // ── Filter pills ────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlacesFilter.values().forEach { filter ->
                        val sel = filter == activeFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (sel) KipitaRed else Color.White)
                                .border(1.dp, if (sel) KipitaRed else Color(0xFFE6E6E6), RoundedCornerShape(999.dp))
                                .clickable { activeFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                filter.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (sel) Color.White else KipitaOnSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = KipitaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::searchQuery,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = KipitaOnSurface),
                        cursorBrush = SolidColor(KipitaOnSurface),
                        decorationBox = { inner ->
                            if (state.searchQuery.isBlank()) {
                                Text(
                                    "Search in ${currentCategory.label.lowercase()}...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KipitaTextSecondary
                                )
                            } else inner()
                        }
                    )
                    if (state.searchQuery.isNotBlank()) {
                        Text(
                            "Clear",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaRed,
                            modifier = Modifier.clickable { viewModel.searchQuery("") }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Show example places while loading or when no real results yet
            val examples = examplePlacesByCategory[currentCategory]
            if ((state.isLoading || displayPlaces.isEmpty()) && !examples.isNullOrEmpty()) {
                item {
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = KipitaRed,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (state.isLoading) "Finding nearby — example places below"
                            else "Example ${currentCategory.label} near you",
                            style = MaterialTheme.typography.labelSmall,
                            color = KipitaTextSecondary
                        )
                    }
                }
                items(examples) { ex ->
                    ExamplePlaceCard(ex = ex)
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (state.error != null && !state.isLoading && displayPlaces.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
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

            if (!state.isLoading && displayPlaces.isNotEmpty()) {
                itemsIndexed(displayPlaces, key = { _, p -> p.id }) { index, place ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(80 + index * 35)) + slideInVertically(tween(80 + index * 35)) { 20 }
                    ) {
                        PlaceResultCard(
                            place = place,
                            isEmergency = currentCategory in emergencyCategories,
                            onTap = {
                                if (currentCategory in emergencyCategories) {
                                    val query = Uri.encode("${place.name} ${place.address}")
                                    onOpenWebView("https://www.google.com/maps/search/$query", "Find ${place.name}")
                                }
                            },
                            onCall = {
                                if (place.phone.isNotBlank()) {
                                    uriHandler.openUri("tel:${place.phone.filter { it.isDigit() || it == '+' }}")
                                }
                            },
                            onDirections = {
                                val query = Uri.encode("${place.name} ${place.address}")
                                uriHandler.openUri("https://www.google.com/maps/search/$query")
                            },
                            onMoreInfo = {
                                val query = Uri.encode("${place.name} ${place.address}")
                                onOpenWebView("https://www.google.com/search?q=$query", place.name)
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

@Composable
private fun PlaceResultCard(
    place: NearbyPlace,
    isEmergency: Boolean = false,
    onTap: () -> Unit = {},
    onCall: () -> Unit = {},
    onDirections: () -> Unit = {},
    onMoreInfo: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { if (isEmergency) onTap() else expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                if (place.photoRef.isNotBlank()) {
                    AsyncImage(
                        model = googlePlacePhotoUrl(place.photoRef),
                        contentDescription = "${place.name} photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(if (isEmergency) Color(0xFFFFEBEE) else KipitaCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(place.emoji, fontSize = 28.sp)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))
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
                    if (place.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${"%.1f".format(place.rating)}", style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (place.isOpen) KipitaGreenAccent.copy(alpha = 0.15f) else KipitaRed.copy(alpha = 0.10f))
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

            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(place.distanceKm)} km", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
                if (isEmergency) {
                    Text("View ->", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = KipitaRed, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        if (!isEmergency) {
            Spacer(Modifier.height(12.dp))
            QuickActionRow(
                hasPhone = place.phone.isNotBlank(),
                onCall = onCall,
                onDirections = onDirections,
                onMoreInfo = onMoreInfo
            )
        }

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
                            Icon(Icons.Default.Phone, contentDescription = null, tint = KipitaTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(place.phone, style = MaterialTheme.typography.bodySmall, color = KipitaOnSurface)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("${"%.2f".format(place.distanceKm)} km away", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
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


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionRow(
    hasPhone: Boolean,
    onCall: () -> Unit,
    onDirections: () -> Unit,
    onMoreInfo: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionPill("CALL", Icons.Default.Phone, enabled = hasPhone, onClick = onCall)
        ActionPill("DIRECTIONS", Icons.Default.Navigation, onClick = onDirections)
        ActionPill("MORE INFO", Icons.Default.Info, onClick = onMoreInfo)
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) KipitaCardBg else Color(0xFFEEEEEE))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) KipitaOnSurface else KipitaTextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (enabled) KipitaOnSurface else KipitaTextSecondary
        )
    }
}

// ---------------------------------------------------------------------------
// Example place card — shown while real data is loading
// ---------------------------------------------------------------------------
@Composable
private fun ExamplePlaceCard(ex: ExamplePlace) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(ex.emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                ex.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface
            )
            Text(
                ex.detail,
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            "Example",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = KipitaTextSecondary.copy(alpha = 0.6f)
        )
    }
}

@android.annotation.SuppressLint("MissingPermission")
private suspend fun resolveLocationAndFetch(
    context: Context,
    viewModel: PlacesViewModel,
    category: PlaceCategory
) {
    try {
        val loc = withContext(Dispatchers.IO) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        if (loc != null) {
            viewModel.updateLocation(loc.latitude, loc.longitude)
            viewModel.selectCategory(category)
        } else {
            viewModel.selectCategory(category)
        }
    } catch (_: Exception) {
        viewModel.selectCategory(category)
    }
}

private fun googlePlacePhotoUrl(photoRef: String): String {
    return "https://places.googleapis.com/v1/$photoRef/media?maxHeightPx=320&maxWidthPx=320&key=${BuildConfig.GOOGLE_PLACES_API_KEY}"
}
