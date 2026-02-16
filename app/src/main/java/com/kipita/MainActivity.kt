package com.kipita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kipita.presentation.main.KipitaApp
import com.kipita.presentation.theme.KipitaTheme
import com.kipita.work.MerchantTravelSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import com.kipita.presentation.main.KipitaApp
import com.kipita.presentation.theme.KipitaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enqueueSyncWork()
        setContent { AppEntryPoint() }
    }

    private fun enqueueSyncWork() {
        val work = PeriodicWorkRequestBuilder<MerchantTravelSyncWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("merchant-travel-sync", ExistingPeriodicWorkPolicy.UPDATE, work)
    }
        setContent { AppEntryPoint() }
    }
}

@Composable
private fun AppEntryPoint() {
    KipitaTheme {
        KipitaApp()
    }
}
