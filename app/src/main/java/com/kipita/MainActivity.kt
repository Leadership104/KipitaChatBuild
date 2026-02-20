package com.kipita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kipita.presentation.main.KipitaApp
import com.kipita.presentation.theme.KipitaTheme
import com.kipita.work.MerchantTravelSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppEntryPoint() }
        lifecycleScope.launch(Dispatchers.IO) { enqueueSyncWork() }
    }

    private fun enqueueSyncWork() {
        val work = PeriodicWorkRequestBuilder<MerchantTravelSyncWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("merchant-travel-sync", ExistingPeriodicWorkPolicy.UPDATE, work)
    }
}

@Composable
private fun AppEntryPoint() {
    KipitaTheme {
        KipitaApp()
    }
}
