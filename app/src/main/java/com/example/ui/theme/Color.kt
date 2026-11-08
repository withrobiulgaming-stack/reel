package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Primary Short-Video Colors (TikTok / Reels inspired)
val ShortlyPrimary = Color(0xFFFE2C55)      // Electric Pink/Red
val ShortlyCyan = Color(0xFF25F4EE)         // Neon Cyan
val ShortlyDark = Color(0xFF0A0B12)         // Pure Dark Cosmic Canvas
val ShortlySurfaceDark = Color(0xFF141724)  // Card Surface
val ShortlySurfaceLight = Color(0xFF22263A) // Elevated Surface
val ShortlyTextPrimary = Color(0xFFFFFFFF)  // Pure White
val ShortlyTextSecondary = Color(0xFFB5B8CD)// Frosted Silver
val ShortlyAccentGold = Color(0xFFFFB800)   // Creator Gold
val ShortlySuccess = Color(0xFF00E676)      // Active green
val ShortlyError = Color(0xFFFF453A)        // Error red

// Payment branding
val BkashPink = Color(0xFFE2136E)
val NagadOrange = Color(0xFFF7941D)

// 🌟 Full Glassmorphism & Liquid Glass Tokens
val GlassBackgroundDeep = Color(0xFF07080F)
val GlassSurface = Color(0x331C2136)          // Frosted translucent acrylic surface (20% white/indigo)
val GlassSurfaceElevated = Color(0x5028304C)  // Elevated glassy card surface
val GlassSurfaceUltra = Color(0x22FFFFFF)     // Clear luminous crystal layer
val GlassSurfaceSubtle = Color(0x18FFFFFF)    // Faint glassy veil
val GlassBorder = Color(0x40FFFFFF)           // Specular highlight border
val GlassBorderSubtle = Color(0x20FFFFFF)     // Subtle frosted edge
val GlassGlowPink = Color(0x35FE2C55)         // Ambient pink glass glow
val GlassGlowCyan = Color(0x3525F4EE)         // Ambient cyan glass glow

// Glass Gradients
val GlassBorderGradient = Brush.linearGradient(
    listOf(
        Color(0x66FFFFFF),
        Color(0x20FFFFFF),
        Color(0x08FFFFFF)
    )
)

val GlassCardGradient = Brush.verticalGradient(
    listOf(
        Color(0x35283048),
        Color(0x20141828)
    )
)

val GlassAtmosphereGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF0A0B14),
        Color(0xFF120E1E),
        Color(0xFF08121C),
        Color(0xFF07080F)
    )
)

val DarkColorSchemePrimary = Color(0xFFFE2C55)
val DarkColorSchemeBackground = Color(0xFF07080F)
val DarkColorSchemeSurface = Color(0x331C2136)
val DarkColorSchemeOnPrimary = Color(0xFFFFFFFF)
val DarkColorSchemeOnBackground = Color(0xFFFFFFFF)
val DarkColorSchemeOnSurface = Color(0xFFEDEDF5)

// Extension Modifier for Liquid Frosted Glass Cards
fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurface,
    borderWidth: Dp = 1.dp,
    borderBrush: Brush = GlassBorderGradient
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .border(borderWidth, borderBrush, shape)
