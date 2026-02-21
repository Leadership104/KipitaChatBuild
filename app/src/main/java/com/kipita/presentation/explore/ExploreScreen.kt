package com.kipita.presentation.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.domain.model.ExploreDestination
import com.kipita.domain.model.SampleData
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay

@Composable
fun ExploreScreen(paddingValues: PaddingValues) {
    var visible by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var gridView by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // Top controls bar
        AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
            Column(modifier = Modifier.background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Live button
                    LiveIndicatorButton(isLive = isLive, onClick = { isLive = !isLive })

                    Spacer(Modifier.weight(1f))

                    // Filter button
                    Surface(
                        modifier = Modifier
                            .border(1.5.dp, KipitaRed, RoundedCornerShape(20.dp))
                            .clickable {},
                        color = Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Filters", style = MaterialTheme.typography.labelMedium, color = KipitaRed)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Add FAB
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(KipitaRed)
                            .clickable {},
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Search bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search destinations...", color = KipitaTextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KipitaTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KipitaRed,
                        unfocusedBorderColor = KipitaBorder
                    )
                )

                Spacer(Modifier.height(8.dp))

                // View controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ViewToggleChip(
                            icon = Icons.Default.GridView,
                            label = "Grid",
                            selected = gridView,
                            onClick = { gridView = true }
                        )
                        ViewToggleChip(
                            icon = Icons.Default.Sort,
                            label = "Sort",
                            selected = false,
                            onClick = {}
                        )
                    }
                    Text(
                        text = "${SampleData.destinations.size} destinations",
                        style = MaterialTheme.typography.labelSmall,
                        color = KipitaTextTertiary
                    )
                }

                // Data source pills
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DataSourcePill("Nomad List", "💻")
                    DataSourcePill("Open-Meteo", "🌡")
                    DataSourcePill("BTCMap", "₿")
                    DataSourcePill("ECB Rates", "💱")
                }
            }
        }

        // Destination list
        val filtered = if (searchText.isBlank()) SampleData.destinations
        else SampleData.destinations.filter {
            it.city.contains(searchText, ignoreCase = true) || it.country.contains(searchText, ignoreCase = true)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(filtered) { index, dest ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(200 + index * 80)) + slideInVertically(tween(200 + index * 80)) { 40 }
                ) {
                    DestinationCard(destination = dest, index = index)
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

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
private fun LiveIndicatorButton(isLive: Boolean, onClick: () -> Unit) {
    var pulse by remember { mutableStateOf(1f) }
    LaunchedEffect(isLive) {
        if (isLive) {
            while (true) {
                pulse = 1.15f
                delay(600)
                pulse = 1f
                delay(600)
            }
        }
    }
    val scale by animateFloatAsState(pulse, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "live-pulse")

    Surface(
        modifier = Modifier
            .scale(scale)
            .clip(CircleShape)
            .shadow(if (isLive) 4.dp else 0.dp, CircleShape)
            .clickable(onClick = onClick),
        color = if (isLive) KipitaRed else KipitaCardBg,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isLive) Color.White else KipitaTextSecondary)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Live",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isLive) Color.White else KipitaTextSecondary
            )
        }
    }
}

@Composable
private fun ViewToggleChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (selected) KipitaRedLight else KipitaCardBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) KipitaRed else KipitaTextSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) KipitaRed else KipitaTextSecondary)
        }
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
            // Image area with gradient + badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Brush.linearGradient(colors = gradient))
            ) {
                // Rank badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f)
                ) {
                    Text(
                        text = "#${destination.rank}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = KipitaOnSurface
                    )
                }

                // WiFi badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = KipitaGreenAccent, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "${destination.wifiSpeedMbps} Mbps",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaGreenAccent
                        )
                    }
                }

                // Popular badge
                if (destination.isPopular) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "Popular",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface
                        )
                    }
                }

                // City name overlay
                Text(
                    text = "${destination.weatherIcon}",
                    fontSize = 36.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Card content
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = destination.city,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = KipitaOnSurface
                        )
                        Text(
                            text = destination.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${destination.costPerMonthUsd} / mo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = KipitaOnSurface
                        )
                        Text(
                            text = "nomad cost",
                            style = MaterialTheme.typography.labelSmall,
                            color = KipitaTextTertiary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Weather + tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = destination.weatherSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = KipitaTextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        destination.tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = KipitaCardBg
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KipitaTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
