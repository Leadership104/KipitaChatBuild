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
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Place
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.kipita.presentation.advisory.AdvisoryScreen
import com.kipita.presentation.home.HomeScreen
import com.kipita.presentation.home.WeatherViewModel
import com.kipita.presentation.perks.PerksScreen
import com.kipita.presentation.map.MapScreen
import com.kipita.presentation.places.PlacesScreen
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
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.translate.TranslateScreen
import com.kipita.presentation.trips.MyTripsScreen
import com.kipita.presentation.trips.TripDetailScreen
import com.kipita.presentation.wallet.WalletScreen

// ---------------------------------------------------------------------------
// Navigation routes
// Bottom-tab routes: HOME | PLACES | TRIPS | ADVISORY
// SETTINGS is not in the nav bar — accessed via the top-right profile avatar menu
// ---------------------------------------------------------------------------
enum class MainRoute {
    HOME, PLACES, AI, TRIPS, ADVISORY, SETTINGS
}

private data class NavItem(
    val route: MainRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenter: Boolean = false
)

private val navItems = listOf(
    NavItem(MainRoute.HOME,     "Home",     Icons.Filled.Home,           Icons.Outlined.Home),
    NavItem(MainRoute.ADVISORY, "Advisory", Icons.Filled.HealthAndSafety, Icons.Outlined.HealthAndSafety),
    NavItem(MainRoute.TRIPS,    "Trips",    Icons.Filled.Luggage,         Icons.Outlined.Luggage),
    NavItem(MainRoute.PLACES,   "Places",   Icons.Filled.Place,           Icons.Outlined.Place)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KipitaApp() {
    val authVm: AuthViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var route by rememberSaveable { mutableStateOf(MainRoute.PLACES) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showAuth by rememberSaveable { mutableStateOf(false) }
    var showMap by rememberSaveable { mutableStateOf(false) }
    var showTranslate by rememberSaveable { mutableStateOf(false) }
    var showWebView by rememberSaveable { mutableStateOf(false) }
    var webViewUrl by rememberSaveable { mutableStateOf("") }
    var webViewTitle by rememberSaveable { mutableStateOf("") }
    var showNearbyTravelers by rememberSaveable { mutableStateOf(false) }
    var showTravelGroups by rememberSaveable { mutableStateOf(false) }
    var showSocial by rememberSaveable { mutableStateOf(false) }
    var aiPreFill by rememberSaveable { mutableStateOf("") }
    var isGuest by rememberSaveable { mutableStateOf(true) }
    var userName by rememberSaveable { mutableStateOf("") }
    var userAvatarUri by rememberSaveable { mutableStateOf("") }
    var showProfileMenu by rememberSaveable { mutableStateOf(false) }
    var selectedTripId by rememberSaveable { mutableStateOf<String?>(null) }
    var showPerks by rememberSaveable { mutableStateOf(false) }
    var showWallet by rememberSaveable { mutableStateOf(false) }
    var openSosSignal by rememberSaveable { mutableStateOf(0) }
    val topBarWeatherViewModel: WeatherViewModel = hiltViewModel()
    val topBarWeatherState by topBarWeatherViewModel.state.collectAsStateWithLifecycleCompat()

    // Sync avatar from AuthViewModel whenever the current user changes
    val currentUser by authVm.currentUser.collectAsStateWithLifecycleCompat()
    LaunchedEffect(currentUser) {
        currentUser?.let {
            if (it.avatarUrl.isNotBlank()) userAvatarUri = it.avatarUrl
        }
    }
    LaunchedEffect(Unit) {
        topBarWeatherViewModel.refresh()
    }

    val isOnOverlay = showMap || showProfile || showAuth || showTranslate || showWebView ||
        showNearbyTravelers || showTravelGroups || showSocial || selectedTripId != null || showPerks || showWallet
    val canGoBack = isOnOverlay || route != MainRoute.HOME
    val onBack: () -> Unit = {
        when {
            showWebView            -> showWebView = false
            showNearbyTravelers    -> showNearbyTravelers = false
            showTravelGroups       -> showTravelGroups = false
            showSocial             -> showSocial = false
            showTranslate          -> showTranslate = false
            selectedTripId != null -> selectedTripId = null
            showPerks              -> showPerks = false
            showWallet             -> showWallet = false
            showAuth               -> showAuth = false
            showMap                -> showMap = false
            showProfile            -> showProfile = false
            route != MainRoute.HOME -> route = MainRoute.HOME
            else                   -> {}
        }
    }

    Scaffold(
        topBar = {
            KipitaTopBar(
                isGuest = isGuest,
                userName = userName,
                userAvatarUri = userAvatarUri,
                onProfileClick = { showProfileMenu = true },
                weatherEmoji = topBarWeatherState.current?.emoji ?: "🌤️",
                weatherTempC = topBarWeatherState.current?.temperatureC?.toInt(),
                onEmergencyClick = {
                    route = MainRoute.HOME
                    openSosSignal += 1
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!showMap && !showProfile && !showAuth && !showTranslate && !showWebView &&
                !showNearbyTravelers && !showTravelGroups && !showSocial && selectedTripId == null && !showPerks && !showWallet) {
                // Glassmorphism + 3D shadow nav bar container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.05f),
                            spotColor = Color.Black.copy(alpha = 0.09f)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.88f),
                                    Color.White.copy(alpha = 0.96f)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                ) {
                    // Subtle top highlight line — glass shimmer effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.85f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(98.dp)
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
                                onClick = {
                                    route = item.route
                                },
                                icon = {
                                    if (item.isCenter) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .scale(scale)
                                                .shadow(
                                                    elevation = if (selected) 8.dp else 2.dp,
                                                    shape = CircleShape,
                                                    spotColor = KipitaRed.copy(alpha = 0.28f),
                                                    ambientColor = KipitaRed.copy(alpha = 0.12f)
                                                )
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
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    } else {
                                        // Glassy pill behind selected icon
                                        Box(
                                            modifier = Modifier
                                                .then(
                                                    if (selected) Modifier
                                                        .shadow(
                                                            elevation = 4.dp,
                                                            shape = RoundedCornerShape(12.dp),
                                                            ambientColor = KipitaRed.copy(alpha = 0.07f),
                                                            spotColor = KipitaRed.copy(alpha = 0.13f)
                                                        )
                                                        .background(
                                                            brush = Brush.verticalGradient(
                                                                colors = listOf(
                                                                    KipitaRed.copy(alpha = 0.11f),
                                                                    KipitaRed.copy(alpha = 0.04f)
                                                                )
                                                            ),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                    else Modifier
                                                )
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label,
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .scale(scale)
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 12.sp,
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

                showSocial -> KipitaErrorBoundary("SocialScreen") { _ ->
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

                showPerks -> KipitaErrorBoundary("PerksScreen") { _ ->
                    PerksScreen(
                        paddingValues = padding,
                        onBack = { showPerks = false },
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
                                onOpenWallet     = {
                                    showWallet = true
                                },
                                onOpenMap        = { showMap = true },
                                onOpenAI         = { prompt -> aiPreFill = prompt; route = MainRoute.AI },
                                onOpenSocial     = { showSocial = true },
                                onOpenTranslate  = { showTranslate = true },
                                onOpenPerks      = { showPerks = true },
                                openSosSignal    = openSosSignal,
                                onOpenWebView    = { url, title ->
                                    webViewUrl = url
                                    webViewTitle = title
                                    showWebView = true
                                }
                            )
                        }

                        MainRoute.PLACES -> KipitaErrorBoundary("PlacesScreen") { _ ->
                            PlacesScreen(
                                paddingValues      = padding,
                                onBack             = { route = MainRoute.HOME },
                                onAskKipita        = { route = MainRoute.AI },
                                onOpenWebView      = { url, title ->
                                    webViewUrl = url
                                    webViewTitle = title
                                    showWebView = true
                                }
                            )
                        }

                        MainRoute.AI -> KipitaErrorBoundary("AiAssistantScreen") { _ ->
                            AiAssistantScreen(
                                paddingValues = padding,
                                onBack        = { route = MainRoute.HOME },
                                onTripClick   = { tripId -> selectedTripId = tripId },
                                preFillPrompt = aiPreFill.also { aiPreFill = "" }
                            )
                        }

                        MainRoute.TRIPS -> KipitaErrorBoundary("MyTripsScreen") { _ ->
                            MyTripsScreen(
                                paddingValues = padding,
                                onBack        = { route = MainRoute.HOME },
                                onAiSuggest  = { prompt -> aiPreFill = prompt; route = MainRoute.AI },
                                onOpenWallet = {
                                    showWallet = true
                                },
                                onOpenMap    = { showMap = true },
                                onOpenTranslate = { showTranslate = true },
                                onOpenWebView = { url, title ->
                                    webViewUrl = url
                                    webViewTitle = title
                                    showWebView = true
                                },
                                onTripClick  = { tripId -> selectedTripId = tripId }
                            )
                        }

                        MainRoute.ADVISORY -> KipitaErrorBoundary("AdvisoryScreen") { _ ->
                            AdvisoryScreen(
                                paddingValues = padding,
                                onBack        = { route = MainRoute.HOME }
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

            if (showWallet) {
                KipitaErrorBoundary("WalletScreen") { _ ->
                    WalletScreen(
                        paddingValues = padding,
                        onBack = { showWallet = false },
                        walletOpenSignal = 1,
                        onOpenWebView = { url, title ->
                            webViewUrl = url
                            webViewTitle = title
                            showWebView = true
                        }
                    )
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
                        onWallet          = { showProfileMenu = false; showWallet = true },
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
    isGuest: Boolean,
    userName: String,
    userAvatarUri: String = "",
    onProfileClick: () -> Unit,
    weatherEmoji: String,
    weatherTempC: Int?,
    onEmergencyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1565C0))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
                    .clickable(onClick = onEmergencyClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergency",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(weatherEmoji, fontSize = 16.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    weatherTempC?.let { "$it°C" } ?: "--°C",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.5f),
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
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    else -> Text(
                        text = userName.first().uppercaseChar().toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
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
    onWallet: () -> Unit,
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
            ProfileMenuItem("Wallet",              onClick = onWallet)
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
