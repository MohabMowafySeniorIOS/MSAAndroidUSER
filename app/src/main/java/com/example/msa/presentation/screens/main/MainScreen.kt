package com.msa.android.presentation.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.BottomTab
import com.msa.android.presentation.common.components.MSABottomBar
import com.msa.android.presentation.screens.appstatus.MaintainedScreen
import com.msa.android.presentation.screens.appstatus.ScreenMaintenanceGate
import com.msa.android.presentation.screens.appstatus.ScreenMaintenanceViewModel
import com.msa.android.presentation.screens.banks.BanksScreen
import com.msa.android.presentation.screens.calculator.CalculatorScreen
import com.msa.android.presentation.screens.calculators.bullions.BullionScreen
import com.msa.android.presentation.screens.home.HomeScreen
import com.msa.android.presentation.screens.more.MoreScreen
import com.msa.android.presentation.screens.news.NewsScreen
import androidx.compose.foundation.layout.aspectRatio
/**
 * Tab host — exactly like iOS UITabBarController flow.
 * Plus a floating FAB above the bottom bar opening the NetDania WebView (matches iOS).
 */
@Composable
fun MainScreen(
    onOpenWebView: () -> Unit,
    onOpenCalculatorHub: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenUsagePolicy: () -> Unit,
    onOpenRefundPolicy: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenPriceGap: () -> Unit,
    onOpenIndicators: () -> Unit,
    onOpenTechnicalAnalysis: () -> Unit,
    onOpenImageViewer: (String) -> Unit,
    onOpenCurrencyPicker: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenPortfolio: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(BottomTab.HOME) }

    // One shared listener on appVersion/appVersion for the per-screen flags,
    // hoisted here so Home and Dollar reuse it (and so the NetDania FAB can be
    // hidden while Home is down).
    val maintenanceVm: ScreenMaintenanceViewModel = hiltViewModel()
    val maintenance by maintenanceVm.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        // Content (tabs)
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_content",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            when (tab) {
                // Home and Dollar are each gated on their own Firestore flag.
                // The gate sits inside the tab host, so the bottom bar below
                // stays live and the user can move to another tab.
                BottomTab.HOME -> ScreenMaintenanceGate(
                    screen = MaintainedScreen.HOME,
                    vm = maintenanceVm
                ) {
                    HomeScreen(
                        onOpenPriceGap = onOpenPriceGap,
                        onOpenQrScanner = onOpenQrScanner
                    )
                }
                BottomTab.DOLLAR -> ScreenMaintenanceGate(
                    screen = MaintainedScreen.DOLLAR,
                    vm = maintenanceVm
                ) {
                    BanksScreen(onChangeCurrency = onOpenCurrencyPicker)
                }
                BottomTab.BULLIONS -> BullionScreen(onBack = {
                    // Tab content has no real back; tapping the back arrow just
                    // bounces user to the Home tab (matches iOS where Bullions
                    // is a tab root and can't be popped).
                    selected = BottomTab.HOME
                })
                BottomTab.NEWS -> NewsScreen(onOpenImage = onOpenImageViewer)
                BottomTab.MORE -> MoreScreen(
                    onGoldSilverCalculator = onOpenCalculatorHub,
                    onHelp = onOpenHelp,
                    onFaq = onOpenFaq,
                    onAbout = onOpenAbout,
                    onUsagePolicy = onOpenUsagePolicy,
                    onRefundPolicy = onOpenRefundPolicy,
                    onPrivacyPolicy = onOpenPrivacyPolicy,
                    onIndicators = onOpenIndicators,
                    onTechnicalAnalysis = onOpenTechnicalAnalysis,
                    onPortfolio = onOpenPortfolio
                )
            }
        }

        // Floating chart button (matches iOS NetDania fab in HomeVC storyboard).
        // iOS uses leadingAnchor.constant=16 → in RTL Arabic that means the RIGHT side.
        // Compose: BottomStart = "start" which is also RIGHT in RTL.
        // We render the real netDania logo image (matches iOS state.normal backgroundImage).
        // Hidden while Home is under maintenance — nothing to chart behind it.
        if (selected == BottomTab.HOME && !maintenance.home.isUnderMaintenance) {
            Image(
                painter = painterResource(R.drawable.ic_netdania),
                contentDescription = "NetDania chart",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 100.dp)
                    .size(70.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .clickable { onOpenWebView() }
            )
        }

        // Bottom bar
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom
        ) {
            MSABottomBar(selected = selected, onSelect = { selected = it })
        }
    }
}
