package com.kipita.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile by walking through all main navigation destinations.
 *
 * Run with (AGP 8.x+):
 *   ./gradlew generateDevDebugBaselineProfile
 *
 * The generated profile is written to app/src/main/baseline-prof.txt automatically.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "com.mytum.dev") {
        pressHome()
        startActivityAndWait()

        // Experience tab is the default — already captured by startActivityAndWait()

        // Map tab
        device.findObject(By.text("MAP"))?.let { tab ->
            tab.click()
            device.wait(Until.hasObject(By.text("MAP")), 3_000)
        }

        // Chat tab
        device.findObject(By.text("CHAT"))?.let { tab ->
            tab.click()
            device.wait(Until.hasObject(By.text("CHAT")), 3_000)
        }

        // AI tab
        device.findObject(By.text("AI"))?.let { tab ->
            tab.click()
            device.wait(Until.hasObject(By.text("AI")), 3_000)
        }

        // Wallet tab
        device.findObject(By.text("WALLET"))?.let { tab ->
            tab.click()
            device.wait(Until.hasObject(By.text("WALLET")), 3_000)
        }

        // Settings tab
        device.findObject(By.text("SETTINGS"))?.let { tab ->
            tab.click()
            device.wait(Until.hasObject(By.text("SETTINGS")), 3_000)
        }
    }
}
