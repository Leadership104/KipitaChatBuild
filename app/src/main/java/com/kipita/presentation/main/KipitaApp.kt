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
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Groups
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
import com.kipita.presentation.profile.ProfileSetupScreen
import com.kipita.presentation.social.SocialScreen
import com.kipita.presentation.trips.MyTripsScreen
import com.kipita.presentation.wallet.WalletScreen
import com.kipita.presentation.theme.KipitaNavBg
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextTertiary

// ---------------------------------------------------------------------------
// Navigation routes — 5 tabs, AI in the center (position 3)
//   Trips | Explore | AI | Social | Wallet
// ---------------------------------------------------------------------------
enum class MainRoute {
    TRIPS, EXPLORE, AI, SOCIAL, WALLET
}

private data class NavItem(
    val route: MainRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenter: Boolean = false
)

private val navItems = listOf(
    NavItem(MainRoute.TRIPS,   "Trips",   Icons.Filled.Flight,        Icons.Outlined.Flight),
    NavItem(MainRoute.EXPLORE, "Explore", Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
    NavItem(MainRoute.AI,      "AI",      Icons.Filled.AutoAwesome,   Icons.Outlined.AutoAwesome,  isCenter = true),
    NavItem(MainRoute.SOCIAL,  "Social",  Icons.Filled.Groups,        Icons.Outlined.Groups),
    NavItem(MainRoute.WALLET,  "Wallet",  Icons.Filled.Wallet,        Icons.Outlined.Wallet)
)

@Composable
fun KipitaApp() {
    var route by rememberSaveable { mutableStateOf(MainRoute.TRIPS) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    // Cross-screen navigation callback (e.g. Explore → AI with pre-filled prompt)
    var aiPreFill by rememberSaveable { mutableStateOf("") }

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
                        label = "nav-scale"
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = { route = item.route },
                        icon = {
                            // Center AI button gets a red pill background when unselected too
                            if (item.isCenter && !selected) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .scale(scale)
                                        .background(KipitaRed, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
        ) {
            if (showProfile) {
                ProfileSetupScreen(paddingValues = padding)
            } else {
                Crossfade(targetState = route, label = "route-transition") { destination ->
                    when (destination) {
                        MainRoute.TRIPS   -> MyTripsScreen(
                            paddingValues = padding,
                            onAiSuggest = { prompt -> aiPreFill = prompt; route = MainRoute.AI }
                        )
                        MainRoute.EXPLORE -> ExploreScreen(
                            paddingValues = padding,
                            onAiSuggest = { prompt -> aiPreFill = prompt; route = MainRoute.AI }
                        )
                        MainRoute.AI      -> AiAssistantScreen(
                            paddingValues = padding,
                            preFillPrompt = aiPreFill.also { aiPreFill = "" }
                        )
                        MainRoute.SOCIAL  -> SocialScreen(padding)
                        MainRoute.WALLET  -> WalletScreen(padding)
                    }
                }
            }
        }
    }
}
