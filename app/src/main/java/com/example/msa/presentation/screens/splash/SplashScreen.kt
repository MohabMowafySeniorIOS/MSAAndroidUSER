package com.msa.android.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onRouteHome: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) { scale.animateTo(1.0f, animationSpec = tween(700)) }
    LaunchedEffect(Unit) { alpha.animateTo(1.0f, animationSpec = tween(700)) }

    LaunchedEffect(state) {
        if (state.ready) {
            delay(900)
            onRouteHome()
        }
    }

    MSABackground(showBars = false) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_msa_logo),
                    contentDescription = "MSA",
                    modifier = Modifier.size(180.dp).scale(scale.value)
                )
                Spacer(Modifier.height(16.dp))
                Text("MSA", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
