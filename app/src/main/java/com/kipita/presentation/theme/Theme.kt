package com.kipita.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.kipita.R

// ---------------------------------------------------------------------------
// Kipita Brand Colors — exact hex values from brand guidelines
// ---------------------------------------------------------------------------
val BrandBlue  = Color(0xFF56ACF9)   // Primary Blue
val BrandGreen = Color(0xFF44BC44)   // Primary Green
val BrandTeal  = Color(0xFF5BE1AE)   // Primary Teal
val BrandRed   = Color(0xFFDD3B49)   // Primary Red

// ---------------------------------------------------------------------------
// Extended palette — UI surfaces, text, and states
// ---------------------------------------------------------------------------
val KipitaRed          = Color(0xFFDD3B49)   // updated to exact brand red
val KipitaRedDark      = Color(0xFFC62828)
val KipitaRedLight     = Color(0xFFFFEBEE)
val KipitaBackground   = Color(0xFFFAFAFA)
val KipitaSurface      = Color(0xFFFFFFFF)
val KipitaCardBg       = Color(0xFFF5F5F5)
val KipitaOnSurface    = Color(0xFF1A1A1A)
val KipitaTextSecondary = Color(0xFF6B6B6B)
val KipitaTextTertiary  = Color(0xFF9E9E9E)
val KipitaBorder       = Color(0xFFE8E8E8)
val KipitaSuccess      = Color(0xFF2E7D32)
val KipitaWarning      = Color(0xFFF57C00)
val KipitaNavBg        = Color(0xFFFFFFFF)
val KipitaGreenAccent  = Color(0xFF44BC44)   // updated to brand green
val KipitaBlueAccent   = Color(0xFF56ACF9)   // updated to brand blue

// ---------------------------------------------------------------------------
// Montserrat — brand typeface via Google Fonts provider
// ---------------------------------------------------------------------------
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private val montserrat = GoogleFont("Montserrat")

val MontserratFontFamily = FontFamily(
    Font(googleFont = montserrat, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = montserrat, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = montserrat, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = montserrat, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = montserrat, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = montserrat, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

// ---------------------------------------------------------------------------
// Material 3 color schemes
// ---------------------------------------------------------------------------
private val KipitaDarkColors = darkColorScheme(
    primary            = BrandRed,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFF7B1515),
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary          = BrandBlue,
    onSecondary        = Color(0xFF0D2137),
    background         = Color(0xFF121212),
    onBackground       = Color(0xFFE8E8E8),
    surface            = Color(0xFF1E1E1E),
    onSurface          = Color(0xFFE8E8E8),
    surfaceVariant     = Color(0xFF2C2C2C),
    onSurfaceVariant   = Color(0xFFAAAAAA),
    outline            = Color(0xFF3A3A3A),
    error              = Color(0xFFEF9A9A),
    onError            = Color(0xFF7B1515)
)

private val KipitaLightColors = lightColorScheme(
    primary            = BrandRed,
    onPrimary          = Color.White,
    primaryContainer   = KipitaRedLight,
    onPrimaryContainer = KipitaRedDark,
    secondary          = BrandBlue,
    onSecondary        = Color.White,
    background         = KipitaBackground,
    onBackground       = KipitaOnSurface,
    surface            = KipitaSurface,
    onSurface          = KipitaOnSurface,
    surfaceVariant     = KipitaCardBg,
    onSurfaceVariant   = KipitaTextSecondary,
    outline            = KipitaBorder,
    error              = BrandRed,
    onError            = Color.White
)

// ---------------------------------------------------------------------------
// Typography — Montserrat throughout; headings Bold, subheadings lighter
// ---------------------------------------------------------------------------
private val KipitaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 36.sp,
        lineHeight   = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 28.sp,
        lineHeight   = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 24.sp,
        lineHeight   = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 20.sp,
        lineHeight   = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 18.sp,
        lineHeight   = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 16.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 10.sp,
        lineHeight   = 14.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun KipitaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KipitaDarkColors else KipitaLightColors,
        typography  = KipitaTypography,
        content     = content
    )
}
