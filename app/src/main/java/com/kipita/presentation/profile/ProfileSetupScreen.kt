package com.kipita.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay

private val travelStyleOptions = listOf(
    "Budget Explorer", "Luxury Nomad", "Adventure Seeker", "Cultural Immersion",
    "Digital Nomad", "Solo Traveler", "Group Trips", "Family Travel",
    "Business Travel", "Weekend Getaways", "Long-Term Travel", "Backpacker"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit = {},
    onSave: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var isGroup by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var homeCity by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var selectedStyles by remember { mutableStateOf(setOf<String>()) }
    var setupComplete by remember { mutableStateOf(false) }
    // Avatar photo picker
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) avatarUri = uri }
    // Support form state
    var supportText by remember { mutableStateOf("") }
    var supportSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp, ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
                    Column {
                        Text(
                            text = "Set Up Profile",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = KipitaOnSurface
                        )
                        Text(
                            text = "Tell the community about you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KipitaTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Profile type selector
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 20 }) {
                    Column {
                        Text(
                            text = "Profile Type",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ProfileTypeCard(
                                icon = Icons.Default.Person,
                                title = "Individual",
                                description = "Personal travel profile",
                                selected = !isGroup,
                                onClick = { isGroup = false },
                                modifier = Modifier.weight(1f)
                            )
                            ProfileTypeCard(
                                icon = Icons.Default.Group,
                                title = "Group",
                                description = "Family, friends or team",
                                selected = isGroup,
                                onClick = { isGroup = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Avatar
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 30 }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box {
                            // Avatar circle — shows picked photo or default icon
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(KipitaCardBg)
                                    .border(2.dp, if (avatarUri != null) KipitaRed else KipitaBorder, CircleShape)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUri != null) {
                                    AsyncImage(
                                        model = avatarUri,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        if (isGroup) Icons.Default.Group else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = KipitaTextSecondary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            // Camera badge — opens image picker
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(KipitaRed)
                                    .align(Alignment.BottomEnd)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Pick photo", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (avatarUri != null) "Change photo" else "Add photo",
                            style = MaterialTheme.typography.labelMedium,
                            color = KipitaRed,
                            modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
                        )
                    }
                }
            }

            // Form fields
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 40 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (isGroup) {
                            KipitaTextField(
                                value = groupName,
                                onValueChange = { groupName = it },
                                label = "Group Name",
                                placeholder = "The Adventure Crew"
                            )
                        }
                        KipitaTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Display Name",
                            placeholder = if (isGroup) "Public group name" else "Your full name"
                        )
                        KipitaTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Username",
                            placeholder = "@username"
                        )
                        KipitaTextField(
                            value = homeCity,
                            onValueChange = { homeCity = it },
                            label = "Home City",
                            placeholder = "Where are you based?"
                        )
                        KipitaTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = "Bio",
                            placeholder = "Tell travelers about yourself...",
                            singleLine = false,
                            minLines = 3
                        )
                    }
                }
            }

            // Travel Style
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 50 }) {
                    Column {
                        Text(
                            text = "Travel Style",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Select all that apply",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            travelStyleOptions.forEach { style ->
                                val selected = style in selectedStyles
                                StyleChip(
                                    label = style,
                                    selected = selected,
                                    onClick = {
                                        selectedStyles = if (selected) {
                                            selectedStyles - style
                                        } else {
                                            selectedStyles + style
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Save button
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 60 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(KipitaRed)
                                .clickable {
                                    setupComplete = true
                                    onSave(displayName.ifBlank { groupName })
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (setupComplete) "Profile Saved!" else "Save Profile",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }

                        if (setupComplete) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF43A047), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Profile set up successfully",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF43A047)
                                )
                            }
                        }
                    }
                }
            }

            // ── Support & Feedback section ─────────────────────────────────
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 70 }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .background(KipitaCardBg)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.HeadsetMic,
                                contentDescription = null,
                                tint = KipitaRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Support & Feedback",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = KipitaOnSurface
                            )
                        }
                        Text(
                            "Found a bug or have feedback? Describe it below — your report will be sent directly to info@kipita.com.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = supportText,
                            onValueChange = { supportText = it; supportSent = false },
                            placeholder = { Text("Describe the issue or share your feedback...", color = KipitaTextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            singleLine = false,
                            minLines = 3,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KipitaRed,
                                unfocusedBorderColor = KipitaBorder
                            )
                        )

                        Spacer(Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .background(if (supportText.isNotBlank()) KipitaRed else KipitaBorder)
                                .clickable(enabled = supportText.isNotBlank()) {
                                    val subject = "Kipita App — Bug/Feedback Report"
                                    val body = supportText
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:info@kipita.com")
                                        putExtra(Intent.EXTRA_SUBJECT, subject)
                                        putExtra(Intent.EXTRA_TEXT, body)
                                    }
                                    runCatching { context.startActivity(intent) }
                                    supportSent = true
                                    supportText = ""
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Send to info@kipita.com",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        }

                        if (supportSent) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF43A047),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Report sent! Thank you.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF43A047)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ProfileTypeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        if (selected) 1f else 0.97f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "type-scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) KipitaRedLight else Color.White)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) KipitaRed else KipitaBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) KipitaRed else KipitaCardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (selected) Color.White else KipitaTextSecondary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) KipitaRed else KipitaOnSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = KipitaTextSecondary
            )
        }
    }
}

@Composable
private fun KipitaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = KipitaOnSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = KipitaTextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KipitaRed,
                unfocusedBorderColor = KipitaBorder
            )
        )
    }
}

@Composable
private fun StyleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) KipitaRed else KipitaBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        color = if (selected) KipitaRedLight else Color.White,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                color = if (selected) KipitaRed else KipitaTextSecondary
            )
        }
    }
}
