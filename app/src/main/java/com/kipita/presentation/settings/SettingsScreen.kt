package com.kipita.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat

@Composable
fun SettingsScreen(paddingValues: PaddingValues, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refreshLogs() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("Account Settings", style = MaterialTheme.typography.headlineMedium)
        Text("Contact: info@kipita.com", modifier = Modifier.padding(vertical = 8.dp))

        Button(onClick = {
            val body = buildString {
                appendLine("Please describe the issue:")
                appendLine()
                appendLine("Latest in-house error logs:")
                state.logs.take(10).forEach {
                    appendLine("- [${it.tag}] ${it.message} (${it.createdAtEpochMillis})")
                }
            }
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:info@kipita.com")
                putExtra(Intent.EXTRA_SUBJECT, "Kipita Support / Error Report")
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(intent)
        }) {
            Text("Contact support")
        }

        Button(onClick = { viewModel.flushLogs() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Send in-house error log")
        }

        if (state.lastFlushStatus.isNotBlank()) {
            Text(state.lastFlushStatus, modifier = Modifier.padding(top = 8.dp))
        }

        Text("In-house error log", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        LazyColumn {
            items(state.logs) { log ->
                Text("${log.tag}: ${log.message}")
            }
        }
    }
}
