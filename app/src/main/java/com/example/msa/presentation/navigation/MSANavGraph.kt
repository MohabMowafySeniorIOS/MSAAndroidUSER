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
    /*
     * الفجوة السعرية — المعدن جزء من الراوت.
     *
     * الرئيسية بتعرض فجوة الدهب أو الفضة حسب الوضع المختار، فالشاشة
     * لازم تفتح على نفس المعدن. الوسيط اختياري وبيقع على الدهب عشان
     * أي استدعاء قديم يفضل شغّال.
     */
    const val PRICE_GAP_ARG = "metal"
    const val PRICE_GAP = "price_gap?$PRICE_GAP_ARG={$PRICE_GAP_ARG}"

    fun priceGap(metal: String = "gold") = "price_gap?metal=$metal"
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
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY_OTP = "verify_otp/{phone}"
    // اجتماعات الفيدرالي
    const val FOMC = "fomc"
    const val FOMC_EVENT_ARG = "eventId"
    const val FOMC_EVENT_ROUTE = "fomc_event/{$FOMC_EVENT_ARG}"

    fun fomcEvent(id: Long) = "fomc_event/$id"

    /*
     * التقويم الاقتصادي.
     *
     * مسار منفصل عن `fomc_event`: الاتنين بيتعرضوا في نفس الشاشة بس
     * كيانين مختلفين تماماً — اجتماع الفيدرالي له عدّاد وقرار فايدة
     * وبيان، وحدث التقويم له ثلاث أرقام وسلسلة تاريخية. مسار واحد
     * كان هيحتاج `if` على نوع الحدث في كل خطوة.
     */
    const val CALENDAR_EVENT_ARG = "calendarEventId"
    const val CALENDAR_EVENT_ROUTE = "calendar_event/{$CALENDAR_EVENT_ARG}"

    fun calendarEvent(id: Long) = "calendar_event/$id"

    // الفروع
    const val BRANCHES = "branches"
    const val BRANCH_ARG = "branchId"
    const val BRANCH_ROUTE = "branch/{$BRANCH_ARG}"

    fun branch(id: Long) = "branch/$id"

    // المتجر — المنتجات والسلة والطلبات
    const val PRODUCTS = "products"
    const val PRODUCT_ARG = "productId"
    const val PRODUCT_ROUTE = "product/{$PRODUCT_ARG}"
    const val CART = "cart"

    /*
     * `orders?highlight={id}` — الطلب اللي لسه اتعمل بيتفتح مفتوح
     * عشان العميل يشوف الكود على طول. الوسيط اختياري لأن الشاشة
     * بتتفتح كمان من «المزيد» من غير طلب بعينه.
     */
    const val ORDERS_ARG = "highlight"
    const val ORDERS = "orders?$ORDERS_ARG={$ORDERS_ARG}"

    fun product(id: Long) = "product/$id"
    fun orders(highlightId: Long? = null) =
        if (highlightId == null) "orders" else "orders?highlight=$highlightId"

    const val WALLET = "wallet"
    const val WALLET_EDITOR = "wallet_editor?id={id}"
    const val PROFILE = "profile"

    fun walletEditor(id: Long? = null) =
        if (id == null) "wallet_editor" else "wallet_editor?id=$id"

    fun verifyOtp(phone: String) = "verify_otp/$phone"
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
                    onOpenPriceGap = { metal -> nav.navigate(Routes.priceGap(metal)) },
                    onOpenIndicators = { nav.navigate(Routes.INDICATORS) },
                    onOpenTechnicalAnalysis = { nav.navigate(Routes.TECHNICAL_ANALYSIS) },
                    onOpenImageViewer = { url ->
                        // Path-arg encoder so '/' and ':' don't break navigation
                        val encoded = java.net.URLEncoder.encode(url, "UTF-8")
                        nav.navigate("${Routes.IMAGE_VIEWER}/$encoded")
                    },
                    onOpenCurrencyPicker = { nav.navigate(Routes.CURRENCY_PICKER) },
                    onOpenQrScanner = { nav.navigate(Routes.QR_SCANNER) },
                    onOpenPortfolio = { nav.navigate(Routes.WALLET) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onOpenLanguage = { nav.navigate(Routes.LANGUAGE) },
                    onOpenLogin = { nav.navigate(Routes.LOGIN) },
                    onOpenProfile = { nav.navigate(Routes.PROFILE) },
                    onOpenFomc = { nav.navigate(Routes.FOMC) },
                    onOpenBranches = { nav.navigate(Routes.BRANCHES) },
                    onOpenProducts = { nav.navigate(Routes.PRODUCTS) },
                    onOpenOrders = { nav.navigate(Routes.orders()) }
                )
            }
        }

        // اجتماعات الفيدرالي — التقويم والتفاصيل
        composable(Routes.FOMC) {
            com.msa.android.presentation.screens.fomc.FomcScreen(
                onBack = { nav.popBackStack() },
                onOpenEvent = { id -> nav.navigate(Routes.fomcEvent(id)) },
                onOpenCalendarEvent = { id -> nav.navigate(Routes.calendarEvent(id)) }
            )
        }

        // تفاصيل حدث في التقويم الاقتصادي
        composable(
            route = Routes.CALENDAR_EVENT_ROUTE,
            arguments = listOf(navArgument(Routes.CALENDAR_EVENT_ARG) { type = NavType.LongType })
        ) { entry ->
            com.msa.android.presentation.screens.calendar.EventDetailsScreen(
                eventId = entry.arguments?.getLong(Routes.CALENDAR_EVENT_ARG) ?: 0L,
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Routes.FOMC_EVENT_ROUTE,
            arguments = listOf(navArgument(Routes.FOMC_EVENT_ARG) { type = NavType.LongType })
        ) { entry ->
            com.msa.android.presentation.screens.fomc.FomcEventDetailsScreen(
                eventId = entry.arguments?.getLong(Routes.FOMC_EVENT_ARG) ?: 0L,
                onBack = { nav.popBackStack() }
            )
        }

        // الفروع — القايمة والتفاصيل
        composable(Routes.BRANCHES) {
            com.msa.android.presentation.screens.branches.BranchesScreen(
                onBack = { nav.popBackStack() },
                onOpenBranch = { id -> nav.navigate(Routes.branch(id)) }
            )
        }

        composable(
            route = Routes.BRANCH_ROUTE,
            arguments = listOf(navArgument(Routes.BRANCH_ARG) { type = NavType.LongType })
        ) { entry ->
            com.msa.android.presentation.screens.branches.BranchDetailsScreen(
                branchId = entry.arguments?.getLong(Routes.BRANCH_ARG) ?: 0L,
                onBack = { nav.popBackStack() }
            )
        }

        // المتجر — المنتجات والسلة والطلبات
        composable(Routes.PRODUCTS) {
            com.msa.android.presentation.screens.shop.ProductsScreen(
                onBack = { nav.popBackStack() },
                onOpenProduct = { id -> nav.navigate(Routes.product(id)) },
                onOpenCart = { nav.navigate(Routes.CART) },
                onNeedsLogin = { nav.navigate(Routes.LOGIN) }
            )
        }

        composable(
            route = Routes.PRODUCT_ROUTE,
            arguments = listOf(navArgument(Routes.PRODUCT_ARG) { type = NavType.LongType })
        ) { entry ->
            com.msa.android.presentation.screens.shop.ProductDetailsScreen(
                productId = entry.arguments?.getLong(Routes.PRODUCT_ARG) ?: 0L,
                onBack = { nav.popBackStack() },
                onOpenCart = { nav.navigate(Routes.CART) },
                onNeedsLogin = { nav.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.CART) {
            com.msa.android.presentation.screens.shop.CartScreen(
                onBack = { nav.popBackStack() },
                onOrderPlaced = { id ->
                    /*
                     * بنشيل السلة والمنتجات من الـ back stack بعد نجاح
                     * الطلب: الرجوع لسلة فاضية بعد ما تطلب بيبان كأن
                     * حاجة ضاعت.
                     */
                    nav.navigate(Routes.orders(id)) {
                        popUpTo(Routes.PRODUCTS) { inclusive = true }
                    }
                },
                onBrowseProducts = { nav.popBackStack() }
            )
        }

        composable(
            route = Routes.ORDERS,
            arguments = listOf(
                navArgument(Routes.ORDERS_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            com.msa.android.presentation.screens.shop.OrdersScreen(
                onBack = { nav.popBackStack() },
                onBrowseProducts = { nav.navigate(Routes.PRODUCTS) },
                highlightOrderId = entry.arguments
                    ?.getString(Routes.ORDERS_ARG)
                    ?.toLongOrNull()
            )
        }

        // المحفظة (المخزون)
        composable(Routes.WALLET) {
            com.msa.android.presentation.screens.wallet.WalletScreen(
                onBack = { nav.popBackStack() },
                onAdd = { nav.navigate(Routes.walletEditor()) },
                onEdit = { id -> nav.navigate(Routes.walletEditor(id)) },
                onLogin = { nav.navigate(Routes.LOGIN) }
            )
        }

        composable(
            route = Routes.WALLET_EDITOR,
            arguments = listOf(navArgument("id") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            com.msa.android.presentation.screens.wallet.WalletEditorScreen(
                entryId = entry.arguments?.getString("id")?.toLongOrNull(),
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() }
            )
        }

        // البروفايل
        composable(Routes.PROFILE) {
            com.msa.android.presentation.screens.profile.ProfileScreen(
                onBack = { nav.popBackStack() },
                // بعد الخروج أو الحذف بنرجع للرئيسية
                onSignedOut = { nav.popBackStack(Routes.MAIN, inclusive = false) }
            )
        }

        // الحساب — دخول وتسجيل وتأكيد الكود
        composable(Routes.LOGIN) {
            com.msa.android.presentation.screens.auth.LoginScreen(
                onBack = { nav.popBackStack() },
                // بعد الدخول بنرجع لشاشة المزيد ونشيل شاشات الحساب من الـ stack
                onLoggedIn = { nav.popBackStack(Routes.MAIN, inclusive = false) },
                onGoRegister = {
                    nav.navigate(Routes.REGISTER) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onNeedVerify = { phone -> nav.navigate(Routes.verifyOtp(phone)) }
            )
        }

        composable(Routes.REGISTER) {
            com.msa.android.presentation.screens.auth.RegisterScreen(
                onBack = { nav.popBackStack() },
                onCodeSent = { phone -> nav.navigate(Routes.verifyOtp(phone)) },
                onGoLogin = {
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } }
                }
            )
        }

        composable(
            route = Routes.VERIFY_OTP,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { entry ->
            com.msa.android.presentation.screens.auth.VerifyOtpScreen(
                phone = entry.arguments?.getString("phone").orEmpty(),
                onBack = { nav.popBackStack() },
                onVerified = { nav.popBackStack(Routes.MAIN, inclusive = false) }
            )
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

        composable(
            route = Routes.PRICE_GAP,
            arguments = listOf(
                navArgument(Routes.PRICE_GAP_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            com.msa.android.presentation.screens.pricegap.PriceGapDetailsScreen(
                onBack = { nav.popBackStack() },
                metal = com.msa.android.presentation.screens.pricegap.GapMetal.from(
                    entry.arguments?.getString(Routes.PRICE_GAP_ARG)
                )
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
