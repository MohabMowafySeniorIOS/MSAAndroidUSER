package com.msa.android.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.theme.GoldenBorder

@Composable
fun OnBoardingScreen(
    onDone: () -> Unit,
    vm: OnBoardingViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val language by vm.language.collectAsStateWithLifecycle()

    MSABackground(showBars = false) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(Modifier.height(8.dp))

            // Top image (slides on page change)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 0.dp)
            ) {
                AnimatedContent(
                    targetState = state.current,
                    transitionSpec = {
                        (slideInHorizontally { w -> w } + fadeIn()) togetherWith
                                (slideOutHorizontally { w -> -w } + fadeOut())
                    },
                    label = "ob_image"
                ) { idx ->
                    val page = state.pages.getOrNull(idx)
                    if (page?.imageUrl?.isNotBlank() == true) {
                        AsyncImage(
                            model = page.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.onboarding_placeholder),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Card bottom (gold soft card with title/desc + dots + back arrow)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFC9A05A))
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val page = state.pages.getOrNull(state.current)
                    Text(
                        text = if (language == "ar") page?.titleAr.orEmpty() else page?.titleEn.orEmpty(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (language == "ar") page?.descAr.orEmpty() else page?.descEn.orEmpty(),
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    // dots
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        state.pages.forEachIndexed { i, _ ->
                            val isActive = i == state.current
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isActive) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Color.White else Color(0x80FFFFFF))
                                    .clickable { vm.goTo(i) }
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // back arrow button (forward through onboarding)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                            .clickable {
                                if (state.current == state.pages.lastIndex) {
                                    vm.finishOnBoarding(onDone)
                                } else {
                                    // Following iOS: the arrow moves to next page.
                                    // (In RTL Arabic the arrow points left = "next")
                                    vm.next()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val ltrDirection = LocalLayoutDirection.current == LayoutDirection.Ltr
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
