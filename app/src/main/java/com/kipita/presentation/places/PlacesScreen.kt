package com.kipita.presentation.places

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.kipita.data.api.PlaceCategory
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextSecondary
import kotlinx.coroutines.delay

private data class PlacesMainTab(
    val label: String,
    val icon: String,
    val categories: List<PlaceCategory>
)

private val mainTabs = listOf(
    PlacesMainTab(
        label = "Food",
        icon = "🍽",
        categories = listOf(PlaceCategory.RESTAURANTS, PlaceCategory.CAFES, PlaceCategory.NIGHTLIFE)
    ),
    PlacesMainTab(
        label = "Travel",
        icon = "🧭",
        categories = listOf(PlaceCategory.HOTELS, PlaceCategory.TRANSPORT, PlaceCategory.BANKS_ATMS)
    ),
    PlacesMainTab(
        label = "Essentials",
        icon = "🛟",
        categories = listOf(PlaceCategory.SAFETY, PlaceCategory.URGENT_CARE, PlaceCategory.PHARMACIES)
    )
)

@Composable
fun PlacesScreen(
    paddingValues: PaddingValues,
    viewModel: PlacesViewModel = hiltViewModel(),
    onCategorySelected: (PlaceCategory) -> Unit = {},
    onAskKipita: () -> Unit = {},
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    var activeTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var resultsExpanded by rememberSaveable { mutableStateOf(true) }
    val tabs = if (state.showDestinations) mainTabs else mainTabs.map { tab ->
        if (tab.label == "Travel") {
            tab.copy(categories = listOf(PlaceCategory.TRANSPORT, PlaceCategory.BANKS_ATMS, PlaceCategory.GAS_STATIONS))
        } else tab
    }

    val gpsLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            scope.launch(Dispatchers.IO) {
                val lm = context.getSystemService(LocationManager::class.java)
                val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) viewModel.updateLocation(loc.latitude, loc.longitude)
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(80); visible = true
        withContext(Dispatchers.IO) {
            val perm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            if (perm == PackageManager.PERMISSION_GRANTED) {
                val lm = context.getSystemService(LocationManager::class.java)
                val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) viewModel.updateLocation(loc.latitude, loc.longitude)
            } else {
                withContext(Dispatchers.Main) { gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            }
        }
    }

    LaunchedEffect(tabs.size) {
        if (tabs.isEmpty()) return@LaunchedEffect
        if (activeTabIndex > tabs.lastIndex) activeTabIndex = 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // ----------------------------------------------------------------
            // Header
            // ----------------------------------------------------------------
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Explore Places",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        "Large tap targets · one-click categories",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { 20 }) {
                    Column(modifier = Modifier.padding(top = 18.dp)) {
                        if (tabs.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                tabs.forEach { tab ->
                                    val selected = tabs[activeTabIndex].label == tab.label
                                    MainTabButton(
                                        modifier = Modifier.weight(1f),
                                        label = tab.label,
                                        icon = tab.icon,
                                        selected = selected,
                                        onClick = {
                                            val index = tabs.indexOf(tab)
                                            activeTabIndex = index
                                            val firstCategory = tabs[index].categories.firstOrNull()
                                            if (firstCategory != null) {
                                                viewModel.selectCategory(firstCategory)
                                                onCategorySelected(firstCategory)
                                                resultsExpanded = true
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                tabs[activeTabIndex].categories.forEach { category ->
                                    LargeCategoryButton(
                                        category = category,
                                        selected = category == state.selectedCategory,
                                        onClick = {
                                            if (category == state.selectedCategory) {
                                                resultsExpanded = !resultsExpanded
                                            } else {
                                                viewModel.selectCategory(category)
                                                onCategorySelected(category)
                                                resultsExpanded = true
                                            }
                                        }
                                    )
                                }
                                if (tabs[activeTabIndex].label == "Travel") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFFFF2E8))
                                            .border(1.dp, Color(0xFFFFD0B0), RoundedCornerShape(18.dp))
                                            .clickable {
                                                onOpenWebView("https://btcmap.org/map", "BTCMap — Bitcoin Merchants")
                                            }
                                            .padding(horizontal = 18.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            "₿ Open BTCMap",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color(0xFF9B3A00)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (resultsExpanded) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, KipitaBorder, RoundedCornerShape(16.dp))
                            .clickable { resultsExpanded = false }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Nearby ${state.selectedCategory.label} (${state.filteredPlaces.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ExpandLess, contentDescription = "Close results", tint = KipitaOnSurface)
                    }
                }
                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = KipitaRed)
                        }
                    }
                } else if (state.error != null) {
                    item {
                        Text(
                            state.error ?: "Could not load places",
                            color = KipitaRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(state.filteredPlaces) { place ->
                        InlinePlaceCard(place = place)
                    }
                }
            }

            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 20 }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1A1A2E))
                            .clickable(onClick = onAskKipita)
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Ask Kipita",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MainTabButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF1A1A2E) else Color.White)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = KipitaBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else KipitaOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}

@Composable
private fun LargeCategoryButton(
    category: PlaceCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, if (selected) Color(0xFF1A1A2E) else KipitaBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(category.emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Tap once to open live nearby results",
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A2E))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Ready", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
private fun InlinePlaceCard(place: com.kipita.data.repository.NearbyPlace) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, KipitaBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (place.photoRef.isNotBlank()) {
                AsyncImage(
                    model = "https://places.googleapis.com/v1/${place.photoRef}/media?maxHeightPx=320&maxWidthPx=320&key=${BuildConfig.GOOGLE_PLACES_API_KEY}",
                    contentDescription = "${place.name} photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(KipitaCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(place.emoji, fontSize = 26.sp)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                place.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                place.address,
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (place.rating > 0) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${"%.1f".format(place.rating)}", style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = if (place.isOpen) "Open" else "Closed",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (place.isOpen) KipitaGreenAccent else KipitaRed
                )
            }
        }
        Text(
            text = "${"%.1f".format(place.distanceKm)} km",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = KipitaOnSurface
        )
    }
}
