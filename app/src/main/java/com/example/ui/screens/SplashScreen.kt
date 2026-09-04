package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AnnesGeometricLogo
import com.example.ui.components.AnnesLogoVariant
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.92f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onSplashFinished() },
        contentAlignment = Alignment.Center
    ) {
        // Full Page Luxury African Fashion Boutique Splash Background Image
        Image(
            painter = painterResource(id = R.drawable.img_african_luxury_splash),
            contentDescription = "Anne's Fashion Line - Luxury African Fashion",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Luxury Vignette Overlay for maximum contrast and elegant atmosphere
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xAA000000),
                        0.25f to Color(0x33000000),
                        0.55f to Color(0x44000000),
                        0.80f to Color(0xD9050507),
                        1.0f to Color(0xFC030305)
                    )
                )
        )

        // Content layout with elegant typography
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Minimal top spacer
            Spacer(modifier = Modifier.height(20.dp))

            // Center Branding: Official Logo White & Clean Luxury Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                // Official Monogram Logo (White & Gold)
                AnnesGeometricLogo(
                    size = 110.dp,
                    animated = true,
                    variant = AnnesLogoVariant.White,
                    showSubtitle = false
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Title in refined luxury typography
                Text(
                    text = "Anne's Fashion Line",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Minimalist Subtitle
                Text(
                    text = "HAUTE COUTURE • ATELIER",
                    color = PrimaryGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom loading element: sleek minimal gold progress bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(alpha.value)
                    .padding(bottom = 16.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(130.dp)
                        .height(2.5.dp)
                        .clip(CircleShape),
                    color = PrimaryGold,
                    trackColor = Color(0x33FFFFFF)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "INITIALIZING BOUTIQUE",
                    color = Color(0x99FFFFFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.5.sp
                )
            }
        }
    }
}
