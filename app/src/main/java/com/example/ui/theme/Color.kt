package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==================== VIBRANT AFRICAN HAUTE COUTURE PALETTE ====================
// Primary Royal Gold Spectrum
val PrimaryGold = Color(0xFFFFB300) // Radiant Gold
val GoldAccent = Color(0xFFD4AF37) // Metallic Gold
val DarkGold = Color(0xFFC67D00)
val LightGold = Color(0xFFFFD54F)
val PaleGold = Color(0xFFFFF8E1)
val GoldMuted = Color(0x33FFB300)

// Vibrant Stimulating Fashion Accents
val FashionCoral = Color(0xFFFF2A6D) // African Hot Coral
val FashionCoralDark = Color(0xFFD81159)
val FashionCoralBg = Color(0x26FF2A6D)

val FashionEmerald = Color(0xFF00D26A) // Radiant African Emerald
val FashionEmeraldDark = Color(0xFF009647)
val FashionEmeraldBg = Color(0x2600D26A)

val FashionViolet = Color(0xFF8E2DE2) // Royal Haute Couture Purple
val FashionCyan = Color(0xFF00E5FF) // Electric Cyan
val FashionOrange = Color(0xFFFF6F00) // Sunset Marigold

// Dark Mode Surfaces (Deep Midnight Velvet Atelier)
val AppBlack = Color(0xFF0D0E15)
val SurfaceDark = Color(0xFF161722)
val SurfaceCard = Color(0xFF1E202E)
val SurfaceCardElevated = Color(0xFF272A3C)
val SurfaceCardBorder = Color(0xFF3B3E56)
val HeaderHeroWarm = Color(0xFF261D12)

// Light Mode Surfaces (Pristine Luxury Ivory & Alabaster)
val LightBg = Color(0xFFFAF7F2)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightCardElevated = Color(0xFFF3EFEA)
val LightCardBorder = Color(0xFFE5DDD0)
val LightHeaderHero = Color(0xFFFFF6E5)

// Typography Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFC8C8D8)
val TextMuted = Color(0xFF8888A0)

val LightTextPrimary = Color(0xFF1A1A24)
val LightTextSecondary = Color(0xFF5A5A70)
val LightTextMuted = Color(0xFF8E8EA6)

// Status Colors
val StatusSuccess = Color(0xFF00D26A)
val StatusSuccessBg = Color(0x2600D26A)
val StatusDanger = Color(0xFFFF3B30)
val StatusDangerBg = Color(0x26FF3B30)
val StatusWarning = Color(0xFFFF9500)
val StatusWarningBg = Color(0x26FF9500)
val StatusInfo = Color(0xFF0A84FF)
val StatusInfoBg = Color(0x260A84FF)

// Category Badges & Color Tokens
val CatDresses = Color(0xFFFF2A6D)
val CatCasual = Color(0xFF00D26A)
val CatCorporate = Color(0xFF4A00E0)
val CatWeekend = Color(0xFFFF9F0A)
val CatWigs = Color(0xFF9D4EDD)
val CatMakeup = Color(0xFFFF477E)
val CatShoes = Color(0xFF00C7BE)
val CatGeneral = Color(0xFFFFB300)

// Vibrant Dynamic Gradients
val GoldGradient = Brush.horizontalGradient(
    colors = listOf(LightGold, PrimaryGold, DarkGold)
)

val FashionHeroGradient = Brush.horizontalGradient(
    colors = listOf(FashionCoral, PrimaryGold, FashionViolet)
)

val CoralGoldGradient = Brush.horizontalGradient(
    colors = listOf(FashionCoral, PrimaryGold)
)

val EmeraldCyanGradient = Brush.horizontalGradient(
    colors = listOf(FashionEmerald, FashionCyan)
)

val VioletBlueGradient = Brush.horizontalGradient(
    colors = listOf(FashionViolet, Color(0xFF4A00E0))
)

val DarkHeroGradient = Brush.verticalGradient(
    colors = listOf(HeaderHeroWarm, SurfaceDark)
)

val LightHeroGradient = Brush.verticalGradient(
    colors = listOf(LightHeaderHero, LightBg)
)

val LuxuryCardGradient = Brush.linearGradient(
    colors = listOf(SurfaceCardElevated, SurfaceCard)
)


