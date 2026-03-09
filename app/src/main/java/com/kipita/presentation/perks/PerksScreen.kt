package com.kipita.presentation.perks

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.data.api.PerkItem
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary

@Composable
fun PerksScreen(
    paddingValues: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    onOpenWebView: (url: String, title: String) -> Unit,
    viewModel: PerksViewModel = hiltViewModel()
) {
    val dynamicPerks by viewModel.dynamicPerks.collectAsStateWithLifecycleCompat()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycleCompat()
    val staticPerks = viewModel.staticPerks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(paddingValues)
    ) {
        // Dark navy header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C))))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column {
                    Text(
                        "Travel Deals",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        "Flights · Hotels · Car rentals and more",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Static planner deals
        item {
            Text(
                text = "Book Your Trip",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = KipitaOnSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(staticPerks) { perk ->
            PerkCard(perk = perk, onOpenWebView = onOpenWebView)
        }

        // Dynamic affiliate perks
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KipitaRed, modifier = Modifier.size(32.dp))
                }
            }
        } else if (dynamicPerks.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Partner Deals & Perks",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KipitaOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(dynamicPerks) { perk ->
                PerkCard(perk = perk, onOpenWebView = onOpenWebView)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
    } // end outer Column
}

@Composable
private fun PerkCard(
    perk: PerkItem,
    onOpenWebView: (url: String, title: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(enabled = perk.link.isNotBlank()) {
                onOpenWebView(perk.link.trim(), perk.name)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji / icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KipitaRedLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (perk.image.length <= 4 && perk.image.isNotBlank()) perk.image else "🎁",
                fontSize = 22.sp
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = perk.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = KipitaOnSurface
            )
            if (perk.description.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = perk.description,
                    fontSize = 13.sp,
                    color = KipitaTextSecondary,
                    maxLines = 2
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "→",
            fontSize = 18.sp,
            color = KipitaRed,
            fontWeight = FontWeight.Bold
        )
    }
}
