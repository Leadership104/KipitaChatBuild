package com.kipita.presentation.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.presentation.ai.AiAssistantScreen
import com.kipita.presentation.explore.ExploreScreen
import com.kipita.presentation.map.MapScreen
import com.kipita.presentation.profile.ProfileSetupScreen
import com.kipita.presentation.settings.SettingsScreen
import com.kipita.presentation.social.SocialScreen
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaNavBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import com.kipita.presentation.trips.MyTripsScreen
import com.kipita.presentation.wallet.WalletScreen

// ---------------------------------------------------------------------------
// Navigation routes — 6 tabs, AI in the center (position 3)
//   Trips | Explore | AI | Social | Wallet | Settings
// ---------------------------------------------------------------------------
enum class MainRoute {
    TRIPS, EXPLORE, AI, SOCIAL, WALLET, SETTINGS
}

private data class NavItem(
    val route: MainRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenter: Boolean = false
)

private val navItems = listOf(
    NavItem(MainRoute.TRIPS,    "Trips",    Icons.Filled.Flight,        Icons.Outlined.Flight),
    NavItem(MainRoute.EXPLORE,  "Explore",  Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
    NavItem(MainRoute.AI,       "AI",       Icons.Filled.AutoAwesome,   Icons.Outlined.AutoAwesome,  isCenter = true),
    NavItem(MainRoute.SOCIAL,   "Social",   Icons.Filled.Groups,        Icons.Outlined.Groups),
    NavItem(MainRoute.WALLET,   "Wallet",   Icons.Filled.Wallet,        Icons.Outlined.Wallet),
    NavItem(MainRoute.SETTINGS, "Settings", Icons.Filled.Settings,      Icons.Outlined.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KipitaApp() {
    var route by rememberSaveable { mutableStateOf(MainRoute.TRIPS) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showMap by rememberSaveable { mutableStateOf(false) }
    var aiPreFill by rememberSaveable { mutableStateOf("") }
    // User state
    var isGuest by rememberSaveable { mutableStateOf(true) }
    var userName by rememberSaveable { mutableStateOf("") }
    var showProfileMenu by rememberSaveable { mutableStateOf(false) }

    // Determine if a back-navigation is available
    val canGoBack = showMap || showProfile
    val onBack: () -> Unit = {
        when {
            showMap     -> showMap = false
            showProfile -> showProfile = false
            else        -> {}
        }
    }

    Scaffold(
        topBar = {
            KipitaTopBar(
                canGoBack = canGoBack,
                onBack = onBack,
                isGuest = isGuest,
                userName = userName,
                onProfileClick = { showProfileMenu = true }
            )
        },
        bottomBar = {
            if (!showMap && !showProfile) {
                NavigationBar(
                    containerColor = KipitaNavBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(68.dp)
                ) {
                    navItems.forEach { item ->
                        val selected = route == item.route
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.1f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "nav-scale"
                        )
                        NavigationBarItem(
                            selected = selected,
                            onClick = { route = item.route },
                            icon = {
                                if (item.isCenter && !selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .scale(scale)
                                            .background(KipitaRed, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            item.unselectedIcon,
                                            contentDescription = item.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp).scale(scale)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = KipitaRed,
                                selectedTextColor = KipitaRed,
                                unselectedIconColor = KipitaTextTertiary,
                                unselectedTextColor = KipitaTextTertiary,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
        ) {
            when {
                showMap -> MapScreen(
                    paddingValues = padding,
                    onNavigateBack = { showMap = false },
                    onAiSuggest = { prompt -> aiPreFill = prompt; showMap = false; route = MainRoute.AI }
                )
                showProfile -> ProfileSetupScreen(
                    paddingValues = padding,
                    onBack = { showProfile = false },
                    onSave = { savedName ->
                        if (savedName.isNotBlank()) {
                            userName = savedName
                            isGuest = false
                        }
                        showProfile = false
                    }
                )
                else -> Crossfade(targetState = route, label = "route-transition") { destination ->
                    when (destination) {
                        MainRoute.TRIPS   -> MyTripsScreen(
                            paddingValues = padding,
                            onAiSuggest = { prompt -> aiPreFill = prompt; route = MainRoute.AI },
                            onOpenWallet = { route = MainRoute.WALLET },
                            onOpenMap    = { showMap = true }
                        )
                        MainRoute.EXPLORE -> ExploreScreen(
                            paddingValues = padding,
                            onAiSuggest = { prompt -> aiPreFill = prompt; route = MainRoute.AI },
                            onOpenMap   = { showMap = true }
                        )
                        MainRoute.AI      -> AiAssistantScreen(
                            paddingValues = padding,
                            preFillPrompt = aiPreFill.also { aiPreFill = "" }
                        )
                        MainRoute.SOCIAL    -> SocialScreen(padding)
                        MainRoute.WALLET    -> WalletScreen(padding)
                        MainRoute.SETTINGS  -> SettingsScreen(paddingValues = padding)
                    }
                }
            }

            // Profile menu sheet
            if (showProfileMenu) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showProfileMenu = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    ProfileMenuContent(
                        isGuest = isGuest,
                        userName = userName,
                        onSetupProfile = { showProfileMenu = false; showProfile = true },
                        onContinueAsGuest = { isGuest = true; showProfileMenu = false },
                        onSignOut = { isGuest = true; userName = ""; showProfileMenu = false },
                        onSettings = { showProfileMenu = false; route = MainRoute.SETTINGS }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Minimal top bar with silver back button + profile circle
// ---------------------------------------------------------------------------
@Composable
private fun KipitaTopBar(
    canGoBack: Boolean,
    onBack: () -> Unit,
    isGuest: Boolean,
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Silver back button — minimal, always present but dimmed when no back target
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (canGoBack) Color(0xFFE0E0E0) else Color.Transparent
                )
                .then(
                    if (canGoBack) Modifier.clickable(onClick = onBack) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (canGoBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // User profile / guest circle at top right
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isGuest) KipitaCardBg else KipitaRedLight)
                .border(
                    width = 1.5.dp,
                    color = if (isGuest) KipitaBorder else KipitaRed,
                    shape = CircleShape
                )
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (isGuest || userName.isBlank()) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = KipitaTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = userName.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = KipitaRed
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Profile menu bottom sheet content
// ---------------------------------------------------------------------------
@Composable
private fun ProfileMenuContent(
    isGuest: Boolean,
    userName: String,
    onSetupProfile: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onSignOut: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar + name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isGuest) KipitaCardBg else KipitaRedLight)
                    .border(2.dp, if (isGuest) KipitaBorder else KipitaRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isGuest || userName.isBlank()) {
                    Icon(Icons.Default.Person, null, tint = KipitaTextSecondary, modifier = Modifier.size(26.dp))
                } else {
                    Text(
                        userName.first().uppercaseChar().toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = KipitaRed
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = if (isGuest) "Guest" else userName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaOnSurface
                )
                Text(
                    text = if (isGuest) "Browsing without an account" else "Signed in",
                    style = MaterialTheme.typography.bodySmall,
                    color = KipitaTextSecondary
                )
            }
        }

        if (isGuest) {
            // Sign in / Create Profile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KipitaRed)
                    .clickable(onClick = onSetupProfile)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sign In / Create Profile",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
            // Continue as guest
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, KipitaBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onContinueAsGuest)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Continue as Guest",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaTextSecondary
                )
            }
        } else {
            // Profile info options
            ProfileMenuItem("View / Edit Profile", onClick = onSetupProfile)
            ProfileMenuItem("Settings", onClick = onSettings)
            ProfileMenuItem("Sign Out", onClick = onSignOut, isDestructive = true)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileMenuItem(label: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KipitaCardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isDestructive) Color(0xFFE53935) else KipitaOnSurface
        )
        Icon(
            Icons.Default.ArrowBack, // reuse icon rotated 180 as chevron right
            contentDescription = null,
            tint = KipitaTextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}
