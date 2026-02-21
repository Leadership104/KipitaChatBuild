package com.kipita

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kipita.data.service.StartupDataAggregator
import com.kipita.work.DailyReconWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KipitaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Middleware: concurrent fetch of wallets + prices + Yelp POIs on cold start
    @Inject
    lateinit var startupDataAggregator: StartupDataAggregator

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Schedule 24-hour background reconciliation at ~2:00 AM local time.
        // KEEP policy — safe on every cold start, no duplicate scheduling.
        DailyReconWorker.schedule(this)

        // Concurrent startup fetch: wallets (zero-persistence) + prices + POIs.
        // Streams run in parallel; each fails gracefully if API key is absent.
        startupDataAggregator.launchStartupFetch()
    }
}
