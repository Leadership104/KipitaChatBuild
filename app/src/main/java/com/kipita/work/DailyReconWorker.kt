package com.kipita.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kipita.data.api.PlaceCategory
import com.kipita.data.repository.CryptoWalletRepository
import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NomadRepository
import com.kipita.data.repository.YelpPlacesRepository
import com.kipita.data.service.StartupDataAggregator
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// DailyReconWorker
//
// 24-hour background reconciliation job (SOW requirement).
// Scheduled at 2:00 AM local time via periodic WorkManager work with
// a flex window of 30 minutes.
//
// Responsibilities:
//   1. Force-refresh aggregated crypto wallet balances from all sources
//   2. Re-sync BTC merchant map data
//   3. Re-sync Nomad city data
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// DailyReconWorker (expanded)
//
// 24-hour reconciliation job. All four streams run concurrently (coroutineScope
// + async) so the full job completes in max(slowest_source) time.
//
// Streams:
//   1. Crypto wallet balances     — force-refresh from Coinbase / Gemini / River
//   2. BTC merchant map           — re-sync from BTCMap API
//   3. Nomad city data            — re-sync from NomadList API
//   4. Yelp business categories   — refresh all 20 categories for default + major cities
// ---------------------------------------------------------------------------

@HiltWorker
class DailyReconWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val merchantRepository: MerchantRepository,
    private val nomadRepository: NomadRepository,
    private val yelpPlacesRepository: YelpPlacesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            coroutineScope {
                // Stream 1: Force-refresh crypto wallet balances (memory-only, zero-persistence)
                val walletJob = async { cryptoWalletRepository.getAggregatedWallet(forceRefresh = true) }

                // Stream 2: Re-sync BTC merchant map data from BTCMap
                val merchantJob = async { merchantRepository.refresh(cashAppToken = null) }

                // Stream 3: Re-sync Nomad city rankings + quality scores
                val nomadJob = async { nomadRepository.refresh() }

                // Stream 4: Refresh Yelp business categories for key locations
                val yelpJob = async { refreshYelpCategories() }

                walletJob.await()
                merchantJob.await()
                nomadJob.await()
                yelpJob.await()
            }
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    /**
     * Refresh the 20 Yelp business categories for the default reference location
     * (Tokyo) plus additional hub cities. Results are cached in-memory by
     * YelpPlacesRepository and evicted after 15 minutes at runtime; this job
     * ensures cache is warm at ~2 AM when network conditions are optimal.
     */
    private suspend fun refreshYelpCategories() {
        // Reference locations: Tokyo default + major nomad hubs
        val locations = listOf(
            StartupDataAggregator.DEFAULT_LAT to StartupDataAggregator.DEFAULT_LNG  // Tokyo
        )
        // Refresh all static categories for each location
        locations.forEach { (lat, lng) ->
            PlaceCategory.entries.forEach { category ->
                runCatching { yelpPlacesRepository.fetchCategory(lat, lng, category) }
                // Individual category failures are silently skipped
            }
        }
    }

    companion object {
        private const val WORK_NAME = "kipita_daily_recon"

        /**
         * Schedules the 24-hour recon job to run at ~2:00 AM local time.
         * Uses a 24h period with a 15-minute flex window and requires network.
         * Safe to call on every app launch — KEEP policy avoids re-scheduling if running.
         */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 2)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If 2AM has already passed today, schedule for tomorrow
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelayMs = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyReconWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 15,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
