package com.msa.android.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// MARK: - Brand Colors (matching iOS GoldenScreen.swift)
val Gold              = Color(0xFFE8B138)
val GoldDark          = Color(0xFFA77F28)
val GoldLight         = Color(0xFFEFC874)
val GoldMain          = Color(0xFFE8B138)
val GoldDarker        = Color(0xFF906E23)
val GoldenBorder      = Color(0xFFF2D28C)
val GoldenBorderSoft  = Color(0x80F2D28C)

// Backgrounds (gradient base)
val BgBlack           = Color(0xFF181111)
val BgDarkGold1       = Color(0xFF231918)
val BgDarkGold2       = Color(0xFF0E0E0E)
val BgDarkGold3       = Color(0xFF000000)

val SelectionBackground = Color(0xFF1C1414)
val SelectionBorder   = Color(0xFFE8B138)

val CardBlackAlpha    = Color(0x66000000)
val CardGold          = Color(0xFFB58934)
val CardGoldAlpha     = Color(0x80B58934)
val CardGray          = Color(0xFFE5E5E5)
val CardDarkGray      = Color(0xFF261B1A)

val WhiteSoft         = Color(0xFFF5F6F9)
val WhiteFull         = Color(0xFFFFFFFF)

val Accent            = Color(0xFFD4B6FF)   // اللافندر من الهوية
val GreenTrend        = Color(0xFF18A957)
val RedTrend          = Color(0xFFE53935)

val TextPrimary       = Color(0xFFFFFFFF)
val TextSecondary     = Color(0xFFCCCCCC)
val TextHint          = Color(0xFF9E9E9E)

// Tab bar
val TabBarBg          = Color(0xCC1C1414)
val TabBarSelectedBg  = Color(0x33E8B138)

// MSA app background — EXACTLY matches iOS BGSwiftUIView.swift:
//
//   LinearGradient(colors: [
//       Color(red: 0.07, green: 0.07, blue: 0.07),  // ~#121212
//       Color(red: 0.12, green: 0.12, blue: 0.12),  // ~#1F1F1F
//       Color(red: 0.18, green: 0.18, blue: 0.18)   // ~#2E2E2E
//   ], startPoint: .topLeading, endPoint: .bottomTrailing)
val MSABaseTop      = Color(0xFF181111)
val MSABaseMid      = Color(0xFF231918)
val MSABaseBottom   = Color(0xFF2F2221)

val MSAGradient = listOf(MSABaseTop, MSABaseMid, MSABaseBottom)

// Gold light effect overlay — iOS:
//   Color(red: 0.83, green: 0.69, blue: 0.22).opacity(0.4) → .clear
val MSAGoldLight    = Color(0x66E8B138)

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
