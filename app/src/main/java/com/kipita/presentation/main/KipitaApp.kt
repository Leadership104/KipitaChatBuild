package com.kipita.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kipita.presentation.ai.AiAssistantScreen
import com.kipita.presentation.chat.ChatScreen
import com.kipita.presentation.home.ExperienceScreen
import com.kipita.presentation.map.MapScreen
import com.kipita.presentation.settings.SettingsScreen
import com.kipita.presentation.wallet.WalletScreen

enum class MainRoute { EXPERIENCE, MAP, CHAT, AI, WALLET, SETTINGS }

@Composable
fun KipitaApp() {
    var route by rememberSaveable { mutableStateOf(MainRoute.EXPERIENCE) }
import com.kipita.presentation.map.MapScreen
import com.kipita.presentation.wallet.WalletScreen

enum class MainRoute { MAP, AI, WALLET }

@Composable
fun KipitaApp() {
    var route by rememberSaveable { mutableStateOf(MainRoute.MAP) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainRoute.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = route == destination,
                        onClick = { route = destination },
                        icon = { Text(destination.name.take(1)) },
                        label = { Text(destination.name) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = true) {
                Crossfade(targetState = route, label = "route-transition") { destination ->
                    when (destination) {
                        MainRoute.EXPERIENCE -> ExperienceScreen(padding)
                        MainRoute.MAP -> MapScreen(padding)
                        MainRoute.CHAT -> ChatScreen(padding)
                        MainRoute.AI -> AiAssistantScreen(padding)
                        MainRoute.WALLET -> WalletScreen(padding)
                        MainRoute.SETTINGS -> SettingsScreen(padding)
                        MainRoute.MAP -> MapScreen(padding)
                        MainRoute.AI -> AiAssistantScreen(padding)
                        MainRoute.WALLET -> WalletScreen(padding)
                    }
                }
            }
        }
    }
}
