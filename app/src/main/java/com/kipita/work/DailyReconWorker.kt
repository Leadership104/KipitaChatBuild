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
import com.kipita.data.repository.CryptoWalletRepository
import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NomadRepository
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

@HiltWorker
class DailyReconWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val merchantRepository: MerchantRepository,
    private val nomadRepository: NomadRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            // Force refresh crypto wallets (bypasses 60-second cache)
            cryptoWalletRepository.getAggregatedWallet(forceRefresh = true)
            // Re-sync BTC merchant map
            merchantRepository.refresh(cashAppToken = null)
            // Re-sync Nomad place data
            nomadRepository.refresh()
            Result.success()
        }.getOrElse {
            // Retry with exponential backoff on failure
            Result.retry()
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
