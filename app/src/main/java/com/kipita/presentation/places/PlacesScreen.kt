package com.kipita.presentation.places

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.data.api.PlaceCategory
import com.kipita.data.repository.NearbyPlace
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

// ---------------------------------------------------------------------------
// Group configuration for the category strip
// ---------------------------------------------------------------------------
private data class CategoryGroup(val label: String, val categories: List<PlaceCategory>)

private val categoryGroups = listOf(
    CategoryGroup("Travel & Lodging", listOf(
        PlaceCategory.HOTELS, PlaceCategory.VACATION_RENTALS,
        PlaceCategory.AIRPORTS, PlaceCategory.TOURS
    )),
    CategoryGroup("Transportation", listOf(
        PlaceCategory.TRANSPORT, PlaceCategory.CAR_RENTAL,
        PlaceCategory.EV_CHARGING, PlaceCategory.GAS_STATIONS
    )),
    CategoryGroup("Dining", listOf(
        PlaceCategory.RESTAURANTS, PlaceCategory.CAFES, PlaceCategory.NIGHTLIFE
    )),
    CategoryGroup("Finance & Services", listOf(PlaceCategory.BANKS_ATMS)),
    CategoryGroup("Safety & Health", listOf(
        PlaceCategory.SAFETY, PlaceCategory.URGENT_CARE,
        PlaceCategory.PHARMACIES, PlaceCategory.FITNESS
    )),
    CategoryGroup("Culture & Shopping", listOf(
        PlaceCategory.ARTS, PlaceCategory.SHOPPING,
        PlaceCategory.PARKS, PlaceCategory.ENTERTAINMENT
    ))
)

@Composable
fun PlacesScreen(
    paddingValues: PaddingValues,
    viewModel: PlacesViewModel = hiltViewModel(),
    onAiSuggest: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(80); visible = true }

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
                            horizontalArrangement = Arrangement.SpaceBetween,
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
                                        "Yelp Fusion · open now · real-time",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                            // AI Suggest button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable(onClick = onAiSuggest)
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        "AI Suggest",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Search bar
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            BasicTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::searchQuery,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                cursorBrush = SolidColor(Color.White),
                                decorationBox = { inner ->
                                    if (state.searchQuery.isEmpty()) {
                                        Text(
                                            "Hotels, restaurants, transport...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    } else inner()
                                }
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Category groups + horizontal scroll strips
            // ----------------------------------------------------------------
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { 20 }) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        categoryGroups.forEach { group ->
                            Text(
                                text = group.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaTextTertiary,
                                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                items(group.categories.size) { i ->
                                    val cat = group.categories[i]
                                    CategoryChip(
                                        category = cat,
                                        selected = state.selectedCategory == cat,
                                        onClick = { viewModel.selectCategory(cat) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Results header
            // ----------------------------------------------------------------
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 20 }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                state.selectedCategory.emoji,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = state.selectedCategory.label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface
                            )
                        }
                        if (!state.isLoading) {
                            Text(
                                text = "${state.filteredPlaces.size} results",
                                style = MaterialTheme.typography.labelSmall,
                                color = KipitaTextSecondary
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Loading state
            // ----------------------------------------------------------------
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = KipitaRed, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Searching Yelp nearby...",
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextSecondary
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Empty / API key not set state
            // ----------------------------------------------------------------
            if (!state.isLoading && state.places.isEmpty() && state.error == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KipitaCardBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.selectedCategory.emoji, fontSize = 36.sp)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "No places found nearby",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface
                            )
                            Text(
                                "Add your Yelp API key in Settings to see real-time results",
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextSecondary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Error state
            // ----------------------------------------------------------------
            if (state.error != null) {
                item {
                    Text(
                        state.error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = KipitaRed,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            // ----------------------------------------------------------------
            // Place cards
            // ----------------------------------------------------------------
            itemsIndexed(state.filteredPlaces, key = { _, p -> p.id }) { index, place ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(100 + index * 40)) + slideInHorizontally(tween(100 + index * 40)) { 30 }
                ) {
                    PlaceCard(place = place, modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp))
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Category chip
// ---------------------------------------------------------------------------
@Composable
private fun CategoryChip(category: PlaceCategory, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) KipitaRed else Color.White)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = KipitaBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(category.emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
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
// Place result card
// ---------------------------------------------------------------------------
@Composable
private fun PlaceCard(place: NearbyPlace, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji badge
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(place.emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (place.address.isNotBlank()) {
                Text(
                    text = place.address,
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
                        Text(
                            "${"%.1f".format(place.rating)} (${place.reviewCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = KipitaTextSecondary
                        )
                    }
                }
                // Open badge
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
        // Distance
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${"%.1f".format(place.distanceKm)} km",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface
            )
            if (place.phone.isNotBlank()) {
                Text(
                    place.phone,
                    style = MaterialTheme.typography.labelSmall,
                    color = KipitaTextTertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
