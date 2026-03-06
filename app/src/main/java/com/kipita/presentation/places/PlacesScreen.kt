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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
private data class CategorySection(val label: String, val categories: List<PlaceCategory>)
private data class QuickIntent(val label: String, val category: PlaceCategory, val query: String = "")

private val quickIntents = listOf(
    QuickIntent("🍕 Pizza", PlaceCategory.RESTAURANTS, "pizza"),
    QuickIntent("☕ Coffee", PlaceCategory.CAFES, "coffee"),
    QuickIntent("🏧 ATM", PlaceCategory.BANKS_ATMS, "atm"),
    QuickIntent("⛽ Gas", PlaceCategory.GAS_STATIONS, "gas")
)

private val baseCategorySections = listOf(
    CategorySection("Restaurants", listOf(
        PlaceCategory.RESTAURANTS, PlaceCategory.CAFES, PlaceCategory.NIGHTLIFE
    )),
    CategorySection("Entertainment", listOf(
        PlaceCategory.ENTERTAINMENT, PlaceCategory.ARTS, PlaceCategory.PARKS
    )),
    CategorySection("Shopping", listOf(PlaceCategory.SHOPPING)),
    CategorySection("Transportation", listOf(
        PlaceCategory.TRANSPORT, PlaceCategory.CAR_RENTAL,
        PlaceCategory.EV_CHARGING, PlaceCategory.GAS_STATIONS, PlaceCategory.AIRPORTS
    )),
    CategorySection("Services", listOf(
        PlaceCategory.BANKS_ATMS, PlaceCategory.FITNESS
    )),
    CategorySection("Safety", listOf(
        PlaceCategory.SAFETY, PlaceCategory.URGENT_CARE, PlaceCategory.PHARMACIES
    )),
    CategorySection("Destinations", listOf(
        PlaceCategory.HOTELS, PlaceCategory.VACATION_RENTALS,
        PlaceCategory.TOURS
    ))
)

@Composable
fun PlacesScreen(
    paddingValues: PaddingValues,
    viewModel: PlacesViewModel = hiltViewModel(),
    onAiSuggest: () -> Unit = {},
    onCategorySelected: (PlaceCategory) -> Unit = {},
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    var visible by remember { mutableStateOf(false) }
    var activeSectionIndex by rememberSaveable { mutableIntStateOf(0) }
    val enabledSections = if (state.showDestinations) baseCategorySections
    else baseCategorySections.filterNot { it.label == "Destinations" }
    val sections = sortSectionsForCurrentTime(enabledSections) { it.label }

    LaunchedEffect(Unit) { delay(80); visible = true }
    LaunchedEffect(sections.size) {
        if (sections.isEmpty()) return@LaunchedEffect
        if (activeSectionIndex > sections.lastIndex) activeSectionIndex = 0
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
                                        "Google Places · open now · real-time",
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

                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickIntents) { intent ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .clickable {
                                            viewModel.searchQuery(intent.query)
                                            viewModel.selectCategory(intent.category)
                                            onCategorySelected(intent.category)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        intent.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }
                            if (state.searchQuery.isNotBlank()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.White.copy(alpha = 0.16f))
                                            .clickable {
                                                onCategorySelected(state.selectedCategory)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            "🔁 ${state.searchQuery}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Two-level category tabs (Kipita-style)
            // ----------------------------------------------------------------
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { 20 }) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        if (sections.isNotEmpty()) {
                            ScrollableTabRow(
                                selectedTabIndex = activeSectionIndex,
                                edgePadding = 20.dp
                            ) {
                                sections.forEachIndexed { index, section ->
                                    Tab(
                                        selected = activeSectionIndex == index,
                                        onClick = { activeSectionIndex = index },
                                        text = {
                                            Text(
                                                section.label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    )
                                }
                            }

                            val childCategories = sections[activeSectionIndex].categories
                            val selectedChildIndex = childCategories.indexOf(state.selectedCategory).let {
                                if (it >= 0) it else 0
                            }
                            Spacer(Modifier.height(8.dp))
                            ScrollableTabRow(
                                selectedTabIndex = selectedChildIndex,
                                edgePadding = 20.dp
                            ) {
                                childCategories.forEachIndexed { index, cat ->
                                    Tab(
                                        selected = selectedChildIndex == index,
                                        onClick = {
                                            viewModel.selectCategory(cat)
                                            onCategorySelected(cat)
                                        },
                                        text = {
                                            Text(
                                                "${cat.emoji} ${cat.label}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )
                                }
                                if (sections[activeSectionIndex].label == "Services") {
                                    Tab(
                                        selected = false,
                                        onClick = {
                                            onOpenWebView(
                                                "https://btcmap.org/map",
                                                "BTCMap — Bitcoin Merchants"
                                            )
                                        },
                                        text = {
                                            Text("₿ BTCMap", style = MaterialTheme.typography.labelSmall)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Tap-to-explore prompt
            // ----------------------------------------------------------------
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 20 }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KipitaCardBg)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👆", fontSize = 28.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "1-tap quick intents or choose section + category to explore nearby places",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                "Results open in a full-screen view with real-time data from Google Places",
                                style = MaterialTheme.typography.bodySmall,
                                color = KipitaTextSecondary,
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
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

// ---------------------------------------------------------------------------
// BTCMap special chip — orange Bitcoin branding, opens btcmap.org/map in WebView
// ---------------------------------------------------------------------------
@Composable
private fun BtcMapChip(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFF6B00))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("₿", fontSize = 20.sp, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(
            "BTCMap",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1
        )
    }
}
