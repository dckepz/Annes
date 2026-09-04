package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class AnnesLogoVariant {
    Black, // Official Logo Black (Black outer geometry + Gold accents, for light surfaces & launcher)
    White  // Official Logo White (White outer geometry + Gold accents, for dark surfaces & login)
}

@Composable
fun AnnesGeometricLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    animated: Boolean = false,
    variant: AnnesLogoVariant = AnnesLogoVariant.White,
    showSubtitle: Boolean = false,
    subtitleText: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val shimmerOffset by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )
    } else {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "shimmer_static"
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val strokeW = (w * 0.08f).coerceAtLeast(3f)

                // Official Rich Metallic Gold Gradient
                val goldBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE8CA65),
                        PrimaryGold,
                        Color(0xFFB8860B),
                        Color(0xFFDFB743)
                    ),
                    start = Offset(shimmerOffset % w, 0f),
                    end = Offset((shimmerOffset % w) + w, h)
                )

                // Primary Stroke Brush: Pure White for Logo White, Deep Black for Logo Black
                val primaryBrush = when (variant) {
                    AnnesLogoVariant.White -> SolidColor(Color.White)
                    AnnesLogoVariant.Black -> SolidColor(Color(0xFF111111))
                }

                // 1. Upper Left Diagonal Stroke (Gold Accent)
                val goldUpperLeft = Path().apply {
                    moveTo(w * 0.395f, h * 0.07f)
                    lineTo(w * 0.230f, h * 0.49f)
                }
                drawPath(
                    path = goldUpperLeft,
                    brush = goldBrush,
                    style = Stroke(width = strokeW, cap = StrokeCap.Square)
                )

                // 2. Outer Left Diagonal Leg + Horizontal Crossbar (Primary: Black / White)
                val leftLegAndBar = Path().apply {
                    moveTo(w * 0.05f, h * 0.925f)
                    lineTo(w * 0.215f, h * 0.55f)
                    lineTo(w * 0.545f, h * 0.55f)
                }
                drawPath(
                    path = leftLegAndBar,
                    brush = primaryBrush,
                    style = Stroke(width = strokeW, cap = StrokeCap.Square, join = StrokeJoin.Miter)
                )

                // 3. Top Apex Down-Left Diagonal Stroke (Primary: Black / White)
                val topApexDownLeft = Path().apply {
                    moveTo(w * 0.590f, h * 0.075f)
                    lineTo(w * 0.405f, h * 0.490f)
                }
                drawPath(
                    path = topApexDownLeft,
                    brush = primaryBrush,
                    style = Stroke(width = strokeW, cap = StrokeCap.Square)
                )

                // 4. Outer Right Main Leg (Primary: Black / White)
                val mainRightLeg = Path().apply {
                    moveTo(w * 0.590f, h * 0.075f)
                    lineTo(w * 0.945f, h * 0.925f)
                }
                drawPath(
                    path = mainRightLeg,
                    brush = primaryBrush,
                    style = Stroke(width = strokeW, cap = StrokeCap.Square)
                )

                // 5. Inner Left Chevron (Gold Accent)
                val innerGoldChevron = Path().apply {
                    moveTo(w * 0.235f, h * 0.925f)
                    lineTo(w * 0.335f, h * 0.710f)
                    lineTo(w * 0.610f, h * 0.710f)
                }
                drawPath(
                    path = innerGoldChevron,
                    brush = goldBrush,
                    style = Stroke(width = strokeW, cap = StrokeCap.Square, join = StrokeJoin.Miter)
                )

                // 6. Inner Right Diagonal Stroke (Gold Accent)
                val innerGoldRight = Path().apply {
                    moveTo(w * 0.545f, h * 0.380f)
                    lineTo(w * 0.785f, h * 0.925f)
                }
                drawPath(
                    path = innerGoldRight,
                    brush = goldBrush,
                    style = Stroke(width = strokeW, cap = StrokeCap.Square)
                )
            }
        }

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "ANNE'S FASHION LINE",
                color = PrimaryGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 3.sp
            )
            val sub = subtitleText ?: "HAUTE COUTURE & LUXURY ATELIER"
            Text(
                text = sub,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

