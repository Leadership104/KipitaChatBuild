package com.kipita.presentation.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kipita.presentation.map.collectAsStateWithLifecycleCompat
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay

private val popularCurrencies = listOf("USD", "EUR", "GBP", "JPY", "SGD", "AUD", "CAD", "CHF", "BTC", "ETH")

@Composable
fun WalletScreen(paddingValues: PaddingValues, viewModel: WalletViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val animatedBalance = remember { Animatable(0f) }
    var amount by remember { mutableStateOf("100") }
    var from by remember { mutableStateOf("USD") }
    var to by remember { mutableStateOf("JPY") }
    var visible by remember { mutableStateOf(false) }
    var swapRotation by remember { mutableStateOf(0f) }
    val swapAnim by animateFloatAsState(swapRotation, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "swap-rotate")
    var converting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    LaunchedEffect(state.coinbaseBalance + state.cashAppBalance) {
        animatedBalance.animateTo(
            (state.coinbaseBalance + state.cashAppBalance).toFloat(),
            animationSpec = tween(700)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Dark header with BTC balance
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
                            )
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                    ) {
                        Text(
                            text = "Travel Wallet",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Supports 150+ currencies + Bitcoin",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Bitcoin Balance",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                        Text(
                                            text = "₿ ${"%.6f".format(animatedBalance.value)}",
                                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.refreshBalances("coinbase-token", "cashapp-token") },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    BalanceChip("Coinbase", "₿ ${"%.4f".format(state.coinbaseBalance)}", Modifier.weight(1f))
                                    BalanceChip("Cash App", "₿ ${"%.4f".format(state.cashAppBalance)}", Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Currency Converter
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 30 }) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KipitaRedLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Currency Converter",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = KipitaOnSurface
                                )
                                Text(
                                    text = "Live exchange rates",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KipitaTextSecondary
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .padding(20.dp)
                        ) {
                            CurrencyInputField(
                                label = "From",
                                amount = amount,
                                currency = from,
                                onAmountChange = { amount = it },
                                onCurrencyChange = { from = it }
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(KipitaCardBg)
                                        .border(1.dp, KipitaBorder, CircleShape)
                                        .clickable {
                                            val temp = from; from = to; to = temp
                                            swapRotation += 180f
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.SwapVert,
                                        contentDescription = "Swap",
                                        tint = KipitaRed,
                                        modifier = Modifier.size(22.dp).rotate(swapAnim)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            CurrencyInputField(
                                label = "To",
                                amount = if (state.conversionValue != null) "${"%.2f".format(state.conversionValue)}" else "",
                                currency = to,
                                onAmountChange = {},
                                onCurrencyChange = { to = it },
                                readOnly = true,
                                placeholder = "Result"
                            )

                            Spacer(Modifier.height(16.dp))

                            if (state.conversionRate != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(KipitaCardBg)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "1 ${state.conversionLabel.substringBefore("→")} =",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KipitaTextSecondary
                                    )
                                    Text(
                                        text = "${"%.4f".format(state.conversionRate)} ${state.conversionLabel.substringAfter("→")}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = KipitaOnSurface
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(KipitaRed)
                                    .clickable {
                                        converting = true
                                        viewModel.convert(amount.toDoubleOrNull() ?: 0.0, from, to)
                                        converting = false
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (converting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Convert",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick currency chips
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 40 }) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Quick Select",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KipitaOnSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            popularCurrencies.chunked(5).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { currency ->
                                        QuickCurrencyChip(
                                            currency = currency,
                                            selected = from == currency || to == currency,
                                            onClick = {
                                                if (from == currency) to = currency else from = currency
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun BalanceChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
    }
}

@Composable
private fun CurrencyInputField(
    label: String,
    amount: String,
    currency: String,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    readOnly: Boolean = false,
    placeholder: String = "0.00"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(KipitaCardBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                readOnly = readOnly,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = KipitaOnSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(KipitaRed),
                decorationBox = { inner ->
                    if (amount.isEmpty()) Text(placeholder, style = MaterialTheme.typography.headlineSmall, color = KipitaTextTertiary)
                    else inner()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {},
                color = if (currency == "BTC" || currency == "ETH") Color(0xFFFFF3E0) else Color.White,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = currency,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = when (currency) {
                        "BTC" -> Color(0xFFF57C00)
                        "ETH" -> Color(0xFF5C6BC0)
                        else -> KipitaOnSurface
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickCurrencyChip(
    currency: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        if (selected) 1f else 0.97f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chip-scale"
    )
    val isCrypto = currency == "BTC" || currency == "ETH"

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    selected -> KipitaRedLight
                    isCrypto -> Color(0xFFFFF8E1)
                    else -> Color.White
                }
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) KipitaRed else KipitaBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (currency) {
                    "USD" -> "🇺🇸"; "EUR" -> "🇪🇺"; "GBP" -> "🇬🇧"; "JPY" -> "🇯🇵"
                    "SGD" -> "🇸🇬"; "AUD" -> "🇦🇺"; "CAD" -> "🇨🇦"; "CHF" -> "🇨🇭"
                    "BTC" -> "₿"; "ETH" -> "Ξ"; else -> "💱"
                },
                fontSize = 14.sp
            )
            Text(
                text = currency,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                color = if (selected) KipitaRed else KipitaOnSurface
            )
        }
    }
}
