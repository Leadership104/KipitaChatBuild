package com.kipita.presentation.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kipita.data.api.PlaceCategory
import com.kipita.data.repository.NearbyPlace
import com.kipita.domain.model.ExploreDestination
import com.kipita.domain.model.SampleData
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay
import java.util.Locale

// ---------------------------------------------------------------------------
// Location scope pills
// ---------------------------------------------------------------------------
private enum class LocationScope(val label: String, val emoji: String) {
    LOCAL("Local", "📍"),
    CITY("City", "🏙"),
    COUNTY("County", "🗺"),
    STATE("State", "🏛"),
    NATIONAL("National", "🇺🇸"),
    GLOBAL("Global", "🌍")
}

// ---------------------------------------------------------------------------
// Places category groups (Yelp)
// ---------------------------------------------------------------------------
private data class CategoryGroup(val label: String, val categories: List<PlaceCategory>)
private val exploreCategories = listOf(
    CategoryGroup("Travel & Lodging", listOf(
        PlaceCategory.HOTELS, PlaceCategory.VACATION_RENTALS, PlaceCategory.TOURS, PlaceCategory.AIRPORTS
    )),
    CategoryGroup("Transportation", listOf(
        PlaceCategory.TRANSPORT, PlaceCategory.CAR_RENTAL, PlaceCategory.EV_CHARGING, PlaceCategory.GAS_STATIONS
    )),
    CategoryGroup("Dining", listOf(
        PlaceCategory.RESTAURANTS, PlaceCategory.CAFES, PlaceCategory.NIGHTLIFE
    )),
    CategoryGroup("Safety & Health", listOf(
        PlaceCategory.SAFETY, PlaceCategory.URGENT_CARE, PlaceCategory.PHARMACIES, PlaceCategory.FITNESS
    )),
    CategoryGroup("Finance & Services", listOf(
        PlaceCategory.BANKS_ATMS
    )),
    CategoryGroup("Culture & Entertainment", listOf(
        PlaceCategory.ARTS, PlaceCategory.SHOPPING, PlaceCategory.PARKS, PlaceCategory.ENTERTAINMENT
    ))
)

@SuppressLint("MissingPermission")
@Composable
fun ExploreScreen(
    paddingValues: PaddingValues,
    onAiSuggest: (String) -> Unit = {},
    onOpenMap: () -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val exploreState by viewModel.state.collectAsStateWithLifecycleCompat()
    var visible by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf(LocationScope.CITY) }
    var selectedTab by remember { mutableIntStateOf(0) }           // 0=Destinations, 1=Places
    var selectedCategory by remember { mutableStateOf(PlaceCategory.HOTELS) }
    var isLocating by remember { mutableStateOf(false) }
    var locationLabel by remember { mutableStateOf("") }
    var detectedLat by remember { mutableStateOf<Double?>(null) }
    var detectedLon by remember { mutableStateOf<Double?>(null) }
    // Sort mode: 0=Default, 1=Cost↑, 2=Safety↓, 3=WiFi↓
    var sortMode by remember { mutableIntStateOf(0) }
    val sortLabels = listOf("Filter", "Cost ↑", "Safety", "WiFi")

    LaunchedEffect(Unit) { delay(80); visible = true }

    // Auto-fetch Yelp results when user switches to Places tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            if (searchText.isNotBlank()) viewModel.fetchByLocation(searchText, selectedCategory)
            else viewModel.fetchByCoordinates(selectedCategory)
        }
    }

    // GPS permission launcher
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isLocating = true
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    detectedLat = loc.latitude
                    detectedLon = loc.longitude
                    // Notify ViewModel so transit deep-links use correct origin
                    viewModel.updateUserLocation(loc.latitude, loc.longitude)
                    // Reverse geocode to city name
                    if (Geocoder.isPresent()) {
                        val geo = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geo.getFromLocation(loc.latitude, loc.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            locationLabel = listOfNotNull(
                                addr.locality ?: addr.subAdminArea,
                                addr.adminArea
                            ).joinToString(", ")
                            searchText = locationLabel
                        } else {
                            searchText = "${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)}"
                        }
                    } else {
                        searchText = "${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)}"
                    }
                    selectedScope = LocationScope.LOCAL
                }
            } catch (_: Exception) {}
            isLocating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // ----------------------------------------------------------------
        // Location search header
        // ----------------------------------------------------------------
        AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Explore",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = KipitaOnSurface
                        )
                        if (locationLabel.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(6.dp).clip(CircleShape)
                                        .background(KipitaGreenAccent)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    locationLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KipitaTextSecondary
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Filter button — cycles through sort modes
                        Surface(
                            modifier = Modifier
                                .border(1.5.dp, KipitaRed, RoundedCornerShape(20.dp))
                                .clickable { sortMode = (sortMode + 1) % sortLabels.size },
                            color = if (sortMode > 0) KipitaRedLight else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FilterList, null, tint = KipitaRed, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(sortLabels[sortMode], style = MaterialTheme.typography.labelSmall, color = KipitaRed)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Location search bar + GPS button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(KipitaCardBg)
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = KipitaTextTertiary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it; if (it.isNotBlank()) selectedScope = LocationScope.CITY },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = KipitaOnSurface),
                            cursorBrush = SolidColor(KipitaRed),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (searchText.isEmpty()) Text(
                                    "City, state, country, address...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KipitaTextTertiary
                                ) else inner()
                            }
                        )
                    }
                    // GPS button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (detectedLat != null) KipitaGreenAccent.copy(.15f) else KipitaCardBg)
                            .clickable { gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KipitaRed)
                        } else {
                            Icon(
                                if (detectedLat != null) Icons.Default.GpsFixed else Icons.Default.MyLocation,
                                "Use my location",
                                tint = if (detectedLat != null) KipitaGreenAccent else KipitaTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Geographic scope pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(LocationScope.entries.size) { i ->
                        val scope = LocationScope.entries[i]
                        val selected = selectedScope == scope
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedScope = scope },
                            color = if (selected) KipitaRed else KipitaCardBg,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "${scope.emoji} ${scope.label}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (selected) Color.White else KipitaTextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Spacer(Modifier.height(4.dp))

                // Tab row: Destinations | Places
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = KipitaRed,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(KipitaRed)
                        )
                    },
                    divider = {}
                ) {
                    listOf("🌍 Destinations", "📍 Places").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (selectedTab == index) KipitaRed else KipitaTextSecondary
                                )
                            }
                        )
                    }
                }
            }
        }

        // ----------------------------------------------------------------
        // Tab content
        // ----------------------------------------------------------------
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "explore-tab"
        ) { tab ->
            when (tab) {
                0 -> DestinationsTab(
                    visible = visible,
                    searchText = searchText,
                    scope = selectedScope,
                    sortMode = sortMode,
                    onAiSuggest = onAiSuggest,
                    onOpenMap = onOpenMap
                )
                1 -> PlacesTab(
                    visible = visible,
                    searchText = searchText,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { cat ->
                        selectedCategory = cat
                        // Trigger Yelp fetch when category changes
                        if (searchText.isNotBlank()) {
                            viewModel.fetchByLocation(searchText, cat)
                        } else {
                            viewModel.fetchByCoordinates(cat)
                        }
                    },
                    places = exploreState.places,
                    placesLoading = exploreState.loading,
                    uberInstalled = exploreState.uberInstalled,
                    lyftInstalled = exploreState.lyftInstalled,
                    onBookUber = { place -> viewModel.bookUberToPlace(place) },
                    onBookLyft = { place -> viewModel.bookLyftToPlace(place) },
                    onAiSuggest = onAiSuggest
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tab 0: Destinations (NomadList data + AI prompts)
// ---------------------------------------------------------------------------
@Composable
private fun DestinationsTab(
    visible: Boolean,
    searchText: String,
    scope: LocationScope,
    sortMode: Int = 0,
    onAiSuggest: (String) -> Unit,
    onOpenMap: () -> Unit = {}
) {
    val base = if (searchText.isBlank()) SampleData.destinations
    else SampleData.destinations.filter {
        it.city.contains(searchText, ignoreCase = true) ||
            it.country.contains(searchText, ignoreCase = true)
    }
    val filtered = when (sortMode) {
        1 -> base.sortedBy { it.costPerMonthUsd }
        2 -> base.sortedByDescending { it.safetyScore }
        3 -> base.sortedByDescending { it.wifiSpeedMbps }
        else -> base
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Interactive Map button
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(80)) + slideInVertically(tween(80)) { 10 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                            )
                        )
                        .clickable(onClick = onOpenMap)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                "Open Interactive Map",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                "Yelp-powered local areas • BTC merchants • Nomad hubs",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        // AI quick-prompt bar
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(100)) + slideInVertically(tween(100)) { 16 }) {
                AiSuggestBar(
                    searchText = searchText,
                    scope = scope,
                    onAiSuggest = onAiSuggest
                )
            }
        }

        // Count
        item {
            Text(
                "${filtered.size} destinations",
                style = MaterialTheme.typography.labelSmall,
                color = KipitaTextTertiary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        itemsIndexed(filtered) { index, dest ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(200 + index * 60)) + slideInVertically(tween(200 + index * 60)) { 30 }
            ) {
                DestinationCard(destination = dest, index = index)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ---------------------------------------------------------------------------
// Tab 1: Places (Yelp category grid → live place list + transit ride buttons)
// ---------------------------------------------------------------------------
@Composable
private fun PlacesTab(
    visible: Boolean,
    searchText: String,
    selectedCategory: PlaceCategory,
    onCategorySelect: (PlaceCategory) -> Unit,
    places: List<NearbyPlace>,
    placesLoading: Boolean,
    uberInstalled: Boolean,
    lyftInstalled: Boolean,
    onBookUber: (NearbyPlace) -> Unit,
    onBookLyft: (NearbyPlace) -> Unit,
    onAiSuggest: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Category groups
        exploreCategories.forEach { group ->
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { 14 }) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            group.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaTextTertiary,
                            modifier = Modifier.padding(start = 16.dp, bottom = 7.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(group.categories.size) { i ->
                                val cat = group.categories[i]
                                PlaceCategoryChip(
                                    category = cat,
                                    selected = selectedCategory == cat,
                                    onClick = { onCategorySelect(cat) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected category header + AI prompt
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 14 }) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedCategory.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                selectedCategory.label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface
                            )
                            if (placesLoading) {
                                Spacer(Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = KipitaRed
                                )
                            }
                        }
                        // AI ask about this category
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val loc = if (searchText.isNotBlank()) " in $searchText" else " nearby"
                                    onAiSuggest("What are the best ${selectedCategory.label}$loc? Include tips, what to look for, and any Bitcoin-friendly options.")
                                },
                            color = Color(0xFF1A1A2E),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ask AI", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // Live Yelp results or empty/api-key placeholder
                    if (places.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            places.forEach { place ->
                                NearbyPlaceCard(
                                    place = place,
                                    uberInstalled = uberInstalled,
                                    lyftInstalled = lyftInstalled,
                                    onBookUber = { onBookUber(place) },
                                    onBookLyft = { onBookLyft(place) }
                                )
                            }
                        }
                    } else if (!placesLoading) {
                        // Empty state — no API key configured yet
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(KipitaCardBg)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(selectedCategory.emoji, fontSize = 32.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Add Yelp API key in Settings to see live ${selectedCategory.label}${if (searchText.isNotBlank()) " in $searchText" else " nearby"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KipitaTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Nearby Place card with Uber / Lyft transit deep-link buttons
// ---------------------------------------------------------------------------
@Composable
private fun NearbyPlaceCard(
    place: NearbyPlace,
    uberInstalled: Boolean,
    lyftInstalled: Boolean,
    onBookUber: () -> Unit,
    onBookLyft: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, KipitaBorder, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
    ) {
        // Main row
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KipitaCardBg),
                contentAlignment = Alignment.Center
            ) {
                Text(place.emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    place.address.ifBlank { place.category.label },
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    // Rating
                    if (place.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                            Text("${"%.1f".format(place.rating)}", style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                        }
                    }
                    // Distance
                    if (place.distanceKm > 0) {
                        Text(
                            "${"%.1f".format(place.distanceKm)} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = KipitaTextTertiary
                        )
                    }
                    // Open/closed badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (place.isOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            if (place.isOpen) "Open" else "Closed",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (place.isOpen) KipitaGreenAccent else KipitaRed
                        )
                    }
                }
            }
        }

        // Transit ride buttons — only shown when place has coordinates
        if (expanded && place.latitude != null && place.longitude != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KipitaCardBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Uber button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF000000))
                        .clickable(onClick = onBookUber)
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (uberInstalled) "Uber" else "Get Uber",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                // Lyft button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE91E8C))
                        .clickable(onClick = onBookLyft)
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.LocalTaxi, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (lyftInstalled) "Lyft" else "Get Lyft",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// AI Suggest bar — shown in Destinations tab
// ---------------------------------------------------------------------------
@Composable
private fun AiSuggestBar(
    searchText: String,
    scope: LocationScope,
    onAiSuggest: (String) -> Unit
) {
    val location = if (searchText.isNotBlank()) searchText else "anywhere"
    val prompts = listOf(
        "✨ Best for nomads" to "What are the best digital nomad cities ${if (searchText.isNotBlank()) "near $searchText" else "globally"} in 2026? Rank by cost, internet, safety and Bitcoin adoption.",
        "🏨 Hotels" to "Find the best hotels and accommodation for digital nomads in $location. Include Bitcoin-friendly options.",
        "🛡 Safety" to "What's the current safety situation and travel advisory for $location? Include crime stats and tips for solo travelers.",
        "💰 Cost of living" to "What is the cost of living in $location for a digital nomad? Break down rent, food, transport and co-working.",
        "⚡ Bitcoin" to "Where can I spend Bitcoin in $location? List merchants, ATMs and Lightning Network options."
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A2E))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("✨", fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "AI Explore${if (searchText.isNotBlank()) " · $searchText" else ""}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            Text(
                scope.emoji + " " + scope.label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(.55f)
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            items(prompts.size) { i ->
                val (label, aiPrompt) = prompts[i]
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onAiSuggest(aiPrompt) },
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Places category chip
// ---------------------------------------------------------------------------
@Composable
private fun PlaceCategoryChip(category: PlaceCategory, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) KipitaRed else Color.White)
            .border(if (selected) 0.dp else 1.dp, KipitaBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(category.emoji, fontSize = 18.sp)
        Spacer(Modifier.height(3.dp))
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) Color.White else KipitaOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Shared composables (used inside column/LazyColumn)
// ---------------------------------------------------------------------------

@Composable
private fun DataSourcePill(label: String, icon: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(KipitaCardBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 10.sp)
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
    }
}

@Composable
private fun DestinationCard(destination: ExploreDestination, index: Int) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dest-scale"
    )

    val cardGradients = listOf(
        listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
        listOf(Color(0xFF43B89C), Color(0xFF3AAFA9)),
        listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53)),
        listOf(Color(0xFF4ECDC4), Color(0xFF44A6AC)),
        listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB))
    )
    val gradient = cardGradients[index % cardGradients.size]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { pressed = !pressed }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Placeholder gradient — visible while photo loads or on error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(colors = gradient))
                )
                // Real location photo via Picsum Photos seeded by city name
                // Free service, no API key needed, consistent per city seed
                val photoSeed = destination.city.lowercase().replace(" ", "-")
                AsyncImage(
                    model = "https://picsum.photos/seed/$photoSeed/800/400",
                    contentDescription = "${destination.city} photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark gradient overlay so text remains readable over any photo
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Black.copy(.10f), Color.Black.copy(.65f)))
                    )
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    shape = CircleShape, color = Color.White.copy(alpha = 0.92f)
                ) {
                    Text("#${destination.rank}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = KipitaOnSurface)
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, null, tint = KipitaGreenAccent, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${destination.wifiSpeedMbps} Mbps", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                    }
                }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                    Text(destination.city, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(destination.country, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(.85f))
                        if (destination.isPopular) {
                            Surface(shape = RoundedCornerShape(4.dp), color = KipitaRed) {
                                Text("Popular", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡", fontSize = 10.sp)
                        Spacer(Modifier.width(3.dp))
                        Text("%.1f".format(destination.safetyScore), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(destination.weatherSummary, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                        destination.tags.take(2).forEach { tag ->
                            Surface(shape = RoundedCornerShape(6.dp), color = KipitaCardBg) {
                                Text(tag, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${destination.costPerMonthUsd}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = KipitaOnSurface)
                    Text("/ month", style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
                }
            }
        }
    }
}
