package com.msa.android.presentation.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.msa.android.presentation.common.shareApp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.NumberFormatter
import com.msa.android.presentation.common.RelativeTimeFormatter
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.common.components.PullToRefreshContainer
import com.msa.android.presentation.common.components.FitToHeight
import com.msa.android.presentation.screens.home.banner.HomeBannerPopupHost
import com.msa.android.presentation.screens.home.banner.HomeInlineBanner
import com.msa.android.presentation.theme.GoldenBorder
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mirrors iOS GoldenScreen.swift "الشاشة العالمية":
 *  - Top: title + MSA diamond logo
 *  - Time-since-update text
 *  - Two selectable cards: gold / silver
 *  - Subtitle "متوسط أسعار سوق الدهب الاسترشادي المصري"
 *  - Rows table (Karat / Buy / Sell)
 *  - Two-cell row: "الشاشة العالمية" value + (دولار البنك / دولار الصاغة)
 */
@Composable
fun HomeScreen(
    /**
     * الفجوة السعرية.
     *
     * بياخد المعدن (`gold` / `silver`) عشان الشاشة تفتح على نفس
     * الوضع اللي المستخدم شايفه: الصف نفسه بيعرض فجوة الفضة لما
     * يختار فضة، فلو الشاشة فتحت على الدهب كان الرقمين هيختلفوا.
     */
    onOpenPriceGap: (String) -> Unit = {},
    onOpenQrScanner: () -> Unit = {},
    /** تقويم اجتماعات الفيدرالي — من كارت الاجتماع القادم */
    onOpenFomc: () -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    // خلفية الرئيسية بتتبدّل مع اختيار المعدن: خلفية الفضة لما المستخدم
    // يختار «فضة»، وخلفية الدهب الأصلية لما يرجع لـ«دهب». باقي شاشات
    // التطبيق مش بتتأثر — بتستخدم الافتراضي.
    //
    // لون الأساس بيتبدّل معاها لأنه بيبان تحت الصورة على الشاشات الطويلة،
    // ولو فضل بني وخلفية الفضة رمادية كان هيبان شريط لونه غلط.
    val silver = state.mode == HomeMode.SILVER

    MSABackground(
        textureRes = if (silver) R.drawable.bg_texture_silver else R.drawable.bg_texture,
        baseColor = if (silver) Color(0xFF070707) else Color(0xFF2E2020)
    ) {
        PullToRefreshContainer(
            refreshing = refreshing,
            onRefresh = {
                refreshScope.launch {
                    refreshing = true
                    delay(700)
                    refreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── popup البانر ────────────────────────────────────────────
            // بيظهر في نص الشاشة أول ما الرئيسية تفتح، وبيقفل نفسه لوحده
            // لو مفيش بانرات. مالوش أي تأثير على ترتيب المحتوى تحته لأنه
            // بيتفتح في نافذة منفصلة (Dialog) مش جوه الـ layout.
            //
            // تكرار الظهور متظبط من `POPUP_POLICY` في BannerRepository.kt
            // (الافتراضي: مرة واحدة كل يوم).
            HomeBannerPopupHost(
                onOpenLink = { url ->
                    // TODO: افتح اللينك في الويب فيو بتاعك أو في المتصفح.
                    // مثال: navController.navigate("web?url=" + Uri.encode(url))
                }
            )

            /*
             * الرئيسية بتحاول تترص في ارتفاع الشاشة من غير سكرول.
             *
             * `BoxWithConstraints` بيدينا المساحة المتاحة فعلاً بعد
             * ما نطرح مكان شريط التابات وأزرار النظام، و`FitToHeight`
             * بيصغّر المحتوى كله بنسبة واحدة لو مش راضي يترص. الشاشة
             * الكبيرة بتاخد النسبة ١٫٠ يعني مفيش أي تغيير عن قبل كده.
             *
             * السكرول لسه مكانه: لو الشاشة قصيرة أوي لدرجة إن التصغير
             * المسموح بيه (٧٠٪) مش كفاية، `FitToHeight` بيقول ارتفاعه
             * الحقيقي والعمود اللي بره بيسكرول عادي. مفيش حالة بيتقص
             * فيها محتوى.
             */
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 140.dp)
                    .navigationBarsPadding()
            ) {
            val availableHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
            FitToHeight(targetHeight = availableHeight) {
            Column(modifier = Modifier.fillMaxWidth()) {
                MSATopBar(
                    title = stringResource(R.string.global_screen),
                    showBack = false,
                    showShare = true,
                    onShare = { shareApp(ctx) }
                )

            Spacer(Modifier.height(8.dp))

            // Last updated — Start in RTL = right side (matches iOS screenshot 12)
            Text(
                text = RelativeTimeFormatter.format(ctx, state.lastUpdated),
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(12.dp))

            // Toggle (gold / silver) — pill design matching iOS HomeVC.
            //   Outer:    dark rounded pill (#1A1A1A) with 4dp inner padding.
            //   Selected: gold-filled inner pill (#D4B978), black bold text.
            //   Other:    transparent (shows the outer's bg), white bold text.
            // No icons, no checkmark — minimalist by design.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                com.msa.android.presentation.common.components.SegmentedToggle(
                    selected = state.mode,
                    options = listOf(
                        HomeMode.GOLD   to stringResource(R.string.gold),
                        HomeMode.SILVER to stringResource(R.string.silver)
                    ),
                    onSelect = vm::setMode
                )
            }

            Spacer(Modifier.height(14.dp))

            // Subtitle — Start in RTL = right side (matches iOS)
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.average_market_price),
                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.commission_hint),
                    color = Color.White, fontSize = 12.sp,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(12.dp))

            // Header row — order is [karat, buy, sell] so in RTL it visually reads:
            //   عيار (right) | الشراء (middle) | البيع (left)   ← matches iOS screenshot 17
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                HeaderCell(stringResource(R.string.karat), Modifier.weight(1f))
                HeaderCell(stringResource(R.string.buy),   Modifier.weight(1f))
                HeaderCell(stringResource(R.string.sell),  Modifier.weight(1f))
            }

            Spacer(Modifier.height(6.dp))

            // Rows depending on mode
            //
            // We render rows UNCONDITIONALLY, even before the first Firestore
            // tick arrives. The cells themselves show "جاري التحميل" until a
            // real value lands — preferable to a blank screen because the user
            // immediately sees the grid skeleton + label and knows what's
            // coming. Cf. the OLD `state.gold?.let { ... }` which hid all five
            // rows until the gold object existed.
            if (state.mode == HomeMode.GOLD) {
                val g = state.gold
                PriceRow("24", g?.karat24Buy, g?.karat24Sell, highlighted = false)
                PriceRow("21", g?.karat21Buy, g?.karat21Sell, highlighted = true)
                PriceRow("18", g?.karat18Buy, g?.karat18Sell, highlighted = false)
                PriceRow(stringResource(R.string.gold_pound), g?.goldPoundBuy, g?.goldPoundSell, highlighted = false)
                PriceRow(stringResource(R.string.gold_kilo),  g?.goldKiloBuy,  g?.goldKiloSell,  highlighted = false)
            } else {
                val s = state.silver
                PriceRow("999", s?.karat999Buy, s?.karat999Sell, highlighted = true)
                PriceRow("925", s?.karat925Buy, s?.karat925Sell, highlighted = false)
                PriceRow("800", s?.karat800Buy, s?.karat800Sell, highlighted = false)
                PriceRow(stringResource(R.string.silver_kilo), s?.silverKiloBuy, s?.silverKiloSell, highlighted = false)
            }

            Spacer(Modifier.height(8.dp))

            // "الشاشة العالمية" — single combined view (label + value), value from ounce.
            // Background flashes green when price went up, red when it went down,
            // then animates back to white after 800ms (mirrors iOS OunceDollarCell).
            val globalValue = when (state.mode) {
                HomeMode.GOLD -> state.ouncePrice.goldPrice
                HomeMode.SILVER -> state.ouncePrice.silverPrice
            }
            CombinedLabelValue(
                label = stringResource(R.string.global_screen),
                value = priceOrLoading(globalValue),
                trend = state.ounceTrend,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Dollars row — iOS screenshot 12:
            //   دولار البنك (right) | gap | دولار الصاغة (left)
            // To get that in RTL we put bank FIRST (rightmost), sagha SECOND (leftmost).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CombinedLabelValue(
                    label = stringResource(R.string.dollar_bank),
                    value = priceOrLoading3(state.dollarBank),
                    trend = PriceTrend.NONE,
                    modifier = Modifier.weight(1f)
                )
                CombinedLabelValue(
                    label = stringResource(R.string.dollar_sagha),
                    value = priceOrLoading(state.dollarSagha),
                    trend = state.saghaTrend,
                    modifier = Modifier.weight(1f)
                )
            }

            // Price gap = dollarSagha − dollarBank.
            // Price gap — shown for both metals.
            //
            //   Gold:   gap = (goldOunce  * dollarBank) / 31.1  −  gold24Sell      (signed)
            //   Silver: gap = round( (silverOunce * dollarBank) / 31.1  −  silver999Sell )
            //
            // iOS uses `round()` on the silver branch — we keep that for parity.
            //
            // `gapReady` short-circuits "جاري التحميل" when any input is missing:
            // computing the gap with a zero in the formula produces a nonsense
            // number (e.g. a positive 6325 gap when nothing has loaded yet),
            // which would look like real data to the user.
            val gapReady: Boolean = when (state.mode) {
                HomeMode.GOLD ->
                    state.ouncePrice.goldPrice > 0.0 &&
                    state.dollarBank > 0.0 &&
                    (state.gold?.karat24Sell ?: 0.0) > 0.0
                HomeMode.SILVER ->
                    state.ouncePrice.silverPrice > 0.0 &&
                    state.dollarBank > 0.0 &&
                    (state.silver?.karat999Sell ?: 0.0) > 0.0
            }
            val priceGap: Double = when (state.mode) {
                HomeMode.GOLD -> {
                    val world24KEgp =
                        (state.ouncePrice.goldPrice * state.dollarBank) / 31.1
                    world24KEgp - (state.gold?.karat24Sell ?: 0.0)
                }
                HomeMode.SILVER -> {
                    val world999Egp =
                        (state.ouncePrice.silverPrice * state.dollarBank) / 31.1
                    kotlin.math.round(world999Egp - (state.silver?.karat999Sell ?: 0.0))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onOpenPriceGap(
                            if (state.mode == HomeMode.SILVER) "silver" else "gold"
                        )
                    }
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.price_gap),
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.2f),
                    textAlign = TextAlign.Start
                )
                Text(

                    text = if (gapReady)
                        "${NumberFormatter.int(abs(priceGap))} ${stringResource(R.string.gap_currency_egp)}"
                    else stringResource(R.string.loading),

                    color = if (!gapReady) Color(0xFF666666)
                            else if (priceGap >= 0) Color(0xFF4CAF50)
                            else Color(0xFFE53935),

                    fontSize = 14.sp,

                    fontWeight = FontWeight.SemiBold,

                    modifier = Modifier.weight(1f),

                    textAlign = TextAlign.End

                )
            }

            // ── الاجتماع القادم للفيدرالي ───────────────────────────────
            // قرار الفائدة الأمريكية أكبر محرّك لسعر الذهب العالمي، فمكانه
            // الطبيعي في الرئيسية جنب الأسعار. الكارت بيخفي نفسه لوحده لو
            // مفيش اجتماع معلن أو لو الطلب فشل — مش بيسيب فراغ.
            Spacer(Modifier.height(12.dp))
            com.msa.android.presentation.screens.fomc.FomcNextCard(
                onClick = onOpenFomc,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // ── بانر الرئيسية في نهاية المحتوى ─────────────────────────
            // نفس بانر الـ HOME_INLINE القادم من الـ API، لكن مكانه الآن
            // آخر عنصر في الصفحة بعد الأسعار وكارت الفيدرالي.
            HomeInlineBanner(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
                onOpenLink = { url ->
                    // TODO: افتح الرابط داخل WebView التطبيق.
                }
            )

            // ── Bottom AdMob banner ─────────────────────────────────────
            // Sits at the natural end of the Home content. Uses Google test
            // ad unit ID by default — see AdMobBanner.kt to swap in real IDs.
            Spacer(Modifier.height(12.dp))
//            com.msa.android.presentation.common.components.AdMobBanner(
//                modifier = Modifier.padding(horizontal = 12.dp)
//            )
            Spacer(Modifier.height(8.dp))
            }   // Column: محتوى الرئيسية
            }   // FitToHeight
            }   // Column: السكرول الاحتياطي
            }   // BoxWithConstraints

            // QR scan FAB — floating circular button on the BottomEnd corner
            // (in RTL this lands on the LEFT, opposite the NetDania-style FAB
            // we already place on BottomStart). One-tap entry into the scanner.
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(16.dp)
//                    .padding(bottom = 96.dp)            // sit above the bottom tab bar
//                    .size(56.dp)
//                    .clip(androidx.compose.foundation.shape.CircleShape)
//                    .background(com.msa.android.presentation.theme.GoldenBorder)
//                    .clickable { onOpenQrScanner() },
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.QrCodeScanner,
//                    contentDescription = "Scan QR",
//                    tint = Color.Black,
//                    modifier = Modifier.size(28.dp)
//                )
//            }
        }
        }
    }
}

/**
 * One card with value on one side, label on the other.
 * Background animates between white / green / red depending on trend.
 *
 * Mirrors iOS OunceDollarCell logic (which was commented out in iOS but we restored it here).
 */
@Composable
private fun CombinedLabelValue(
    label: String,
    value: String,
    trend: PriceTrend,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when (trend) {
            PriceTrend.UP -> Color(0xFFE8F5E9)   // light green
            PriceTrend.DOWN -> Color(0xFFFFEBEE) // light red
            PriceTrend.NONE -> Color.White
        },
        animationSpec = tween(durationMillis = 800),
        label = "bgColor"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.Start
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color(0xFFCCCCCC),
        fontSize = 13.sp,
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PriceRow(
    label: String,
    buy: Double?,
    sell: Double?,
    highlighted: Boolean
) {
    // Three cells per row, each rendered as an iOS-style rounded card.
    //
    //   ┌──────────┐  ┌──────────┐  ┌──────────┐
    //   │ 999      │  │ 110.00   │  │ 112.00   │   ← highlighted = gold-tinted bg
    //   └──────────┘  └──────────┘  └──────────┘
    //
    // Non-highlighted cards: solid near-white background, near-black text.
    // Highlighted card: muted gold background, dark text + bold weight.
    // Matches iOS HomeVC price grid.
    //
    // Per product (no-local-cache update): nullable inputs mean "data not loaded
    // yet" — the cell renders "جاري التحميل" instead of a misleading 0.00.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PriceCell(text = label,                  highlighted = highlighted, modifier = Modifier.weight(1f))
        PriceCell(text = priceOrLoading(buy),    highlighted = highlighted, modifier = Modifier.weight(1f))
        PriceCell(text = priceOrLoading(sell),   highlighted = highlighted, modifier = Modifier.weight(1f))
    }
}

/**
 * Formats a price for display, or returns the localized "Loading…" string
 * when the value is missing or hasn't arrived yet from the network.
 *
 * We treat `null` AND `value <= 0` as "not loaded" — the app never legitimately
 * shows a zero price for gold, silver, the dollar, or the ounce, so collapsing
 * both states keeps the call sites simple and the UX honest.
 */
@Composable
private fun priceOrLoading(value: Double?): String =
    if (value == null || value <= 0.0) stringResource(R.string.loading)
    else NumberFormatter.two(value)

/**
 * Same idea but with 3-decimal formatting for the dollar rate row.
 */
@Composable
private fun priceOrLoading3(value: Double?): String =
    if (value == null || value <= 0.0) stringResource(R.string.loading)
    else NumberFormatter.three(value)

/**
 * One pill cell in the price grid. Matches the iOS `priceCellView` style:
 * rounded 10dp corners, ~36dp tall, white or gold-tint background.
 */
@Composable
private fun PriceCell(
    text: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (highlighted) Color(0xFFD4B978)         // muted gold accent
             else            Color(0xFFF5F1E8)          // near-white cream
    val fg = if (highlighted) Color(0xFF1A1A1A)         // near-black on gold
             else            Color(0xFF1A1A1A)          // near-black on cream
    val weight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 14.sp,
            fontWeight = weight
        )
    }
}
