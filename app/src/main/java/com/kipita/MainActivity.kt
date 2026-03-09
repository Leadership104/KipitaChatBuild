package com.kipita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kipita.presentation.main.KipitaApp
import com.kipita.presentation.splash.KipitaSplashScreen
import com.kipita.presentation.theme.KipitaTheme
import com.kipita.work.MerchantTravelSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enqueueSyncWork()
        setContent { AppEntryPoint() }
    }

    private fun enqueueSyncWork() {
        try {
            val work = PeriodicWorkRequestBuilder<MerchantTravelSyncWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("merchant-travel-sync", ExistingPeriodicWorkPolicy.UPDATE, work)
        } catch (t: Throwable) {
            android.util.Log.w("MainActivity", "WorkManager unavailable: ${t.message}")
        }
    }
}

@Composable
private fun AppEntryPoint() {
    KipitaTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main app renders underneath the splash from the start
            KipitaApp()

            // Splash overlay — visible for ~1.8 s then fades out over 600 ms
            var splashVisible by remember { mutableStateOf(true) }
            val splashAlpha by animateFloatAsState(
                targetValue = if (splashVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 600),
                label = "splash-fade"
            )

            LaunchedEffect(Unit) {
                delay(1800)
                splashVisible = false
            }

            if (splashAlpha > 0f) {
                KipitaSplashScreen(alpha = splashAlpha)
            }
        }
    }
}
