package com.kipita.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// YC-standard Kipita brand palette
val KipitaRed = Color(0xFFE53935)
val KipitaRedDark = Color(0xFFC62828)
val KipitaRedLight = Color(0xFFFFEBEE)
val KipitaBackground = Color(0xFFFAFAFA)
val KipitaSurface = Color(0xFFFFFFFF)
val KipitaCardBg = Color(0xFFF5F5F5)
val KipitaOnSurface = Color(0xFF1A1A1A)
val KipitaTextSecondary = Color(0xFF6B6B6B)
val KipitaTextTertiary = Color(0xFF9E9E9E)
val KipitaBorder = Color(0xFFE8E8E8)
val KipitaSuccess = Color(0xFF2E7D32)
val KipitaWarning = Color(0xFFF57C00)
val KipitaNavBg = Color(0xFFFFFFFF)
val KipitaGreenAccent = Color(0xFF43A047)
val KipitaBlueAccent = Color(0xFF1976D2)

private val KipitaLightColors = lightColorScheme(
    primary = KipitaRed,
    onPrimary = Color.White,
    primaryContainer = KipitaRedLight,
    onPrimaryContainer = KipitaRedDark,
    secondary = KipitaBlueAccent,
    onSecondary = Color.White,
    background = KipitaBackground,
    onBackground = KipitaOnSurface,
    surface = KipitaSurface,
    onSurface = KipitaOnSurface,
    surfaceVariant = KipitaCardBg,
    onSurfaceVariant = KipitaTextSecondary,
    outline = KipitaBorder,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val KipitaTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun KipitaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KipitaLightColors,
        typography = KipitaTypography,
        content = content
    )
}
