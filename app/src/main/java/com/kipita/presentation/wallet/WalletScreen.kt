package com.kipita.presentation.wallet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat

@Composable
fun WalletScreen(paddingValues: PaddingValues, viewModel: WalletViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val animatedBalance = remember { Animatable(0f) }
    var amount by remember { mutableStateOf("100") }
    var from by remember { mutableStateOf("USD") }
    var to by remember { mutableStateOf("EUR") }

    LaunchedEffect(state.coinbaseBalance + state.cashAppBalance) {
        animatedBalance.animateTo((state.coinbaseBalance + state.cashAppBalance).toFloat(), tween(700))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("Wallet", style = MaterialTheme.typography.headlineMedium)
        Text("BTC Total: ${animatedBalance.value}")
        Text("Coinbase: ${state.coinbaseBalance}")
        Text("Cash App: ${state.cashAppBalance}")
        Button(onClick = { viewModel.refreshBalances("coinbase-token", "cashapp-token") }) {
            Text("Pull to refresh")
        }

        Text("Currency Converter", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 20.dp))
        BasicTextField(value = amount, onValueChange = { amount = it })
        BasicTextField(value = from, onValueChange = { from = it })
        BasicTextField(value = to, onValueChange = { to = it })
        Button(onClick = { viewModel.convert(amount.toDoubleOrNull() ?: 0.0, from, to) }) { Text("Convert") }
        Text("Pair: ${state.conversionLabel}")
        Text("Rate: ${state.conversionRate ?: "-"}")
        Text("Converted: ${state.conversionValue ?: "-"}")
    }
}
