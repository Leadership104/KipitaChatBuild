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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val emergencyCategories = setOf(
    PlaceCategory.SAFETY,
    PlaceCategory.URGENT_CARE,
    PlaceCategory.PHARMACIES,
    PlaceCategory.FITNESS
)

private data class ResultCategorySection(val label: String, val categories: List<PlaceCategory>)

private val resultCategorySections = listOf(
    ResultCategorySection("Restaurants", listOf(
        PlaceCategory.RESTAURANTS, PlaceCategory.CAFES, PlaceCategory.NIGHTLIFE
    )),
    ResultCategorySection("Entertainment", listOf(
        PlaceCategory.ENTERTAINMENT, PlaceCategory.ARTS, PlaceCategory.PARKS
    )),
    ResultCategorySection("Shopping", listOf(PlaceCategory.SHOPPING)),
    ResultCategorySection("Transportation", listOf(
        PlaceCategory.TRANSPORT, PlaceCategory.CAR_RENTAL,
        PlaceCategory.EV_CHARGING, PlaceCategory.GAS_STATIONS, PlaceCategory.AIRPORTS
    )),
    ResultCategorySection("Services", listOf(
        PlaceCategory.BANKS_ATMS, PlaceCategory.FITNESS
    )),
    ResultCategorySection("Safety", listOf(
        PlaceCategory.SAFETY, PlaceCategory.URGENT_CARE, PlaceCategory.PHARMACIES
    )),
    ResultCategorySection("Destinations", listOf(
        PlaceCategory.HOTELS, PlaceCategory.VACATION_RENTALS, PlaceCategory.TOURS
    ))
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
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    var currentCategory by rememberSaveable { mutableStateOf(category) }
    var activeSectionIndex by rememberSaveable { mutableIntStateOf(0) }
    val enabledSections = if (state.showDestinations) resultCategorySections
    else resultCategorySections.filterNot { it.label == "Destinations" }
    val sections = sortSectionsForCurrentTime(enabledSections) { it.label }
    val uriHandler = LocalUriHandler.current

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

    LaunchedEffect(sections, currentCategory) {
        val idx = sections.indexOfFirst { currentCategory in it.categories }
        if (idx >= 0) activeSectionIndex = idx
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
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
                Text(currentCategory.emoji, fontSize = 28.sp)
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
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFB71C1C).copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Emergency",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
                if (sections.isNotEmpty()) {
                    ScrollableTabRow(selectedTabIndex = activeSectionIndex, edgePadding = 0.dp) {
                        sections.forEachIndexed { index, section ->
                            Tab(
                                selected = activeSectionIndex == index,
                                onClick = { activeSectionIndex = index },
                                text = { Text(section.label) }
                            )
                        }
                    }
                    val children = sections[activeSectionIndex].categories
                    val selectedChild = children.indexOf(currentCategory).let { if (it >= 0) it else 0 }
                    ScrollableTabRow(selectedTabIndex = selectedChild, edgePadding = 0.dp) {
                        children.forEachIndexed { index, child ->
                            Tab(
                                selected = selectedChild == index,
                                onClick = {
                                    currentCategory = child
                                    scope.launch { resolveLocationAndFetch(context, viewModel, child) }
                                },
                                text = { Text("${child.emoji} ${child.label}") }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

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

            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = KipitaRed, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Finding ${currentCategory.label} near you...",
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextSecondary
                            )
                        }
                    }
                }
            }

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
                            Text(currentCategory.emoji, fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No ${currentCategory.label} found nearby",
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

            if (!state.isLoading && state.filteredPlaces.isNotEmpty()) {
                itemsIndexed(state.filteredPlaces, key = { _, p -> p.id }) { index, place ->
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isEmergency) Color(0xFFFFEBEE) else KipitaCardBg),
                contentAlignment = Alignment.Center
            ) {
                Text(place.emoji, fontSize = 24.sp)
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
