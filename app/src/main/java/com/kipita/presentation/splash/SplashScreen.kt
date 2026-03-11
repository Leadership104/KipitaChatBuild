package com.kipita.presentation.splash

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kipita.presentation.theme.BrandBlue
import com.kipita.presentation.theme.BrandGreen
import com.kipita.presentation.theme.BrandRed
import com.kipita.presentation.theme.BrandTeal
import com.kipita.presentation.theme.MontserratFontFamily

// ---------------------------------------------------------------------------
// Brand text colors
// ---------------------------------------------------------------------------
private val BrandDarkGray = Color(0xFF595959)

// ---------------------------------------------------------------------------
// Kipita Splash Screen — brand-aligned opening screen
//
// Layout:
//   • Pure white background (#FFFFFF)
//   • Brand-color blobs in each corner, partially clipped at edges
//   • Logo (4 colorful stick figures in pyramid) centered
//   • "Kipita" wordmark — Montserrat ExtraBold, dark gray
//   • "Know Before You Go" tagline — Montserrat Bold, dark gray
//   • 4 brand-color dots as decorative divider between logo and wordmark
// ---------------------------------------------------------------------------
@Composable
fun KipitaSplashScreen(alpha: Float = 1f) {
    // Subtle breathing animation on the logo
    val pulse = rememberInfiniteTransition(label = "pulse")
    val logoScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue  = 1.04f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo-pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        // ── Corner blobs ─────────────────────────────────────────────────────
        // Large soft circles partially cut off at screen edges; brand colors.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r      = size.width * 0.40f   // blob radius
            val inset  = r * 0.50f            // how much to push into the corner

            // Top-left: Brand Blue
            drawCircle(
                color  = BrandBlue.copy(alpha = 0.22f),
                radius = r,
                center = Offset(-inset, -inset)
            )
            // Top-right: Brand Teal
            drawCircle(
                color  = BrandTeal.copy(alpha = 0.22f),
                radius = r,
                center = Offset(size.width + inset, -inset)
            )
            // Bottom-left: Brand Green
            drawCircle(
                color  = BrandGreen.copy(alpha = 0.22f),
                radius = r,
                center = Offset(-inset, size.height + inset)
            )
            // Bottom-right: Brand Red
            drawCircle(
                color  = BrandRed.copy(alpha = 0.22f),
                radius = r,
                center = Offset(size.width + inset, size.height + inset)
            )

            // Smaller accent ring at top-left (layered depth effect)
            drawCircle(
                color  = BrandBlue.copy(alpha = 0.10f),
                radius = r * 0.60f,
                center = Offset(-inset * 0.30f, -inset * 0.30f)
            )
            // Smaller accent ring at bottom-right
            drawCircle(
                color  = BrandRed.copy(alpha = 0.10f),
                radius = r * 0.60f,
                center = Offset(size.width + inset * 0.30f, size.height + inset * 0.30f)
            )
        }

        // ── Logo + wordmark + tagline ────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 4-figure logo (pyramid arrangement using exact brand colors)
            KipitaLogoFigures(
                modifier = Modifier.size((160 * logoScale).dp)
            )

            Spacer(Modifier.height(24.dp))

            // Brand-color dot row — visual bridge between logo and wordmark
            BrandColorDots()

            Spacer(Modifier.height(20.dp))

            // "Kipita" wordmark — Montserrat ExtraBold, dark gray
            Text(
                text          = "Kipita",
                fontSize      = 54.sp,
                fontWeight    = FontWeight.ExtraBold,
                fontFamily    = MontserratFontFamily,
                color         = BrandDarkGray,
                letterSpacing = (-1.5).sp,
                textAlign     = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            // Tagline — Montserrat Bold per brand guidelines
            Text(
                text          = "Know Before You Go",
                fontSize      = 15.sp,
                fontWeight    = FontWeight.Bold,
                fontFamily    = MontserratFontFamily,
                color         = BrandDarkGray.copy(alpha = 0.75f),
                letterSpacing = 0.8.sp,
                textAlign     = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4 brand-color dots — decorative divider under the logo
// ---------------------------------------------------------------------------
@Composable
private fun BrandColorDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(BrandRed, BrandBlue, BrandTeal, BrandGreen).forEach { color ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4 colorful stick figures in a pyramid — using exact brand hex colors
//
//   BrandRed  (top center)
//   BrandBlue (mid left)    BrandTeal  (mid right)
//          BrandGreen (front center)
// ---------------------------------------------------------------------------
@Composable
private fun KipitaLogoFigures(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // BrandRed — top center, arms up
        drawStickFigure(
            centerX = w * 0.50f,
            centerY = h * 0.17f,
            scale   = w * 0.085f,
            color   = Color(0xFFDD3B49),
            armsUp  = true
        )

        // BrandBlue — mid left, arms up
        drawStickFigure(
            centerX = w * 0.24f,
            centerY = h * 0.50f,
            scale   = w * 0.085f,
            color   = Color(0xFF56ACF9),
            armsUp  = true
        )

        // BrandTeal — mid right, arms up
        drawStickFigure(
            centerX = w * 0.76f,
            centerY = h * 0.50f,
            scale   = w * 0.085f,
            color   = Color(0xFF5BE1AE),
            armsUp  = true
        )

        // BrandGreen — front center bottom, slightly larger (foreground)
        drawStickFigure(
            centerX = w * 0.50f,
            centerY = h * 0.74f,
            scale   = w * 0.095f,
            color   = Color(0xFF44BC44),
            armsUp  = true
        )
    }
}

private fun DrawScope.drawStickFigure(
    centerX: Float,
    centerY: Float,
    scale: Float,
    color: Color,
    armsUp: Boolean = true
) {
    val headR   = scale * 0.52f
    val bodyLen = scale * 1.15f
    val armLen  = scale * 0.85f
    val legLen  = scale * 0.95f
    val lw      = scale * 0.26f

    // Head
    drawCircle(color = color, radius = headR, center = Offset(centerX, centerY))

    // Body
    val bodyTop    = centerY + headR
    val bodyBottom = bodyTop + bodyLen
    drawLine(
        color       = color,
        start       = Offset(centerX, bodyTop),
        end         = Offset(centerX, bodyBottom),
        strokeWidth = lw,
        cap         = StrokeCap.Round
    )

    // Arms
    val armAnchor = bodyTop + bodyLen * 0.38f
    if (armsUp) {
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end   = Offset(centerX - armLen, armAnchor - armLen * 0.45f),
            strokeWidth = lw, cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end   = Offset(centerX + armLen, armAnchor - armLen * 0.45f),
            strokeWidth = lw, cap = StrokeCap.Round
        )
    } else {
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end   = Offset(centerX - armLen, armAnchor),
            strokeWidth = lw, cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end   = Offset(centerX + armLen, armAnchor),
            strokeWidth = lw, cap = StrokeCap.Round
        )
    }

    // Legs
    drawLine(
        color = color,
        start = Offset(centerX, bodyBottom),
        end   = Offset(centerX - legLen * 0.50f, bodyBottom + legLen),
        strokeWidth = lw, cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(centerX, bodyBottom),
        end   = Offset(centerX + legLen * 0.50f, bodyBottom + legLen),
        strokeWidth = lw, cap = StrokeCap.Round
    )
}
