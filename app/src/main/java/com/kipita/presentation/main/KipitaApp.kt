package com.kipita.presentation.main

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.auth.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.scale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.presentation.ai.AiAssistantScreen
import com.kipita.presentation.auth.AuthScreen
import com.kipita.presentation.common.InAppBrowserScreen
import com.kipita.presentation.common.KipitaErrorBoundary
import com.kipita.presentation.community.NearbyTravelersScreen
import com.kipita.presentation.community.TravelGroupsScreen
import com.kipita.presentation.explore.ExploreScreen
import com.kipita.presentation.home.HomeScreen
import com.kipita.presentation.perks.PerksScreen
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
import com.kipita.data.api.PlaceCategory
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.places.PlacesCategoryResultScreen
import com.kipita.presentation.translate.TranslateScreen
import com.kipita.presentation.trips.MyTripsScreen
import com.kipita.presentation.trips.TripDetailScreen
import com.kipita.presentation.wallet.WalletScreen

// ---------------------------------------------------------------------------
// Navigation routes
// 6 bottom-tab routes: HOME | EXPLORE | AI(centre) | TRIPS | WALLET | SOCIAL
// SETTINGS is not in the nav bar — accessed via the top-right profile avatar menu
// ---------------------------------------------------------------------------
enum class MainRoute {
    HOME, EXPLORE, AI, TRIPS, WALLET, SOCIAL, SETTINGS
}

private data class NavItem(
    val route: MainRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenter: Boolean = false
)

private val navItems = listOf(
    NavItem(MainRoute.HOME,    "Home",    Icons.Filled.Home,          Icons.Outlined.Home),
    NavItem(MainRoute.EXPLORE, "Explore", Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
    NavItem(MainRoute.AI,      "AI",      Icons.Filled.AutoAwesome,   Icons.Outlined.AutoAwesome, isCenter = true),
    NavItem(MainRoute.TRIPS,   "Trips",   Icons.Filled.Flight,        Icons.Outlined.Flight),
    NavItem(MainRoute.WALLET,  "Wallet",  Icons.Filled.Wallet,        Icons.Outlined.Wallet),
    NavItem(MainRoute.SOCIAL,  "Social",  Icons.Filled.Groups,        Icons.Outlined.Groups)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KipitaApp() {
    val authVm: AuthViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var route by rememberSaveable { mutableStateOf(MainRoute.HOME) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showAuth by rememberSaveable { mutableStateOf(false) }
    var showMap by rememberSaveable { mutableStateOf(false) }
    var showTranslate by rememberSaveable { mutableStateOf(false) }
    var showWebView by rememberSaveable { mutableStateOf(false) }
    var webViewUrl by rememberSaveable { mutableStateOf("") }
    var webViewTitle by rememberSaveable { mutableStateOf("") }
    var showNearbyTravelers by rememberSaveable { mutableStateOf(false) }
    var showTravelGroups by rememberSaveable { mutableStateOf(false) }
    var aiPreFill by rememberSaveable { mutableStateOf("") }
    var isGuest by rememberSaveable { mutableStateOf(true) }
    var userName by rememberSaveable { mutableStateOf("") }
    var userAvatarUri by rememberSaveable { mutableStateOf("") }
    var showProfileMenu by rememberSaveable { mutableStateOf(false) }
    var selectedTripId by rememberSaveable { mutableStateOf<String?>(null) }
    var showPlacesResult by rememberSaveable { mutableStateOf(false) }
    var placesResultCategory by rememberSaveable { mutableStateOf(PlaceCategory.HOTELS) }
    var showPerks by rememberSaveable { mutableStateOf(false) }

    // Sync avatar from AuthViewModel whenever the current user changes
    val currentUser by authVm.currentUser.collectAsStateWithLifecycleCompat()
    LaunchedEffect(currentUser) {
        currentUser?.let {
            if (it.avatarUrl.isNotBlank()) userAvatarUri = it.avatarUrl
        }
    }

    val canGoBack = showMap || showProfile || showAuth || showTranslate || showWebView ||
        showNearbyTravelers || showTravelGroups || selectedTripId != null || showPlacesResult || showPerks
    val onBack: () -> Unit = {
        when {
            showWebView         -> showWebView = false
            showPlacesResult    -> showPlacesResult = false
            showNearbyTravelers -> showNearbyTravelers = false
            showTravelGroups    -> showTravelGroups = false
            showTranslate       -> showTranslate = false
            selectedTripId != null -> selectedTripId = null
            showPerks   -> showPerks = false
            showAuth    -> showAuth = false
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
                userAvatarUri = userAvatarUri,
                onProfileClick = { showProfileMenu = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!showMap && !showProfile && !showAuth && !showTranslate && !showWebView &&
                !showNearbyTravelers && !showTravelGroups && selectedTripId == null && !showPlacesResult && !showPerks) {
                NavigationBar(
                    containerColor = KipitaNavBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(90.dp)
                ) {
                    navItems.forEach { item ->
                        val selected = route == item.route
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.1f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "nav-scale"
                        )
                        NavigationBarItem(
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            selected = selected,
                            onClick = { route = item.route },
                            icon = {
                                if (item.isCenter) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .scale(scale)
                                            .background(
                                                color = if (selected) KipitaRed else KipitaRed.copy(alpha = 0.92f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .scale(scale)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.padding(top = 2.dp)
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
                showWebView -> KipitaErrorBoundary("InAppBrowserScreen") { _ ->
                    InAppBrowserScreen(
                        url = webViewUrl,
                        title = webViewTitle,
                        paddingValues = padding,
                        onBack = { showWebView = false }
                    )
                }

                showNearbyTravelers -> KipitaErrorBoundary("NearbyTravelersScreen") { _ ->
                    NearbyTravelersScreen(
                        paddingValues = padding,
                        onBack = { showNearbyTravelers = false }
                    )
                }

                showTravelGroups -> KipitaErrorBoundary("TravelGroupsScreen") { _ ->
                    TravelGroupsScreen(
                        paddingValues = padding,
                        onBack = { showTravelGroups = false }
                    )
                }

                showTranslate -> KipitaErrorBoundary("TranslateScreen") { _ ->
                    TranslateScreen(
                        paddingValues = padding,
                        onBack = { showTranslate = false },
                        onOpenWebView = { url, title ->
                            webViewUrl = url
                            webViewTitle = title
                            showWebView = true
                        }
                    )
                }

                selectedTripId != null -> KipitaErrorBoundary("TripDetailScreen") { _ ->
                    TripDetailScreen(
                        tripId = selectedTripId!!,
                        paddingValues = padding,
                        onBack = { selectedTripId = null },
                        onOpenWebView = { url, title ->
                            webViewUrl = url
                            webViewTitle = title
                            showWebView = true
                        },
                        onAiSuggest = { prompt ->
                            selectedTripId = null
                            aiPreFill = prompt
                            route = MainRoute.AI
                        }
                    )
                }

                showPlacesResult -> KipitaErrorBoundary("PlacesCategoryResultScreen") { _ ->
                    PlacesCategoryResultScreen(
                        category = placesResultCategory,
                        onBack = { showPlacesResult = false },
                        onOpenWebView = { url, title ->
                            webViewUrl = url
                            webViewTitle = title
                            showWebView = true
                        }
                    )
                }

                showPerks -> KipitaErrorBoundary("PerksScreen") { _ ->
                    PerksScreen(
                        paddingValues = padding,
                        onOpenWebView = { url, title ->
                            webViewUrl = url
                            webViewTitle = title
                            showWebView = true
                        }
                    )
                }

                showAuth -> KipitaErrorBoundary("AuthScreen") { _ ->
                    AuthScreen(
                        paddingValues = padding,
                        onBack = { showAuth = false },
                        onOpenWebView = { url, title ->
                            webViewUrl = url
                            webViewTitle = title
                            showWebView = true
                        },
                        onAuthSuccess = { displayName ->
                            userName = displayName
                            isGuest = false
                            showAuth = false
                        },
                        onContinueAsGuest = {
                            isGuest = true
                            showAuth = false
                        }
                    )
                }

                showMap -> KipitaErrorBoundary("MapScreen") { _ ->
                    MapScreen(
                        paddingValues = padding,
                        onNavigateBack = { showMap = false },
                        onAiSuggest = { prompt ->
                            aiPreFill = prompt
                            showMap = false
                            route = MainRoute.AI
                        }
                    )
                }

                showProfile -> KipitaErrorBoundary("ProfileSetupScreen") { _ ->
                    ProfileSetupScreen(
                        paddingValues = padding,
                        onBack = { showProfile = false },
                        onSave = { savedName, avatarUri ->
                            if (savedName.isNotBlank()) {
                                userName = savedName
                                isGuest = false
                            }
                            if (avatarUri.isNotBlank()) {
                                userAvatarUri = avatarUri
                                authVm.updateProfile(savedName, avatarUri)
                            }
                            showProfile = false
                        }
                    )
                }

                else -> Crossfade(
                    targetState = route,
                    label = "route-transition"
                ) { destination ->
                    when (destination) {
                        MainRoute.HOME -> KipitaErrorBoundary("HomeScreen") { _ ->
                            HomeScreen(
                                paddingValues    = padding,
                                onOpenWallet     = { route = MainRoute.WALLET },
                                onOpenMap        = { showMap = true },
                                onOpenAI         = { prompt -> aiPreFill = prompt; route = MainRoute.AI },
                                onOpenTranslate  = { showTranslate = true },
                                onOpenPerks      = { showPerks = true },
                                onOpenWebView    = { url, title ->
                                    webViewUrl = url
                                    webViewTitle = title
                                    showWebView = true
                                }
                            )
                        }

                        MainRoute.EXPLORE -> KipitaErrorBoundary("ExploreScreen") { _ ->
                            ExploreScreen(
                                paddingValues      = padding,
                                onAiSuggest        = { prompt -> aiPreFill = prompt; route = MainRoute.AI },
                                onOpenMap          = { showMap = true },
                                onTripClick        = { tripId -> selectedTripId = tripId },
                                onCategorySelected = { cat -> placesResultCategory = cat; showPlacesResult = true }
                            )
                        }

                        MainRoute.AI -> KipitaErrorBoundary("AiAssistantScreen") { _ ->
                            AiAssistantScreen(
                                paddingValues = padding,
                                onTripClick   = { tripId -> selectedTripId = tripId },
                                preFillPrompt = aiPreFill.also { aiPreFill = "" }
                            )
                        }

                        MainRoute.TRIPS -> KipitaErrorBoundary("MyTripsScreen") { _ ->
                            MyTripsScreen(
                                paddingValues = padding,
                                onAiSuggest  = { prompt -> aiPreFill = prompt; route = MainRoute.AI },
                                onOpenWallet = { route = MainRoute.WALLET },
                                onOpenMap    = { showMap = true },
                                onOpenWebView = { url, title ->
                                    webViewUrl = url
                                    webViewTitle = title
                                    showWebView = true
                                },
                                onTripClick  = { tripId -> selectedTripId = tripId }
                            )
                        }

                        MainRoute.WALLET -> KipitaErrorBoundary("WalletScreen") { _ ->
                            WalletScreen(
                                paddingValues = padding,
                                onOpenWebView = { url, title ->
                                    webViewUrl = url
                                    webViewTitle = title
                                    showWebView = true
                                }
                            )
                        }

                        MainRoute.SOCIAL -> KipitaErrorBoundary("SocialScreen") { _ ->
                            SocialScreen(
                                paddingValues       = padding,
                                onNearbyTravelers   = { showNearbyTravelers = true },
                                onTravelGroups      = {
                                    if (isGuest) {
                                        showAuth = true
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Create a profile to join groups")
                                        }
                                    } else {
                                        showTravelGroups = true
                                    }
                                }
                            )
                        }

                        MainRoute.SETTINGS -> KipitaErrorBoundary("SettingsScreen") { _ ->
                            SettingsScreen(
                                paddingValues = padding,
                                onOpenWebView = { url, title ->
                                    webViewUrl = url
                                    webViewTitle = title
                                    showWebView = true
                                }
                            )
                        }
                    }
                }
            }

            // Profile / account bottom sheet
            if (showProfileMenu) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showProfileMenu = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    ProfileMenuContent(
                        isGuest           = isGuest,
                        userName          = userName,
                        userAvatarUri     = userAvatarUri,
                        onSetupProfile    = { showProfileMenu = false; if (isGuest) showAuth = true else showProfile = true },
                        onContinueAsGuest = { isGuest = true; showProfileMenu = false },
                        onSignOut         = { isGuest = true; userName = ""; userAvatarUri = ""; showProfileMenu = false },
                        onSettings        = { showProfileMenu = false; route = MainRoute.SETTINGS }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Top bar — silver back button (←) + profile avatar, visible on every screen
// ---------------------------------------------------------------------------
@Composable
private fun KipitaTopBar(
    canGoBack: Boolean,
    onBack: () -> Unit,
    isGuest: Boolean,
    userName: String,
    userAvatarUri: String = "",
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
        // Silver back button — always reserves space; only visible/clickable when navigable
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canGoBack) Color(0xFFE0E0E0) else Color.Transparent)
                .then(if (canGoBack) Modifier.clickable(onClick = onBack) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (canGoBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Profile / guest circle — always visible top-right on every screen
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
            when {
                userAvatarUri.isNotBlank() -> AsyncImage(
                    model = userAvatarUri,
                    contentDescription = "Profile photo",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                isGuest || userName.isBlank() -> Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = KipitaTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                else -> Text(
                    text = userName.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = KipitaRed
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Profile menu — Sign In | Continue as Guest | User Profile | Settings
// ---------------------------------------------------------------------------
@Composable
private fun ProfileMenuContent(
    isGuest: Boolean,
    userName: String,
    userAvatarUri: String = "",
    onSetupProfile: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onSignOut: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar + name header
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
                when {
                    userAvatarUri.isNotBlank() -> AsyncImage(
                        model = userAvatarUri,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    isGuest || userName.isBlank() -> Icon(Icons.Default.Person, null, tint = KipitaTextSecondary, modifier = Modifier.size(26.dp))
                    else -> Text(
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
            ProfileMenuItem("View / Edit Profile", onClick = onSetupProfile)
            ProfileMenuItem("Settings",            onClick = onSettings)
            ProfileMenuItem("Sign Out",            onClick = onSignOut, isDestructive = true)
        }

        // Share Kipita — visible to all users (guest and signed-in)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(KipitaRedLight)
                .clickable {
                    runCatching {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Check out Kipita!")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "I've been using Kipita — the smartest travel + finance super app. Check it out: https://Kipita.com"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Kipita"))
                    }
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🚀", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Share Kipita with Friends",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KipitaRed
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileMenuItem(
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
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
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = KipitaTextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}
