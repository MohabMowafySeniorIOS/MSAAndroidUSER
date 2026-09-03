package com.msa.android.presentation.screens.home.banner

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * بانر الرئيسية كـ **popup في نص الشاشة**.
 *
 * بيظهر أول ما الرئيسية تفتح (حسب سياسة التكرار في BannerRepository)،
 * وجواه نفس السلايدر بتاع الـ iOS: صور وفيديوهات بتقلب أوتوماتيك.
 *
 * الشكل:
 *   ┌─────────────────────┐
 *   │                 ✕   │  ← زرار إغلاق فوق الكارت
 *   │ ┌─────────────────┐ │
 *   │ │                 │ │
 *   │ │   الميديا 3:4    │ │  ← سلايدر: صورة/فيديو + عنوان + نقط
 *   │ │                 │ │
 *   │ └─────────────────┘ │
 *   │ [   اعرف أكثر    ]  │  ← زرار ذهبي (بيظهر لو البانر عنده لينك)
 *   └─────────────────────┘
 *
 * نادِها مرة واحدة من HomeScreen:
 *     HomeBannerPopupHost(onOpenLink = { url -> /* افتح الويب فيو */ })
 */
@Composable
fun HomeBannerPopupHost(
    onOpenLink: (String) -> Unit = {},
    vm: BannerViewModel = hiltViewModel()
) {
    val items by vm.banners.collectAsStateWithLifecycle()
    val visible by vm.popupVisible.collectAsStateWithLifecycle()

    if (!visible || items.isEmpty()) return

    BannerPopup(
        items = items,
        onDismiss = vm::dismissPopup,
        onOpenLink = onOpenLink
    )
}

// ─────────────────────────────────────────────────────────────

@Composable
fun BannerPopup(
    items: List<Banner>,
    onDismiss: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    if (items.isEmpty()) return

    // البانر الظاهر دلوقتي — زرار الـ CTA بيتغيّر معاه.
    var current by remember { mutableStateOf(items.first()) }

    // لو المستخدم دوس على الميديا نفسها بنفتحها بملء الشاشة فوق الـ popup.
    var fullScreenItem by remember { mutableStateOf<Banner?>(null) }

    // أنيميشن الدخول: بيكبر ويظهر بنعومة بدل ما ينطّ في وش المستخدم.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    val popupScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "popupScale"
    )
    val popupAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(220),
        label = "popupAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // الضغط على الخلفية بيقفل. بننزع الـ ripple عشان مفيش
                // حاجة مرئية المفروض "تتضغط" هنا.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .background(Color.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    // عرض الشاشة كامل ناقص ١٦ يمين وشمال.
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        // ⚠️ الأسماء هنا لازم تكون مختلفة عن خصائص
                        // GraphicsLayerScope (scaleX/scaleY/alpha) — لو سميناها
                        // `alpha` الكومبايلر هيقراها كخاصية الـ scope نفسه
                        // ويطلع `alpha = alpha` اللي مبيعملش حاجة.
                        scaleX = popupScale
                        scaleY = popupScale
                        alpha = popupAlpha
                    }
                    // بنمتص الضغطات هنا عشان الضغط على الكارت نفسه
                    // ما يوصلش للخلفية ويقفل الـ popup.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    )
            ) {

                // ── زرار الإغلاق ──
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 10.dp, end = 2.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // ── كارت الميديا ──
                BannerSlider(
                    items = items,
                    modifier = Modifier
                        .fillMaxWidth()
                        // 16:9 — الارتفاع بيتحسب من العرض تلقائياً.
                        // aspectRatio = العرض ÷ الارتفاع، فـ 16f/9f = مستطيل
                        // عريض. غيّرها لـ 1f لمربع أو 3f/4f لطولي.
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, BannerGold.copy(alpha = 0.22f), RoundedCornerShape(24.dp)),
                    // وقّف التقليب وإحنا فاتحين الفل سكرين فوقه.
                    paused = fullScreenItem != null,
                    cornerRadius = 24.dp,
                    onTap = { item ->
                        val link = item.linkUrl
                        if (!link.isNullOrBlank()) {
                            onDismiss()
                            onOpenLink(link)
                        } else {
                            fullScreenItem = item
                        }
                    },
                    onPageChanged = { current = it }
                )

                // ── زرار الإجراء ──
                // بيظهر بس لو البانر الحالي عنده لينك. لو مفيش لينك
                // مفيش زرار ولا حتى المسافة اللي فوقه — أحسن من زرار
                // بيودّي على لا حاجة، ومن فراغ تحت الكارت.
                val link = current.linkUrl
                if (!link.isNullOrBlank()) {

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BannerCream, BannerGold)
                                )
                            )
                            .clickable {
                                onDismiss()
                                onOpenLink(link)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "اعرف أكثر",
                            color = BannerInk,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = BannerInk,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }

    fullScreenItem?.let { item ->
        BannerFullScreenViewer(
            item = item,
            onDismiss = { fullScreenItem = null }
        )
    }
}
