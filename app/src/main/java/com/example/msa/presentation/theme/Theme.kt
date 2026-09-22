package com.msa.android.presentation.theme

import android.app.Activity
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MSAColorScheme = darkColorScheme(
    primary = GoldMain,
    onPrimary = WhiteFull,
    primaryContainer = GoldDark,
    onPrimaryContainer = WhiteFull,
    secondary = GoldenBorder,
    onSecondary = BgDarkGold3,
    background = BgDarkGold3,
    onBackground = TextPrimary,
    surface = BgDarkGold2,
    onSurface = TextPrimary,
    surfaceVariant = SelectionBackground,
    onSurfaceVariant = TextSecondary,
    outline = GoldenBorder,
    error = RedTrend,
    onError = WhiteFull,
)

@Composable
fun MSATheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = MSAColorScheme,
        typography = MSATypography
    ) {
        // `MaterialTheme` بيظبط الـ typography بس — و`Text` اللي مش بياخد
        // `style` بيقرا من `LocalTextStyle` اللي قيمته الافتراضية خط النظام.
        // يعني أي `Text(text = ..., fontSize = ...)` كان بيطلع بخط النظام
        // مش بخط التطبيق. بنحقن الخط هنا فيوصل لكل نص في التطبيق
        // من غير ما نلمس 212 استدعاء.
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = MSAFontFamily),
            content = content
        )
    }
}
