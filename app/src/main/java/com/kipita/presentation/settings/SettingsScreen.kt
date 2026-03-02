package com.kipita.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat

// ---------------------------------------------------------------------------
// Colour tokens
// ---------------------------------------------------------------------------
private val NavyDark   = Color(0xFF0A0F1E)
private val GreenOk    = Color(0xFF22C55E)
private val RedDanger  = Color(0xFFEF4444)
private val AccentBlue = Color(0xFF3B82F6)
private val TextMuted  = Color(0xFF9CA3AF)
private val CardBg     = Color(0xFFF9FAFB)
private val BorderGray = Color(0xFFE5E7EB)
private val NavyMid    = Color(0xFF111827)

// ---------------------------------------------------------------------------
// SettingsScreen
// ---------------------------------------------------------------------------
@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenWebView: (url: String, title: String) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshLogs()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)),
        contentPadding = paddingValues
    ) {
        // ----------------------------------------------------------------
        // Header
        // ----------------------------------------------------------------
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDark)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "preferences · support",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ----------------------------------------------------------------
        // Security info (simplified — no API key vault language)
        // ----------------------------------------------------------------
        item {
            SectionHeader(title = "Privacy & Security")
        }

        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyMid)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1D4ED8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "End-to-End Encrypted",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "All local data is encrypted with AES-256 and stored only on your device. " +
                        "Kipita never sells or shares your personal data.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null,
                            tint = GreenOk, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Zero data sold · No ad tracking",
                            color = GreenOk, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ----------------------------------------------------------------
        // Partner & Affiliate Links
        // ----------------------------------------------------------------
        item {
            SectionHeader(title = "Partner Links & Affiliates")
        }

        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val affiliates = listOf(
                    AffiliateEntry(
                        name  = "Fold",
                        desc  = "Bitcoin rewards debit card — earn sats on every purchase",
                        emoji = "🟠",
                        url   = "AFFILIATE_FOLD_URL"
                    ),
                    AffiliateEntry(
                        name  = "Swan Bitcoin",
                        desc  = "Automated Bitcoin savings & accumulation platform",
                        emoji = "🦢",
                        url   = "AFFILIATE_SWAN_URL"
                    ),
                    AffiliateEntry(
                        name  = "Kinesis",
                        desc  = "Digital gold & silver — spend, save, and earn yield",
                        emoji = "⚡",
                        url   = "AFFILIATE_KINESIS_URL"
                    ),
                    AffiliateEntry(
                        name  = "Upside",
                        desc  = "Cash back on gas, groceries, and dining",
                        emoji = "⬆️",
                        url   = "AFFILIATE_UPSIDE_URL"
                    )
                )
                affiliates.forEachIndexed { index, affiliate ->
                    AffiliateRow(entry = affiliate, onOpenWebView = onOpenWebView)
                    if (index < affiliates.lastIndex) {
                        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ----------------------------------------------------------------
        // Share Kipita
        // ----------------------------------------------------------------
        item {
            SectionHeader(title = "Share")
        }

        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0F1E))
                    .clickable {
                        runCatching {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Check out Kipita!")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "I've been using Kipita — the smartest travel + finance super app. Check it out: https://Kipita.com"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Kipita"))
                        }
                    }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🚀", fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Share Kipita with Friends",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "kipita.com",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ----------------------------------------------------------------
        // Support section
        // ----------------------------------------------------------------
        item {
            SectionHeader(title = "Support")
        }

        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val body = buildString {
                            appendLine("Please describe your issue below:")
                            appendLine()
                            state.logs.take(10).forEach {
                                appendLine("[${it.tag}] ${it.message}")
                            }
                        }
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:info@kipita.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Kipita Support")
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Contact Support", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { viewModel.flushLogs() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Send Error Log", color = Color(0xFF374151))
                }

                if (state.lastFlushStatus.isNotBlank()) {
                    Text(
                        state.lastFlushStatus,
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ----------------------------------------------------------------
        // Error log list
        // ----------------------------------------------------------------
        if (state.logs.isNotEmpty()) {
            item {
                SectionHeader(title = "Recent Error Log")
            }

            items(state.logs.take(20)) { log ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "[${log.tag}]",
                        color = RedDanger,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        log.message,
                        color = Color(0xFF374151),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(2.dp))
            }

            item { Spacer(Modifier.height(16.dp)) }
        }

        // ----------------------------------------------------------------
        // Legal
        // ----------------------------------------------------------------
        item {
            SectionHeader(title = "Legal")
        }

        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                LegalRow(
                    label = "Privacy Policy",
                    url = "https://kipita.com/privacy",
                    onOpenWebView = onOpenWebView
                )
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                LegalRow(
                    label = "Terms of Service",
                    url = "https://kipita.com/terms",
                    onOpenWebView = onOpenWebView
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // ----------------------------------------------------------------
        // Account deletion (GDPR / CCPA)
        // ----------------------------------------------------------------
        item {
            SectionHeader(title = "Account")
        }

        item {
            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            "This will permanently delete your account and all local data, " +
                            "including your trip history. This cannot be undone.",
                            color = Color(0xFF374151),
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteAccount {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                        ) {
                            Text("Delete My Account", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RedDanger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDanger)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Delete My Account", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Bottom padding
        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ---------------------------------------------------------------------------
// SectionHeader
// ---------------------------------------------------------------------------
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

// ---------------------------------------------------------------------------
// Affiliate link helpers
// ---------------------------------------------------------------------------

private data class AffiliateEntry(
    val name: String,
    val desc: String,
    val emoji: String,
    val url: String
)

@Composable
private fun LegalRow(
    label: String,
    url: String,
    onOpenWebView: (url: String, title: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onOpenWebView(url, label)
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827),
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = BorderGray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AffiliateRow(
    entry: AffiliateEntry,
    onOpenWebView: (url: String, title: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!entry.url.startsWith("AFFILIATE_")) {
                    onOpenWebView(entry.url, entry.name)
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF111827)
            )
            Text(
                entry.desc,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = BorderGray,
            modifier = Modifier.size(18.dp)
        )
    }
}
