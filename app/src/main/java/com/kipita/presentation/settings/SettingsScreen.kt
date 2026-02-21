package com.kipita.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat

// ---------------------------------------------------------------------------
// Colour tokens
// ---------------------------------------------------------------------------
private val NavyDark   = Color(0xFF0A0F1E)
private val NavyMid    = Color(0xFF111827)
private val GreenOk    = Color(0xFF22C55E)
private val RedDanger  = Color(0xFFEF4444)
private val AccentBlue = Color(0xFF3B82F6)
private val TextMuted  = Color(0xFF9CA3AF)
private val CardBg     = Color(0xFFF9FAFB)
private val BorderGray = Color(0xFFE5E7EB)

// ---------------------------------------------------------------------------
// SettingsScreen
// ---------------------------------------------------------------------------
@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refreshLogs()
        viewModel.refreshKeyStatus()
    }

    // Show snackbar whenever saveStatus changes
    LaunchedEffect(state.saveStatus) {
        if (state.saveStatus.isNotBlank()) {
            snackbarHost.showSnackbar(state.saveStatus)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            text = "API keys · vault · support",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ----------------------------------------------------------------
            // API Keys & Connections
            // ----------------------------------------------------------------
            item {
                SectionHeader(title = "API Keys & Connections")
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    ApiKeyField(
                        provider = "Yelp Fusion",
                        description = "Powers local business search and POI data in the Explore tab",
                        docsUrl = "https://docs.developer.yelp.com",
                        isConfigured = state.hasYelpKey,
                        onSave = { viewModel.saveYelpApiKey(it) },
                        onClear = { viewModel.clearYelpApiKey() }
                    )
                    Divider(color = BorderGray, thickness = 0.5.dp)
                    ApiKeyField(
                        provider = "Coinbase",
                        description = "Fetches your Coinbase wallet balance (read-only OAuth token)",
                        docsUrl = "https://docs.cdp.coinbase.com",
                        isConfigured = state.hasCoinbaseToken,
                        onSave = { viewModel.saveCoinbaseToken(it) },
                        onClear = { viewModel.clearCoinbaseToken() }
                    )
                    Divider(color = BorderGray, thickness = 0.5.dp)
                    ApiKeyField(
                        provider = "Gemini API Key",
                        description = "Gemini exchange API key for balance and trade data",
                        docsUrl = "https://docs.gemini.com/rest-api",
                        isConfigured = state.hasGeminiKey,
                        onSave = { viewModel.saveGeminiApiKey(it) },
                        onClear = { viewModel.clearGeminiKeys() }
                    )
                    Divider(color = BorderGray, thickness = 0.5.dp)
                    ApiKeyField(
                        provider = "Gemini API Secret",
                        description = "Gemini exchange API secret (paired with Gemini API Key)",
                        docsUrl = "https://docs.gemini.com/rest-api",
                        isConfigured = state.hasGeminiSecret,
                        onSave = { viewModel.saveGeminiApiSecret(it) },
                        onClear = { viewModel.clearGeminiKeys() }
                    )
                    Divider(color = BorderGray, thickness = 0.5.dp)
                    ApiKeyField(
                        provider = "River",
                        description = "River Financial OAuth token for Bitcoin wallet balance",
                        docsUrl = "https://river.com/learn/terms/a/api",
                        isConfigured = state.hasRiverToken,
                        onSave = { viewModel.saveRiverToken(it) },
                        onClear = { viewModel.clearRiverToken() }
                    )
                    Divider(color = BorderGray, thickness = 0.5.dp)
                    ApiKeyField(
                        provider = "CashApp",
                        description = "CashApp OAuth token for peer-to-peer payment data",
                        docsUrl = "https://developers.cash.app",
                        isConfigured = state.hasCashAppToken,
                        onSave = { viewModel.saveCashAppToken(it) },
                        onClear = null   // CashApp clear not exposed yet — coming soon
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ----------------------------------------------------------------
            // Security Vault info card
            // ----------------------------------------------------------------
            item {
                SectionHeader(title = "Security")
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
                        Icon(Icons.Filled.Security, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Hardware-backed KeyStore",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "All API keys are encrypted with AES-256-GCM and stored in the " +
                            "device's hardware security module (StrongBox or TEE). Keys never " +
                            "leave the device in plaintext.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null,
                                tint = GreenOk, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Zero-persistence: balances fetched live, never stored",
                                color = GreenOk, fontSize = 11.sp)
                        }
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
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

            // Bottom padding
            item { Spacer(Modifier.height(32.dp)) }
        }

        // Snackbar overlay
        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = NavyMid,
                contentColor = Color.White
            )
        }
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
// ApiKeyField — expandable card per provider
// ---------------------------------------------------------------------------
@Composable
private fun ApiKeyField(
    provider: String,
    description: String,
    docsUrl: String,
    isConfigured: Boolean,
    onSave: (String) -> Unit,
    onClear: (() -> Unit)?
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var fieldValue by rememberSaveable { mutableStateOf("") }
    var showText by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Row: provider label + status badge + expand chevron
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Icon(
                imageVector = if (isConfigured) Icons.Filled.CheckCircle
                              else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isConfigured) GreenOk else BorderGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                )
                Text(
                    text = if (isConfigured) "Configured" else "Not configured",
                    fontSize = 12.sp,
                    color = if (isConfigured) GreenOk else TextMuted
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        // Expanded content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Description
                Text(description, color = TextMuted, fontSize = 12.sp)

                // Secret text field
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = { fieldValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Paste $provider key/token") },
                    singleLine = true,
                    visualTransformation = if (showText) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showText = !showText }) {
                            Icon(
                                imageVector = if (showText) Icons.Filled.VisibilityOff
                                              else Icons.Filled.Visibility,
                                contentDescription = if (showText) "Hide" else "Show",
                                tint = TextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = BorderGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Action row: Save | Docs | Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSave(fieldValue)
                            fieldValue = ""
                            expanded = false
                        },
                        enabled = fieldValue.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save to Vault", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(docsUrl))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Text("Docs →", fontSize = 13.sp, color = AccentBlue)
                    }

                    if (isConfigured && onClear != null) {
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                onClear()
                                expanded = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, Color(0xFFFFE4E4), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove key",
                                tint = RedDanger, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
