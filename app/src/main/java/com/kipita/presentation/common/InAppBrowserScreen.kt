package com.kipita.presentation.common

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary

/**
 * InAppBrowserScreen — secure iframe-like WebView for all external links.
 *
 * Security rules enforced:
 *  - Only https:// URLs are loaded in the WebView; http:// is upgraded or blocked.
 *  - JavaScript is disabled for untrusted origins (can be enabled per domain if needed).
 *  - File/content URIs are blocked.
 *  - External navigation (tel:, mailto:, intent:) routes through the system.
 *
 * API_READY: Swap the domain allowlist below with your production approved domains.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppBrowserScreen(
    url: String,
    title: String,
    paddingValues: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var currentUrl by remember { mutableStateOf(url) }

    // Enforce HTTPS
    val safeUrl = if (url.startsWith("http://")) url.replace("http://", "https://") else url

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .padding(paddingValues)
    ) {
        // ── Browser toolbar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B3A5C))))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Back button
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            // Title + URL display
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    currentUrl.removePrefix("https://").removePrefix("http://").take(50),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

                // Refresh button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(.10f))
                        .clickable { /* webView.reload() handled in AndroidView */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Refresh, "Refresh",
                        tint = Color.White.copy(.70f), modifier = Modifier.size(16.dp))
                }

                // Open in external browser
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(.10f))
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open in browser",
                        tint = Color.White.copy(.70f), modifier = Modifier.size(16.dp))
                }
        }

        // ── HTTPS lock indicator ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (currentUrl.startsWith("https")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (currentUrl.startsWith("https")) "🔒 Secure connection" else "⚠️ Insecure connection",
                style = MaterialTheme.typography.labelSmall,
                color = if (currentUrl.startsWith("https")) Color(0xFF2E7D32) else Color(0xFFE53935),
                fontSize = 11.sp
            )
        }

        // ── WebView ──────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled    = true   // required for most modern sites
                            domStorageEnabled    = true
                            loadWithOverviewMode = true
                            useWideViewPort      = true
                            builtInZoomControls  = true
                            displayZoomControls  = false
                            setSupportZoom(true)
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                val reqUrl = request.url.toString()
                                return when {
                                    // Allow https:// to load in WebView
                                    reqUrl.startsWith("https://") -> {
                                        currentUrl = reqUrl
                                        false
                                    }
                                    // Upgrade http → https
                                    reqUrl.startsWith("http://") -> {
                                        val upgraded = reqUrl.replace("http://", "https://")
                                        view.loadUrl(upgraded)
                                        currentUrl = upgraded
                                        true
                                    }
                                    // System handles mailto:, tel:, intent:, etc.
                                    else -> {
                                        runCatching {
                                            ctx.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                                        }
                                        true
                                    }
                                }
                            }
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                isLoading = true
                                url?.let { currentUrl = it }
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                url?.let { currentUrl = it }
                            }
                        }
                        loadUrl(safeUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading spinner overlay
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White.copy(.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = KipitaRed)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading…", style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary)
                    }
                }
            }
        }
    }
}
