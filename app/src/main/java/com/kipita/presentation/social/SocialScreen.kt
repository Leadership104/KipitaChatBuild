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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.data.local.DirectMessageEntity
import com.kipita.domain.model.CommunityGroup
import com.kipita.domain.model.NearbyTraveler
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SocialScreen(paddingValues: PaddingValues, viewModel: SocialViewModel = hiltViewModel()) {
    var visible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var openConversationId by remember { mutableStateOf<String?>(null) }
    var openConversationName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { delay(80); visible = true }

    if (openConversationId != null) {
        MessageThreadScreen(
            conversationId = openConversationId!!,
            conversationName = openConversationName,
            onBack = { openConversationId = null },
            viewModel = viewModel
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(paddingValues)) {
        // Header
        AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
            Column(modifier = Modifier.background(Color.White).padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Community", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = KipitaOnSurface)
                        Text("Connect with travelers worldwide", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Offline badge
                        Row(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(KipitaCardBg).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = KipitaGreenAccent, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Offline", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = KipitaGreenAccent)
                        }
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(KipitaRedLight).clickable {}, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchText, onValueChange = { searchText = it },
                    placeholder = { Text("Search groups or travelers...", color = KipitaTextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KipitaTextSecondary) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KipitaRed, unfocusedBorderColor = KipitaBorder)
                )
            }
        }

        // Tabs
        AnimatedVisibility(visible = visible, enter = fadeIn()) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = KipitaRed,
                indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = KipitaRed) }) {
                listOf("Groups", "Travelers", "Messages").forEachIndexed { i, label ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                        text = { Text(label, style = MaterialTheme.typography.labelLarge, color = if (selectedTab == i) KipitaRed else KipitaTextSecondary) })
                }
            }
        }

        when (selectedTab) {
            0 -> GroupsTab(visible, searchText, onOpenGroup = { id, name -> openConversationId = id; openConversationName = name })
            1 -> TravelersTab(visible)
            2 -> DirectMessagesTab(visible, onOpenDm = { id, name -> openConversationId = id; openConversationName = name })
        }
    }
}

@Composable
private fun GroupsTab(visible: Boolean, searchText: String, onOpenGroup: (String, String) -> Unit) {
    val groups = if (searchText.isBlank()) SampleData.communityGroups
    else SampleData.communityGroups.filter { it.name.contains(searchText, ignoreCase = true) }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 30 }) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("Connect With Travelers", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface, modifier = Modifier.padding(bottom = 10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConnectCard(Icons.Default.NearMe, "Find nearby travelers", Modifier.weight(1f))
                        ConnectCard(Icons.Default.Group, "Join travel groups", Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Text("Community Groups", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        itemsIndexed(groups) { index, group ->
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(150 + index * 60)) + slideInHorizontally(tween(150 + index * 60)) { -30 }) {
                GroupRow(group = group, onClick = { onOpenGroup(group.id, group.name) })
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ConnectCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "connect-scale")
    Column(modifier = modifier.scale(scale).shadow(3.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
        .background(Color.White).clickable { pressed = !pressed }.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(KipitaRedLight), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = KipitaOnSurface, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun GroupRow(group: CommunityGroup, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(KipitaCardBg), contentAlignment = Alignment.Center) {
            Text(group.avatarEmoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(group.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
                Spacer(Modifier.width(6.dp))
                Text(group.location, style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
            }
            Text(group.lastMessage, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            if (group.unreadCount > 0) {
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(KipitaRed), contentAlignment = Alignment.Center) {
                    Text("${group.unreadCount}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, fontSize = 10.sp)
                }
                Spacer(Modifier.height(4.dp))
            }
            Text("${group.memberCount}", style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
        }
    }
}

@Composable
private fun TravelersTab(visible: Boolean) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Nearby Travelers", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface, modifier = Modifier.padding(bottom = 4.dp)) }
        itemsIndexed(SampleData.nearbyTravelers) { index, traveler ->
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(100 + index * 80)) + slideInVertically(tween(100 + index * 80)) { 30 }) {
                TravelerCard(traveler)
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun TravelerCard(traveler: NearbyTraveler) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "traveler-scale")
    Row(modifier = Modifier.fillMaxWidth().scale(scale).shadow(2.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
        .background(Color.White).clickable { pressed = !pressed }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(KipitaCardBg), contentAlignment = Alignment.Center) {
            Text(traveler.name.first().toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = KipitaOnSurface)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(traveler.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
            Text(traveler.currentCity, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
            Text(traveler.travelStyle, style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
        }
        Surface(modifier = Modifier.clickable {}, shape = RoundedCornerShape(8.dp), color = KipitaRedLight) {
            Text("Connect", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaRed)
        }
    }
}

@Composable
private fun DirectMessagesTab(visible: Boolean, onOpenDm: (String, String) -> Unit) {
    val sampleDms = listOf(
        Triple("dm-alex", "Alex Chen", "Arriving in Lisbon tmrw! Any coworking recs?"),
        Triple("dm-sofia", "Sofia Martins", "The Fab Cafe in Shibuya accepts Bitcoin ₿"),
        Triple("dm-james", "James Park", "Group trip to Bali — you in? 🌴")
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn()) {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(KipitaCardBg).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(KipitaRedLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Offline Messaging", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
                    Text("Messages sync when back online. Works everywhere — even on a mountain.", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary, modifier = Modifier.padding(top = 6.dp), lineHeight = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(KipitaRed).clickable {}.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("New Message", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        item { Text("Recent Messages", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface) }
        itemsIndexed(sampleDms) { index, (id, name, preview) ->
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(100 + index * 70)) + slideInVertically(tween(100 + index * 70)) { 20 }) {
                DmRow(id = id, name = name, preview = preview, onClick = { onOpenDm(id, name) })
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun DmRow(id: String, name: String, preview: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(KipitaRedLight), contentAlignment = Alignment.Center) {
            Text(name.first().toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = KipitaRed)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
            Text(preview, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudOff, contentDescription = "Offline ready", tint = KipitaGreenAccent, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun MessageThreadScreen(conversationId: String, conversationName: String, onBack: () -> Unit, viewModel: SocialViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycleCompat()
    val sending by viewModel.sendingMessage.collectAsStateWithLifecycleCompat()
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) { viewModel.loadMessages(conversationId); viewModel.markRead(conversationId) }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).imePadding()) {
        // Thread header
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KipitaOnSurface) }
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(KipitaRedLight), contentAlignment = Alignment.Center) {
                Text(conversationName.first().toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KipitaRed)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(conversationName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = KipitaGreenAccent, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Offline enabled", style = MaterialTheme.typography.labelSmall, color = KipitaGreenAccent)
                }
            }
        }

        // Messages list
        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(msg)
            }
        }

        // Input bar
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(KipitaCardBg).padding(horizontal = 14.dp, vertical = 10.dp)) {
                BasicTextField(
                    value = draft, onValueChange = { draft = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = KipitaOnSurface),
                    cursorBrush = SolidColor(KipitaRed),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) Text("Message...", style = MaterialTheme.typography.bodyMedium, color = KipitaTextTertiary)
                        else inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(if (draft.isNotBlank()) KipitaRed else KipitaCardBg)
                .clickable(enabled = draft.isNotBlank() && !sending) {
                    viewModel.sendMessage(conversationId, draft)
                    draft = ""
                }, contentAlignment = Alignment.Center) {
                if (sending) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Send, contentDescription = "Send", tint = if (draft.isNotBlank()) Color.White else KipitaTextTertiary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: DirectMessageEntity) {
    val isMe = msg.senderId == "current-user"
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = fmt.format(Date(msg.createdAtEpochMillis))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
        if (!isMe) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(KipitaRedLight), contentAlignment = Alignment.Center) {
                Text(msg.senderName.first().toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KipitaRed)
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe) {
                Text(msg.senderName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = KipitaTextSecondary, modifier = Modifier.padding(bottom = 3.dp))
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isMe) 16.dp else 4.dp, bottomEnd = if (isMe) 4.dp else 16.dp))
                .background(if (isMe) KipitaRed else Color.White).padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(msg.content, style = MaterialTheme.typography.bodyMedium, color = if (isMe) Color.White else KipitaOnSurface)
            }
            Row(modifier = Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(timeStr, style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
                if (msg.isOffline) {
                    Icon(Icons.Default.CloudOff, contentDescription = "Pending sync", tint = KipitaTextTertiary, modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}
