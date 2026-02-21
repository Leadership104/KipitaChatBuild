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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.kipita.presentation.theme.KipitaGreenAccent
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class CurrencyInfo(val code: String, val flag: String, val name: String)

private val allCurrencies = listOf(
    CurrencyInfo("USD","🇺🇸","US Dollar"), CurrencyInfo("EUR","🇪🇺","Euro"),
    CurrencyInfo("GBP","🇬🇧","British Pound"), CurrencyInfo("JPY","🇯🇵","Japanese Yen"),
    CurrencyInfo("SGD","🇸🇬","Singapore Dollar"), CurrencyInfo("AUD","🇦🇺","Australian Dollar"),
    CurrencyInfo("CAD","🇨🇦","Canadian Dollar"), CurrencyInfo("CHF","🇨🇭","Swiss Franc"),
    CurrencyInfo("CNY","🇨🇳","Chinese Yuan"), CurrencyInfo("HKD","🇭🇰","Hong Kong Dollar"),
    CurrencyInfo("NZD","🇳🇿","New Zealand Dollar"), CurrencyInfo("SEK","🇸🇪","Swedish Krona"),
    CurrencyInfo("NOK","🇳🇴","Norwegian Krone"), CurrencyInfo("DKK","🇩🇰","Danish Krone"),
    CurrencyInfo("KRW","🇰🇷","South Korean Won"), CurrencyInfo("THB","🇹🇭","Thai Baht"),
    CurrencyInfo("MXN","🇲🇽","Mexican Peso"), CurrencyInfo("INR","🇮🇳","Indian Rupee"),
    CurrencyInfo("BRL","🇧🇷","Brazilian Real"), CurrencyInfo("ZAR","🇿🇦","South African Rand"),
    CurrencyInfo("TRY","🇹🇷","Turkish Lira"), CurrencyInfo("AED","🇦🇪","UAE Dirham"),
    CurrencyInfo("SAR","🇸🇦","Saudi Riyal"), CurrencyInfo("MYR","🇲🇾","Malaysian Ringgit"),
    CurrencyInfo("IDR","🇮🇩","Indonesian Rupiah"), CurrencyInfo("PHP","🇵🇭","Philippine Peso"),
    CurrencyInfo("VND","🇻🇳","Vietnamese Dong"), CurrencyInfo("PLN","🇵🇱","Polish Zloty"),
    CurrencyInfo("CZK","🇨🇿","Czech Koruna"), CurrencyInfo("HUF","🇭🇺","Hungarian Forint"),
    CurrencyInfo("RON","🇷🇴","Romanian Leu"), CurrencyInfo("BGN","🇧🇬","Bulgarian Lev"),
    CurrencyInfo("ISK","🇮🇸","Icelandic Króna"), CurrencyInfo("COP","🇨🇴","Colombian Peso"),
    CurrencyInfo("ARS","🇦🇷","Argentine Peso"), CurrencyInfo("CLP","🇨🇱","Chilean Peso"),
    CurrencyInfo("PEN","🇵🇪","Peruvian Sol"), CurrencyInfo("EGP","🇪🇬","Egyptian Pound"),
    CurrencyInfo("NGN","🇳🇬","Nigerian Naira"), CurrencyInfo("KES","🇰🇪","Kenyan Shilling"),
    CurrencyInfo("GHS","🇬🇭","Ghanaian Cedi"), CurrencyInfo("MAD","🇲🇦","Moroccan Dirham"),
    CurrencyInfo("PKR","🇵🇰","Pakistani Rupee"), CurrencyInfo("BDT","🇧🇩","Bangladeshi Taka"),
    CurrencyInfo("LKR","🇱🇰","Sri Lankan Rupee"), CurrencyInfo("ILS","🇮🇱","Israeli Shekel"),
    CurrencyInfo("JOD","🇯🇴","Jordanian Dinar"), CurrencyInfo("KWD","🇰🇼","Kuwaiti Dinar"),
    CurrencyInfo("QAR","🇶🇦","Qatari Riyal"), CurrencyInfo("CRC","🇨🇷","Costa Rican Colón"),
    CurrencyInfo("BTC","₿","Bitcoin"), CurrencyInfo("ETH","Ξ","Ethereum")
)

private val popularCurrencyCodes = listOf("USD","EUR","GBP","JPY","SGD","AUD","CAD","CHF","BTC","ETH")

@OptIn(ExperimentalMaterial3Api::class)
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
    var showPicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf("from") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { delay(80); visible = true }
    LaunchedEffect(state.coinbaseBalance + state.cashAppBalance) {
        animatedBalance.animateTo((state.coinbaseBalance + state.cashAppBalance).toFloat(), animationSpec = tween(700))
    }

    if (showPicker) {
        CurrencyPickerSheet(
            sheetState = sheetState,
            selectedCode = if (pickerTarget == "from") from else to,
            onSelect = { code ->
                if (pickerTarget == "from") from = code else to = code
                scope.launch { sheetState.hide(); showPicker = false }
            },
            onDismiss = { showPicker = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(bottom = 80.dp)) {

            // Header
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -20 }) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))))
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Travel Wallet", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("Real-time ECB rates · 50+ currencies", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f), modifier = Modifier.padding(top = 2.dp))
                            }
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(KipitaGreenAccent.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(KipitaGreenAccent))
                                Spacer(Modifier.width(5.dp))
                                Text("LIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = KipitaGreenAccent)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.1f)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Bitcoin Balance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.65f))
                                        Text("₿ ${"%.6f".format(animatedBalance.value)}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                    IconButton(onClick = { viewModel.refreshBalances("coinbase-token", "cashapp-token") },
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))) {
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(KipitaRedLight), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = KipitaRed, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Currency Converter", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
                                Text("European Central Bank · live rates", style = MaterialTheme.typography.labelSmall, color = KipitaTextSecondary)
                            }
                        }
                        Column(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp)) {
                            CurrencyInputField("From", amount, from, allCurrencies.find { it.code == from }?.flag ?: "💱", { amount = it }, { pickerTarget = "from"; showPicker = true })
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(KipitaCardBg).border(1.dp, KipitaBorder, CircleShape)
                                    .clickable { val t = from; from = to; to = t; swapRotation += 180f }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = KipitaRed, modifier = Modifier.size(22.dp).rotate(swapAnim))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            CurrencyInputField(
                                "To",
                                if (state.conversionValue != null) { if (to == "BTC" || to == "ETH") "%.8f".format(state.conversionValue) else "%.2f".format(state.conversionValue) } else "",
                                to, allCurrencies.find { it.code == to }?.flag ?: "💱", {}, { pickerTarget = "to"; showPicker = true }, readOnly = true, placeholder = "Result"
                            )
                            Spacer(Modifier.height(14.dp))
                            if (state.conversionRate != null) {
                                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(KipitaCardBg).padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("1 $from =", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                                        val rateStr = when {
                                            to == "BTC" || to == "ETH" -> "%.8f".format(state.conversionRate)
                                            (state.conversionRate ?: 0.0) < 0.01 -> "%.6f".format(state.conversionRate)
                                            else -> "%.4f".format(state.conversionRate)
                                        }
                                        Text("$rateStr $to", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
                                    }
                                    if (state.lastUpdated.isNotBlank()) {
                                        Text("ECB · ${state.lastUpdated}", style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                            if (state.error != null) {
                                Text(state.error, style = MaterialTheme.typography.bodySmall, color = KipitaRed, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(KipitaRed)
                                .clickable(enabled = !state.converting) { viewModel.convert(amount.toDoubleOrNull() ?: 0.0, from, to) }
                                .padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                                if (state.converting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Get Live Rate", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick select
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 40 }) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text("Popular Currencies", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface, modifier = Modifier.padding(bottom = 12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            popularCurrencyCodes.chunked(5).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { code ->
                                        val info = allCurrencies.find { it.code == code }
                                        QuickCurrencyChip(code, info?.flag ?: "💱", from == code || to == code,
                                            { if (from == code) to = code else from = code }, Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tips
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)).background(Color(0xFF1A1A2E)).padding(16.dp)) {
                        Text("Traveler Tips", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                        listOf(
                            "Always convert at ECB rates — avoid airport kiosks",
                            "Bitcoin accepted at 10,000+ businesses on BTCMap",
                            "Credit cards often offer better rates than cash exchange",
                            "Notify your bank before traveling internationally"
                        ).forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Text("·", color = KipitaRed, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 6.dp))
                                Text(tip, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    sheetState: androidx.compose.material3.SheetState,
    selectedCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = if (search.isBlank()) allCurrencies
    else allCurrencies.filter { it.code.contains(search, ignoreCase = true) || it.name.contains(search, ignoreCase = true) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Select Currency", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = KipitaOnSurface)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close", tint = KipitaTextSecondary) }
            }
            OutlinedTextField(value = search, onValueChange = { search = it }, placeholder = { Text("Search currencies...", color = KipitaTextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KipitaTextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KipitaRed, unfocusedBorderColor = KipitaBorder))
            LazyColumn(contentPadding = PaddingValues(bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(filtered, key = { it.code }) { info ->
                    val isSelected = info.code == selectedCode
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) KipitaRedLight else Color.Transparent)
                        .clickable { onSelect(info.code) }.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(info.flag, fontSize = 22.sp, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(info.code, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = if (isSelected) KipitaRed else KipitaOnSurface)
                            Text(info.name, style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
                        }
                        if (isSelected) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(KipitaRed), contentAlignment = Alignment.Center) {
                                Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.08f)).padding(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
    }
}

@Composable
private fun CurrencyInputField(label: String, amount: String, currency: String, currencyFlag: String,
    onAmountChange: (String) -> Unit, onCurrencyTap: () -> Unit, readOnly: Boolean = false, placeholder: String = "0.00") {
    val isCrypto = currency == "BTC" || currency == "ETH"
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(KipitaCardBg).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            BasicTextField(value = amount, onValueChange = onAmountChange, readOnly = readOnly,
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold, color = KipitaOnSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), cursorBrush = SolidColor(KipitaRed),
                decorationBox = { inner -> if (amount.isEmpty()) Text(placeholder, style = MaterialTheme.typography.headlineSmall, color = KipitaTextTertiary) else inner() },
                modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(if (isCrypto) Color(0xFFFFF3E0) else Color.White)
                .clickable(onClick = onCurrencyTap).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(currencyFlag, fontSize = if (isCrypto) 14.sp else 16.sp)
                Spacer(Modifier.width(5.dp))
                Text(currency, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = when (currency) { "BTC" -> Color(0xFFF57C00); "ETH" -> Color(0xFF5C6BC0); else -> KipitaOnSurface })
                Text(" ▾", style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
            }
        }
    }
}

@Composable
private fun QuickCurrencyChip(currency: String, flag: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isCrypto = currency == "BTC" || currency == "ETH"
    Box(modifier = modifier.clip(RoundedCornerShape(10.dp))
        .background(when { selected -> KipitaRedLight; isCrypto -> Color(0xFFFFF8E1); else -> Color.White })
        .border(if (selected) 1.5.dp else 1.dp, if (selected) KipitaRed else KipitaBorder, RoundedCornerShape(10.dp))
        .clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(flag, fontSize = 14.sp)
            Text(currency, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                color = if (selected) KipitaRed else KipitaOnSurface)
        }
    }
}
