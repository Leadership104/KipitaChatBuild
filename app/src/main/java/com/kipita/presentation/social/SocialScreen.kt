package com.kipita.presentation.social

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.domain.model.CommunityGroup
import com.kipita.domain.model.NearbyTraveler
import com.kipita.domain.model.SampleData
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay

@Composable
fun SocialScreen(paddingValues: PaddingValues) {
    var visible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }

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
        // Header
        AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Community",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = KipitaOnSurface
                        )
                        Text(
                            text = "Connect with travelers worldwide",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KipitaRedLight)
                            .clickable {},
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search groups or travelers...", color = KipitaTextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KipitaTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KipitaRed,
                        unfocusedBorderColor = KipitaBorder
                    )
                )
            }
        }

        // Tabs
        AnimatedVisibility(visible = visible, enter = fadeIn()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = KipitaRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = KipitaRed
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Groups",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 0) KipitaRed else KipitaTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Travelers",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 1) KipitaRed else KipitaTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Messages",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 2) KipitaRed else KipitaTextSecondary
                        )
                    }
                )
            }
        }

        // Content
        when (selectedTab) {
            0 -> GroupsTab(visible = visible, searchText = searchText)
            1 -> TravelersTab(visible = visible)
            2 -> MessagesTab(visible = visible)
        }
    }
}

@Composable
private fun GroupsTab(visible: Boolean, searchText: String) {
    val groups = if (searchText.isBlank()) SampleData.communityGroups
    else SampleData.communityGroups.filter { it.name.contains(searchText, ignoreCase = true) }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Connect cards
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 30 }) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Connect With Travelers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = KipitaOnSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConnectCard(
                            icon = Icons.Default.NearMe,
                            title = "Find nearby travelers",
                            modifier = Modifier.weight(1f)
                        )
                        ConnectCard(
                            icon = Icons.Default.Group,
                            title = "Join travel groups",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Community Groups",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(groups) { index, group ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(150 + index * 60)) + slideInHorizontally(tween(150 + index * 60)) { -30 }
            ) {
                GroupRow(group = group)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ConnectCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "connect-scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { pressed = !pressed }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KipitaRedLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = KipitaOnSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun GroupRow(group: CommunityGroup) {
    var pressed by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { pressed = !pressed }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(group.avatarEmoji, fontSize = 22.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = group.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = KipitaTextSecondary
                )
            }
            Text(
                text = group.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            if (group.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(KipitaRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${group.unreadCount}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = "${group.memberCount}",
                style = MaterialTheme.typography.labelSmall,
                color = KipitaTextTertiary
            )
        }
    }
}

@Composable
private fun TravelersTab(visible: Boolean) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Nearby Travelers",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        itemsIndexed(SampleData.nearbyTravelers) { index, traveler ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(100 + index * 80)) + slideInVertically(tween(100 + index * 80)) { 30 }
            ) {
                TravelerCard(traveler = traveler)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun TravelerCard(traveler: NearbyTraveler) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "traveler-scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { pressed = !pressed }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(KipitaCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(traveler.name.first().toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = KipitaOnSurface)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = traveler.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaOnSurface
            )
            Text(
                text = traveler.currentCity,
                style = MaterialTheme.typography.bodySmall,
                color = KipitaTextSecondary
            )
            Text(
                text = traveler.travelStyle,
                style = MaterialTheme.typography.labelSmall,
                color = KipitaTextTertiary
            )
        }

        Surface(
            modifier = Modifier.clickable {},
            shape = RoundedCornerShape(8.dp),
            color = KipitaRedLight
        ) {
            Text(
                text = "Connect",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaRed
            )
        }
    }
}

@Composable
private fun MessagesTab(visible: Boolean) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(KipitaCardBg)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(KipitaRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Group Trip Chat",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = KipitaOnSurface
                    )
                    Text(
                        text = "Plan trips together with up to 10 people. AI helps coordinate itineraries in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KipitaTextSecondary,
                        modifier = Modifier.padding(top = 6.dp),
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(KipitaRed)
                            .clickable {}
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Group Chat",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
