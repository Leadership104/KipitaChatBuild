package com.kipita.presentation.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Kipita Splash Screen — shown on first launch, fades out after ~2 seconds
// ---------------------------------------------------------------------------
@Composable
fun KipitaSplashScreen(alpha: Float = 1f) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color(0xFFF7F7F7)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // White card containing the 4 colorful stick-figure logo
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                KipitaLogoFigures(modifier = Modifier.size(170.dp))
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = "Kipita",
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Know before you go",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF555555),
                letterSpacing = 0.3.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4 colorful stick figures arranged in a pyramid (mirroring the logo image)
//   Red on top, Yellow left, Blue right, Green front-center
// ---------------------------------------------------------------------------
@Composable
private fun KipitaLogoFigures(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Red figure — top center, arms up
        drawStickFigure(
            centerX = w * 0.50f,
            centerY = h * 0.17f,
            scale = w * 0.085f,
            color = Color(0xFFD32F2F),
            armsUp = true
        )

        // Yellow figure — mid left, arms up
        drawStickFigure(
            centerX = w * 0.24f,
            centerY = h * 0.50f,
            scale = w * 0.085f,
            color = Color(0xFFF9A825),
            armsUp = true
        )

        // Blue figure — mid right, arms up
        drawStickFigure(
            centerX = w * 0.76f,
            centerY = h * 0.50f,
            scale = w * 0.085f,
            color = Color(0xFF1976D2),
            armsUp = true
        )

        // Green figure — front center bottom, arms up
        drawStickFigure(
            centerX = w * 0.50f,
            centerY = h * 0.74f,
            scale = w * 0.095f,
            color = Color(0xFF388E3C),
            armsUp = true
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
    val headR = scale * 0.52f
    val bodyLen = scale * 1.15f
    val armLen = scale * 0.85f
    val legLen = scale * 0.95f
    val lw = scale * 0.26f

    // Head
    drawCircle(color = color, radius = headR, center = Offset(centerX, centerY))

    // Body
    val bodyTop = centerY + headR
    val bodyBottom = bodyTop + bodyLen
    drawLine(
        color = color,
        start = Offset(centerX, bodyTop),
        end = Offset(centerX, bodyBottom),
        strokeWidth = lw,
        cap = StrokeCap.Round
    )

    // Arms
    val armAnchor = bodyTop + bodyLen * 0.38f
    if (armsUp) {
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end = Offset(centerX - armLen, armAnchor - armLen * 0.45f),
            strokeWidth = lw, cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end = Offset(centerX + armLen, armAnchor - armLen * 0.45f),
            strokeWidth = lw, cap = StrokeCap.Round
        )
    } else {
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end = Offset(centerX - armLen, armAnchor),
            strokeWidth = lw, cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, armAnchor),
            end = Offset(centerX + armLen, armAnchor),
            strokeWidth = lw, cap = StrokeCap.Round
        )
    }

    // Legs
    drawLine(
        color = color,
        start = Offset(centerX, bodyBottom),
        end = Offset(centerX - legLen * 0.50f, bodyBottom + legLen),
        strokeWidth = lw, cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(centerX, bodyBottom),
        end = Offset(centerX + legLen * 0.50f, bodyBottom + legLen),
        strokeWidth = lw, cap = StrokeCap.Round
    )
}
