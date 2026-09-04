package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

val LocalIsDarkTheme = staticCompositionLocalOf { true }

private val AnnesDarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = AppBlack,
    primaryContainer = DarkGold,
    onPrimaryContainer = PaleGold,
    secondary = FashionCoral,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceCardElevated,
    onSecondaryContainer = LightGold,
    tertiary = FashionEmerald,
    onTertiary = AppBlack,
    background = AppBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    error = StatusDanger,
    onError = TextPrimary,
)

private val AnnesLightColorScheme = lightColorScheme(
    primary = DarkGold,
    onPrimary = TextPrimary,
    primaryContainer = PaleGold,
    onPrimaryContainer = DarkGold,
    secondary = FashionCoralDark,
    onSecondary = TextPrimary,
    secondaryContainer = LightCardElevated,
    onSecondaryContainer = DarkGold,
    tertiary = FashionEmeraldDark,
    onTertiary = TextPrimary,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder,
    error = StatusDanger,
    onError = TextPrimary,
)

@Composable
fun MyApplicationTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) AnnesDarkColorScheme else AnnesLightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides isDarkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


