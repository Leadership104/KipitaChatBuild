package com.kipita

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kipita.data.service.StartupDataAggregator
import com.kipita.work.DailyReconWorker
import dagger.hilt.android.HiltAndroidApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

        installGlobalExceptionHandler()

        // Schedule 24-hour background reconciliation at ~2:00 AM local time.
        // KEEP policy — safe on every cold start, no duplicate scheduling.
        DailyReconWorker.schedule(this)

        // Concurrent startup fetch: wallets (zero-persistence) + prices + POIs.
        // Streams run in parallel; each fails gracefully if API key is absent.
        startupDataAggregator.launchStartupFetch()
    }

    /**
     * Captures any unhandled exception, logs it silently, and opens a pre-filled
     * mailto: draft to info@kipita.com so the team receives a full crash report.
     * The default handler is still called after, allowing the process to terminate normally.
     */
    private fun installGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val subject = "Kipita Crash Report — ${throwable.javaClass.simpleName}"
                val body = buildString {
                    appendLine("Kipita App — Unhandled Crash")
                    appendLine("Time: $timestamp")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
                    appendLine()
                    appendLine("Exception: ${throwable.message}")
                    appendLine()
                    appendLine("Stack trace:")
                    appendLine(throwable.stackTraceToString().take(3500))
                }
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:info@kipita.com")
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
            // Always delegate to the default handler so the OS can show the crash dialog
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
