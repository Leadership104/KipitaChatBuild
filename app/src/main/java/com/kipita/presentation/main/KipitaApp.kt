package com.kipita.presentation.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.presentation.ai.AiAssistantScreen
import com.kipita.presentation.explore.ExploreScreen
import com.kipita.presentation.map.MapScreen
import com.kipita.presentation.places.PlacesScreen
import com.kipita.presentation.profile.ProfileSetupScreen
import com.kipita.presentation.social.SocialScreen
import com.kipita.presentation.trips.MyTripsScreen
import com.kipita.presentation.wallet.WalletScreen
import com.kipita.presentation.theme.KipitaNavBg
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextTertiary

// ---------------------------------------------------------------------------
// Navigation routes
// ---------------------------------------------------------------------------
enum class MainRoute {
    TRIPS, EXPLORE, PLACES, MAP, AI, WALLET
}

private data class NavItem(
    val route: MainRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navItems = listOf(
    NavItem(MainRoute.TRIPS,   "Trips",   Icons.Filled.Flight,        Icons.Outlined.Flight),
    NavItem(MainRoute.EXPLORE, "Explore", Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
    NavItem(MainRoute.PLACES,  "Places",  Icons.Filled.LocationOn,    Icons.Outlined.LocationOn),
    NavItem(MainRoute.MAP,     "Map",     Icons.Filled.Map,           Icons.Outlined.Map),
    NavItem(MainRoute.AI,      "AI",      Icons.Filled.AutoAwesome,   Icons.Outlined.AutoAwesome),
    NavItem(MainRoute.WALLET,  "Wallet",  Icons.Filled.Wallet,        Icons.Outlined.Wallet)
)

@Composable
fun KipitaApp() {
    var route by rememberSaveable { mutableStateOf(MainRoute.TRIPS) }
    var showProfile by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
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
                        label = "nav-icon-scale"
                    )

                    NavigationBarItem(
                        selected = selected,
                        onClick = { route = item.route },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(scale)
                            )
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
                            indicatorColor = Color(0xFFFFEBEE)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
        ) {
            if (showProfile) {
                ProfileSetupScreen(paddingValues = padding)
            } else {
                Crossfade(
                    targetState = route,
                    label = "route-transition"
                ) { destination ->
                    when (destination) {
                        MainRoute.TRIPS   -> MyTripsScreen(padding)
                        MainRoute.EXPLORE -> ExploreScreen(padding)
                        MainRoute.PLACES  -> PlacesScreen(
                            paddingValues = padding,
                            onAiSuggest = { route = MainRoute.AI }
                        )
                        MainRoute.MAP     -> MapScreen(padding)
                        MainRoute.AI      -> AiAssistantScreen(padding)
                        MainRoute.WALLET  -> WalletScreen(padding)
                    }
                }
            }
        }
    }
}
