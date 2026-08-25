package com.msa.android.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.msa.android.R
import com.msa.android.presentation.theme.MSAGradients

/**
 * Background layer for every screen — exact port of iOS BGSwiftUIView:
 *
 *   ZStack {
 *       // Base 3-stop dark gradient (topLeading → bottomTrailing)
 *       LinearGradient(...)
 *       // Gold light effect (topLeading)
 *       LinearGradient(gold .opacity(0.4) → clear)
 *       // Subtle texture image
 *       Image("BGImage").opacity(0.15)
 *   }.ignoresSafeArea()
 *
 * The `showBars` flag is kept for backwards compatibility but is now a no-op
 * (iOS doesn't draw gold/silver bars on the background — they belong to
 * specific screens like the splash, not the global background).
 */
@Composable
fun MSABackground(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") showBars: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // Layer 1: base dark gradient
            .background(MSAGradients.appBackground())
    ) {
        // Layer 2: gold light effect (top-leading glow)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MSAGradients.goldLightOverlay())
        )

        // Layer 3: subtle texture at 15% opacity (matches iOS Image("BGImage").opacity(0.15))
        Image(
            painter = painterResource(R.drawable.bg_texture),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.15f
        )

        // Foreground content
        content()
    }
}
