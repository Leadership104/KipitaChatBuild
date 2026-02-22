package com.kipita.presentation.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * KipitaErrorBoundary wraps a screen composable.
 *
 * Because Kotlin/Compose cannot catch exceptions thrown *inside* a composable
 * call at the framework level (unlike React), this boundary works via an
 * explicit [onError] callback that ViewModels / data calls pass back to the UI.
 *
 * Usage:
 *   KipitaErrorBoundary(screenName = "WalletScreen") { onError ->
 *       WalletScreenContent(onError = onError)
 *   }
 *
 * When [onError] is called:
 *  - A friendly fallback UI is shown in place of the screen content.
 *  - An error report is auto-sent to info@kipita.com via mailto:.
 *  - A "Try Again" button resets the error state.
 */
@Composable
fun KipitaErrorBoundary(
    screenName: String,
    content: @Composable (onError: (Throwable) -> Unit) -> Unit
) {
    var error by remember { mutableStateOf<Throwable?>(null) }
    val context = LocalContext.current

    if (error != null) {
        ErrorFallbackScreen(
            screenName = screenName,
            error = error!!,
            context = context,
            onRetry = { error = null }
        )
    } else {
        content { caught -> error = caught }
    }
}

// ---------------------------------------------------------------------------
// Fallback UI
// ---------------------------------------------------------------------------
@Composable
private fun ErrorFallbackScreen(
    screenName: String,
    error: Throwable,
    context: Context,
    onRetry: () -> Unit
) {
    // Fire-and-forget email report on first composition
    LaunchedEffect(error) {
        sendErrorReport(context, screenName, error)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(KipitaRedLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.BugReport,
                contentDescription = null,
                tint = KipitaRed,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Something went wrong",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = KipitaOnSurface,
            textAlign = TextAlign.Center
        )

        Text(
            "We've been notified and will fix this soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = KipitaTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(28.dp))

        // Retry button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(KipitaRed)
                .clickable(onClick = onRetry)
                .padding(horizontal = 28.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Try Again",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "Error report sent automatically to info@kipita.com\nScreen: $screenName",
            style = MaterialTheme.typography.labelSmall,
            color = KipitaTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// Auto-send error report to info@kipita.com via mailto:
// ---------------------------------------------------------------------------
fun sendErrorReport(context: Context, screenName: String, error: Throwable) {
    runCatching {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val subject = "Kipita App Error — $screenName"
        val body = buildString {
            appendLine("Screen: $screenName")
            appendLine("Time: $timestamp")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
            appendLine()
            appendLine("Error: ${error.message}")
            appendLine()
            appendLine("Stack trace:")
            appendLine(error.stackTraceToString().take(3000))
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:info@kipita.com")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    // Swallow — never crash the app while reporting a crash
}
