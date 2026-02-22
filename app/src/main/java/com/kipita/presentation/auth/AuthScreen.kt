package com.kipita.presentation.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.presentation.theme.KipitaBorder
import com.kipita.presentation.theme.KipitaCardBg
import com.kipita.presentation.theme.KipitaOnSurface
import com.kipita.presentation.theme.KipitaRed
import com.kipita.presentation.theme.KipitaRedLight
import com.kipita.presentation.theme.KipitaTextSecondary
import com.kipita.presentation.theme.KipitaTextTertiary
import kotlinx.coroutines.delay

/**
 * AuthScreen — Sign In / Create Account
 *
 * MVP state is local only.
 * API_READY: Replace the TODO stubs below with calls to your production auth endpoint
 * (Firebase Auth, Supabase, Auth0, etc.) — the UI contracts stay identical.
 *
 * Error reporting: any auth failure is captured and forwarded to info@kipita.com
 * via the in-app error mailto link.
 */
@Composable
fun AuthScreen(
    paddingValues: PaddingValues = PaddingValues(),
    onAuthSuccess: (displayName: String) -> Unit = {},
    onContinueAsGuest: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableIntStateOf(0) }   // 0=Sign In, 1=Create
    var visible by remember { mutableStateOf(false) }

    // Sign-in fields
    var siEmail    by remember { mutableStateOf("") }
    var siPassword by remember { mutableStateOf("") }
    var siPwdVisible by remember { mutableStateOf(false) }
    var siError    by remember { mutableStateOf("") }
    var siForgotSent by remember { mutableStateOf(false) }

    // Create-account fields
    var caName     by remember { mutableStateOf("") }
    var caEmail    by remember { mutableStateOf("") }
    var caPassword by remember { mutableStateOf("") }
    var caConfirm  by remember { mutableStateOf("") }
    var caPwdVisible by remember { mutableStateOf(false) }
    var caError    by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { delay(80); visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
    ) {
        // Back button
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF757575), modifier = Modifier.size(18.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 72.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Brand header
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { -24 }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Kipita logo mark
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(listOf(KipitaRed, Color(0xFFBF360C)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✈️", fontSize = 30.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Welcome to Kipita",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = KipitaOnSurface
                        )
                        Text(
                            "Your smart travel companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = KipitaTextSecondary
                        )
                    }
                }
            }

            // Tab row
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 12 }) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = KipitaRed,
                        indicator = { tabPositions ->
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(KipitaRed)
                            )
                        },
                        divider = {}
                    ) {
                        listOf("Sign In", "Create Account").forEachIndexed { i, label ->
                            Tab(
                                selected = selectedTab == i,
                                onClick  = { selectedTab = i; siError = ""; caError = "" },
                                text = {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (selectedTab == i) FontWeight.SemiBold
                                                         else FontWeight.Normal
                                        ),
                                        color = if (selectedTab == i) KipitaRed else KipitaTextSecondary
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── TAB 0: Sign In ────────────────────────────────────────────────
            if (selectedTab == 0) {
                item {
                    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 16 }) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            AuthTextField(
                                value = siEmail,
                                onValueChange = { siEmail = it; siError = "" },
                                label = "Email",
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = KipitaTextTertiary, modifier = Modifier.size(18.dp)) },
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                            AuthTextField(
                                value = siPassword,
                                onValueChange = { siPassword = it; siError = "" },
                                label = "Password",
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = KipitaTextTertiary, modifier = Modifier.size(18.dp)) },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                onDone = { focusManager.clearFocus() },
                                isPassword = true,
                                passwordVisible = siPwdVisible,
                                onTogglePassword = { siPwdVisible = !siPwdVisible }
                            )

                            if (siError.isNotBlank()) ErrorText(siError)

                            // Forgot password
                            Text(
                                text = if (siForgotSent) "✓ Reset link sent to $siEmail" else "Forgot password?",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (siForgotSent) Color(0xFF2E7D32) else KipitaRed,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable(enabled = !siForgotSent && siEmail.isNotBlank()) {
                                        // API_READY: call your auth provider's password-reset endpoint
                                        siForgotSent = true
                                    }
                            )

                            // Sign In button
                            PrimaryButton(
                                label = "Sign In",
                                enabled = siEmail.isNotBlank() && siPassword.isNotBlank()
                            ) {
                                // API_READY: replace with production auth call, e.g.:
                                // authRepository.signIn(siEmail, siPassword)
                                val name = siEmail.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
                                if (siPassword.length < 6) {
                                    siError = "Password must be at least 6 characters"
                                } else {
                                    onAuthSuccess(name)
                                }
                            }

                            OAuthSection(context = context)

                            GuestFooter(
                                text = "Don't have an account?",
                                actionText = "Create one",
                                onAction = { selectedTab = 1 },
                                onGuest = onContinueAsGuest
                            )
                        }
                    }
                }
            }

            // ── TAB 1: Create Account ─────────────────────────────────────────
            if (selectedTab == 1) {
                item {
                    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { 16 }) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            AuthTextField(
                                value = caName,
                                onValueChange = { caName = it; caError = "" },
                                label = "Display Name",
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = KipitaTextTertiary, modifier = Modifier.size(18.dp)) },
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                            AuthTextField(
                                value = caEmail,
                                onValueChange = { caEmail = it; caError = "" },
                                label = "Email",
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = KipitaTextTertiary, modifier = Modifier.size(18.dp)) },
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                            AuthTextField(
                                value = caPassword,
                                onValueChange = { caPassword = it; caError = "" },
                                label = "Password",
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = KipitaTextTertiary, modifier = Modifier.size(18.dp)) },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                isPassword = true,
                                passwordVisible = caPwdVisible,
                                onTogglePassword = { caPwdVisible = !caPwdVisible }
                            )
                            AuthTextField(
                                value = caConfirm,
                                onValueChange = { caConfirm = it; caError = "" },
                                label = "Confirm Password",
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = KipitaTextTertiary, modifier = Modifier.size(18.dp)) },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                onDone = { focusManager.clearFocus() },
                                isPassword = true,
                                passwordVisible = caPwdVisible,
                                onTogglePassword = { caPwdVisible = !caPwdVisible }
                            )

                            if (caError.isNotBlank()) ErrorText(caError)

                            // Create Account button
                            PrimaryButton(
                                label = "Create Account",
                                enabled = caName.isNotBlank() && caEmail.isNotBlank() && caPassword.isNotBlank()
                            ) {
                                when {
                                    caName.isBlank()              -> caError = "Please enter your name"
                                    !caEmail.contains("@")        -> caError = "Enter a valid email address"
                                    caPassword.length < 6         -> caError = "Password must be at least 6 characters"
                                    caPassword != caConfirm       -> caError = "Passwords do not match"
                                    else -> {
                                        // API_READY: replace with production user-creation call
                                        // authRepository.createAccount(caEmail, caPassword, caName)
                                        onAuthSuccess(caName.trim())
                                    }
                                }
                            }

                            OAuthSection(context = context)

                            GuestFooter(
                                text = "Already have an account?",
                                actionText = "Sign in",
                                onAction = { selectedTab = 0 },
                                onGuest = onContinueAsGuest
                            )
                        }
                    }
                }
            }

            // Error reporting footer
            item {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Having trouble? Tap to report an issue",
                    style = MaterialTheme.typography.labelSmall,
                    color = KipitaTextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:info@kipita.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Kipita Auth Issue")
                                    putExtra(Intent.EXTRA_TEXT, "I experienced an issue with sign-in/account creation:\n\n")
                                }
                                context.startActivity(intent)
                            }
                        }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onNext: () -> Unit = {},
    onDone: () -> Unit = {},
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = if (isPassword) ({
            IconButton(onClick = onTogglePassword) {
                Icon(
                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    null,
                    tint = KipitaTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }) else null,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = KipitaRed,
            unfocusedBorderColor = KipitaBorder,
            focusedLabelColor    = KipitaRed
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
            onDone = { onDone() }
        )
    )
}

@Composable
private fun PrimaryButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) KipitaRed else KipitaCardBg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (enabled) Color.White else KipitaTextTertiary
        )
    }
}

@Composable
private fun OAuthSection(context: android.content.Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(KipitaBorder))
            Text("or continue with", style = MaterialTheme.typography.labelSmall, color = KipitaTextTertiary)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(KipitaBorder))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OAuthButton("G  Google") {
                // API_READY: launch Google One-Tap / OAuth2 flow here
                android.widget.Toast.makeText(context, "Google Sign-In coming soon", android.widget.Toast.LENGTH_SHORT).show()
            }
            OAuthButton(" Apple") {
                // API_READY: launch Apple Sign-In flow here
                android.widget.Toast.makeText(context, "Apple Sign-In coming soon", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun OAuthButton(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, KipitaBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = KipitaOnSurface
        )
    }
}

@Composable
private fun GuestFooter(
    text: String,
    actionText: String,
    onAction: () -> Unit,
    onGuest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Text("$text ", style = MaterialTheme.typography.bodySmall, color = KipitaTextSecondary)
            Text(
                actionText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = KipitaRed,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
        Text(
            "Continue as Guest",
            style = MaterialTheme.typography.labelMedium,
            color = KipitaTextTertiary,
            modifier = Modifier.clickable(onClick = onGuest)
        )
    }
}

@Composable
private fun ErrorText(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFEBEE)
    ) {
        Text(
            text = "⚠ $message",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = KipitaRed
        )
    }
}
