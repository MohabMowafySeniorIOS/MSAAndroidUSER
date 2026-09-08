package com.msa.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.msa.android.domain.model.PageType
import com.msa.android.presentation.screens.about.AboutScreen
import com.msa.android.presentation.screens.calculator.CalculatorScreen
import com.msa.android.presentation.screens.calculators.bullions.BullionScreen
import com.msa.android.presentation.screens.calculators.goldvalue.GoldValueScreen
import com.msa.android.presentation.screens.calculators.goldzakat.GoldZakatScreen
import com.msa.android.presentation.screens.calculators.silvervalue.SilverValueScreen
import com.msa.android.presentation.screens.calculators.silverzakat.SilverZakatScreen
import com.msa.android.presentation.screens.faq.FaqScreen
import com.msa.android.presentation.screens.help.HelpScreen
import com.msa.android.presentation.screens.main.MainScreen
import com.msa.android.presentation.screens.appstatus.AppGate
import com.msa.android.presentation.screens.policy.PolicyScreen
import com.msa.android.presentation.screens.splash.SplashScreen
import com.msa.android.presentation.screens.webview.WebViewScreen

object Routes {
    const val SPLASH = "splash"
    const val MAIN = "main"

    const val BULLIONS = "bullions"
    const val CALCULATOR_HUB = "calculator_hub"
    const val GOLD_VALUE = "gold_value"
    const val GOLD_ZAKAT = "gold_zakat"
    const val SILVER_VALUE = "silver_value"
    const val SILVER_ZAKAT = "silver_zakat"

    const val HELP = "help"
    const val FAQ  = "faq"
    const val ABOUT = "about"
    const val POLICY = "policy"
    const val POLICY_ARG = "type"
    const val POLICY_ROUTE = "$POLICY/{$POLICY_ARG}"

    const val WEBVIEW = "webview"
    const val PRICE_GAP = "price_gap"
    const val INDICATORS = "indicators"
    const val TECHNICAL_ANALYSIS = "technical_analysis"
    const val IMAGE_VIEWER = "image_viewer"
    const val IMAGE_VIEWER_ARG = "url"
    const val IMAGE_VIEWER_ROUTE = "$IMAGE_VIEWER/{$IMAGE_VIEWER_ARG}"
    const val CURRENCY_PICKER = "currency_picker"
    const val QR_SCANNER = "qr_scanner"
    const val PORTFOLIO = "portfolio"
    const val PORTFOLIO_ADD = "portfolio_add"
    const val SETTINGS = "settings"
    const val LANGUAGE = "language"
}

@Composable
fun MSANavGraph() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onRouteHome = {
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            AppGate {
                MainScreen(
                    onOpenWebView = { nav.navigate(Routes.WEBVIEW) },
                    onOpenCalculatorHub = { nav.navigate(Routes.CALCULATOR_HUB) },
                    onOpenHelp = { nav.navigate(Routes.HELP) },
                    onOpenFaq = { nav.navigate(Routes.FAQ) },
                    onOpenAbout = { nav.navigate(Routes.ABOUT) },
                    onOpenUsagePolicy = { nav.navigate("${Routes.POLICY}/${PageType.TERMS.name}") },
                    onOpenRefundPolicy = { nav.navigate("${Routes.POLICY}/${PageType.REFUND.name}") },
                    onOpenPrivacyPolicy = { nav.navigate("${Routes.POLICY}/${PageType.PRIVACY.name}") },
                    onOpenPriceGap = { nav.navigate(Routes.PRICE_GAP) },
                    onOpenIndicators = { nav.navigate(Routes.INDICATORS) },
                    onOpenTechnicalAnalysis = { nav.navigate(Routes.TECHNICAL_ANALYSIS) },
                    onOpenImageViewer = { url ->
                        // Path-arg encoder so '/' and ':' don't break navigation
                        val encoded = java.net.URLEncoder.encode(url, "UTF-8")
                        nav.navigate("${Routes.IMAGE_VIEWER}/$encoded")
                    },
                    onOpenCurrencyPicker = { nav.navigate(Routes.CURRENCY_PICKER) },
                    onOpenQrScanner = { nav.navigate(Routes.QR_SCANNER) },
                    onOpenPortfolio = { nav.navigate(Routes.PORTFOLIO) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onOpenLanguage = { nav.navigate(Routes.LANGUAGE) }
                )
            }
        }

        // اللغة — مفتوحة من «المزيد»، فبتظهر بتوب بار وزرار رجوع
        composable(Routes.LANGUAGE) {
            com.msa.android.presentation.screens.language.LanguageScreen(
                onApplied = { nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }

        // الإعدادات — تحكّم منفصل لكل نوع إشعار
        composable(Routes.SETTINGS) {
            com.msa.android.presentation.screens.settings.SettingsScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.PRICE_GAP) {
            com.msa.android.presentation.screens.pricegap.PriceGapDetailsScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.INDICATORS) {
            com.msa.android.presentation.screens.indicators.IndicatorsScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.TECHNICAL_ANALYSIS) {
            com.msa.android.presentation.screens.technicalanalysis.TechnicalAnalysisScreen(
                onBack = { nav.popBackStack() }
            )
        }

        // Fullscreen pinch-zoomable image viewer — opened from MSA news cards
        composable(
            route = Routes.IMAGE_VIEWER_ROUTE,
            arguments = listOf(
                navArgument(Routes.IMAGE_VIEWER_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString(Routes.IMAGE_VIEWER_ARG).orEmpty()
            val decoded = java.net.URLDecoder.decode(encoded, "UTF-8")
            com.msa.android.presentation.screens.imageviewer.ImageViewerScreen(
                imageUrl = decoded,
                onClose = { nav.popBackStack() }
            )
        }

        // Currency picker — feeds the selection back to BanksViewModel by reusing
        // the ViewModel scoped to the Routes.MAIN back-stack entry.
        composable(Routes.CURRENCY_PICKER) {
            val mainEntry = remember(nav.currentBackStackEntry) {
                nav.getBackStackEntry(Routes.MAIN)
            }
            val banksVm: com.msa.android.presentation.screens.banks.BanksViewModel =
                androidx.hilt.navigation.compose.hiltViewModel(mainEntry)
            val banksState by banksVm.state.collectAsState()
            com.msa.android.presentation.screens.currency.CurrencyPickerScreen(
                currentCurrency = banksState.selectedCurrency,
                onSelect = { code ->
                    banksVm.setCurrency(code)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() }
            )
        }

        // QR scanner — opened from the Home screen icon. Verifies the scanned
        // value against Firestore and shows a result dialog.
        composable(Routes.QR_SCANNER) {
            com.msa.android.presentation.screens.qr.QRScannerScreen(
                onBack = { nav.popBackStack() }
            )
        }

        // Calculator hub — opened from MoreScreen's "حاسبة الذهب والفضة" entry.
        composable(Routes.CALCULATOR_HUB) {
            CalculatorScreen(
                onBack = { nav.popBackStack() },
                onGoldValue = { nav.navigate(Routes.GOLD_VALUE) },
                onGoldZakat = { nav.navigate(Routes.GOLD_ZAKAT) },
                onSilverValue = { nav.navigate(Routes.SILVER_VALUE) },
                onSilverZakat = { nav.navigate(Routes.SILVER_ZAKAT) }
            )
        }

        composable(Routes.BULLIONS)       { BullionScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.GOLD_VALUE)     { GoldValueScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.GOLD_ZAKAT)     { GoldZakatScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SILVER_VALUE)   { SilverValueScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SILVER_ZAKAT)   { SilverZakatScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.HELP)   { HelpScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FAQ)    { FaqScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ABOUT)  { AboutScreen(onBack = { nav.popBackStack() }) }

        composable(
            route = Routes.POLICY_ROUTE,
            arguments = listOf(navArgument(Routes.POLICY_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val typeName = backStackEntry.arguments?.getString(Routes.POLICY_ARG) ?: PageType.PRIVACY.name
            val type = runCatching { PageType.valueOf(typeName) }.getOrDefault(PageType.PRIVACY)
            PolicyScreen(type = type, onBack = { nav.popBackStack() })
        }

        composable(Routes.WEBVIEW) {
            WebViewScreen(onBack = { nav.popBackStack() })
        }

        // Portfolio (محفظتي) — summary + list of holdings
        composable(Routes.PORTFOLIO) {
            com.msa.android.presentation.screens.portfolio.PortfolioScreen(
                onBack = { nav.popBackStack() },
                onAddItem = { nav.navigate(Routes.PORTFOLIO_ADD) }
            )
        }

        // Form for adding a new holding
        composable(Routes.PORTFOLIO_ADD) {
            com.msa.android.presentation.screens.portfolio.AddPortfolioItemScreen(
                onBack = { nav.popBackStack() }
            )
        }
    }
}
