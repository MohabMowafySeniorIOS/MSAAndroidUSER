package com.msa.android.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// MARK: - Brand Colors (matching iOS GoldenScreen.swift)
val Gold              = Color(0xFFF2C94C)
val GoldDark          = Color(0xFFB8962E)
val GoldLight         = Color(0xFFE5C76B)
val GoldMain          = Color(0xFFC9A23F)
val GoldDarker        = Color(0xFFB8922F)
val GoldenBorder      = Color(0xFFD8BE8A)
val GoldenBorderSoft  = Color(0x80D8BE8A)

// Backgrounds (gradient base)
val BgBlack           = Color(0xFF121212)
val BgDarkGold1       = Color(0xFF1A1305)
val BgDarkGold2       = Color(0xFF0E0E0E)
val BgDarkGold3       = Color(0xFF000000)

val SelectionBackground = Color(0xFF1E1A0C)
val SelectionBorder   = Color(0xFFD4AF37)

val CardBlackAlpha    = Color(0x66000000)
val CardGold          = Color(0xFFC9A05A)
val CardGoldAlpha     = Color(0x80C9A05A)
val CardGray          = Color(0xFFE5E5E5)
val CardDarkGray      = Color(0xFF2C2C2E)

val WhiteSoft         = Color(0xFFF5F6F9)
val WhiteFull         = Color(0xFFFFFFFF)

val GreenTrend        = Color(0xFF18A957)
val RedTrend          = Color(0xFFE53935)

val TextPrimary       = Color(0xFFFFFFFF)
val TextSecondary     = Color(0xFFCCCCCC)
val TextHint          = Color(0xFF9E9E9E)

// Tab bar
val TabBarBg          = Color(0xCC1C1C1E)
val TabBarSelectedBg  = Color(0x33D4AF37)

// MSA app background — EXACTLY matches iOS BGSwiftUIView.swift:
//
//   LinearGradient(colors: [
//       Color(red: 0.07, green: 0.07, blue: 0.07),  // ~#121212
//       Color(red: 0.12, green: 0.12, blue: 0.12),  // ~#1F1F1F
//       Color(red: 0.18, green: 0.18, blue: 0.18)   // ~#2E2E2E
//   ], startPoint: .topLeading, endPoint: .bottomTrailing)
val MSABaseTop      = Color(red = 0.07f, green = 0.07f, blue = 0.07f)
val MSABaseMid      = Color(red = 0.12f, green = 0.12f, blue = 0.12f)
val MSABaseBottom   = Color(red = 0.18f, green = 0.18f, blue = 0.18f)

val MSAGradient = listOf(MSABaseTop, MSABaseMid, MSABaseBottom)

// Gold light effect overlay — iOS:
//   Color(red: 0.83, green: 0.69, blue: 0.22).opacity(0.4) → .clear
val MSAGoldLight    = Color(red = 0.83f, green = 0.69f, blue = 0.22f, alpha = 0.40f)

val GoldGradientColors = listOf(
    GoldLight, GoldMain, GoldDarker
)

object MSAGradients {
    fun goldButton(): Brush = Brush.linearGradient(GoldGradientColors)

    /**
     * Base gradient: top-leading → bottom-trailing dark grey.
     * Brush.linearGradient defaults: start = (0,0) topLeft, end = (∞,∞) bottomRight.
     */
    fun appBackground(): Brush = Brush.linearGradient(MSAGradient)

    /**
     * Gold light overlay — bright gold spot at top-leading fading to transparent.
     * Implemented as a radial gradient at the top-leading corner, which gives
     * the same visual result as iOS UnitPoint(x: 0.7, y: 0.7) linear gradient.
     */
    fun goldLightOverlay(): Brush = Brush.radialGradient(
        colors = listOf(MSAGoldLight, Color.Transparent),
        center = androidx.compose.ui.geometry.Offset(0f, 0f),
        radius = 1200f
    )
}
