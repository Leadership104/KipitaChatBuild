package com.kipita.presentation.advisory

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kipita.BuildConfig
import com.kipita.domain.model.NoticeCategory
import com.kipita.domain.model.SeverityLevel
import com.kipita.domain.model.TravelNotice
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Brush

@Composable
fun AdvisoryScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit = {},
    viewModel: AdvisoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val tabs = listOf(NoticeCategory.ADVISORY, NoticeCategory.SAFETY, NoticeCategory.HEALTH)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(paddingValues)
    ) {
        // Dark navy header — Places-style
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
                        "Travel Advisory",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        "Safety · Health · Alerts",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tabs.forEach { tab ->
                AdvisoryTabButton(
                    modifier = Modifier.weight(1f),
                    label = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = tab == state.selectedTab,
                    onClick = { viewModel.selectTab(tab) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.tabbedNotices) { notice ->
                    AdvisoryNoticeCard(notice = notice)
                }
            }
        }
    }
}

@Composable
private fun AdvisoryTabButton(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(if (selected) Color(0xFF1A1A2E) else Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) Color(0xFF1A1A2E) else Color(0xFFE2E2E2), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) Color.White else Color(0xFF1A1A1A)
        )
    }
}

@Composable
private fun AdvisoryNoticeCard(notice: TravelNotice) {
    val severityColor = when (notice.severity) {
        SeverityLevel.CRITICAL -> Color(0xFFC62828)
        SeverityLevel.HIGH -> Color(0xFFEF6C00)
        SeverityLevel.MEDIUM -> Color(0xFFF9A825)
        SeverityLevel.LOW -> Color(0xFF2E7D32)
    }
    val timeText = rememberNoticeTime(notice)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFEAEAEA), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = staticMapImageUrl(notice),
            contentDescription = "${notice.title} location preview",
            modifier = Modifier
                .width(120.dp)
                .height(100.dp)
                .background(Color(0xFFF1F1F1), RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                notice.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                maxLines = 3,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(severityColor.copy(alpha = 0.14f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(notice.severity.name, color = severityColor, style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    "Updated $timeText",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666)
                )
                if (notice.verified) {
                    Text("Verified", color = Color(0xFF2E7D32), fontSize = 11.sp)
                }
            }
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF757575)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    notice.sourceName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun rememberNoticeTime(notice: TravelNotice): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
    return notice.lastUpdated.atZone(ZoneId.systemDefault()).format(formatter)
}

private fun staticMapImageUrl(notice: TravelNotice): String {
    val lat = notice.location.latitude
    val lon = notice.location.longitude
    return "https://maps.googleapis.com/maps/api/staticmap?center=$lat,$lon&zoom=11&size=320x240&markers=color:red|$lat,$lon&key=${BuildConfig.GOOGLE_PLACES_API_KEY}"
}
