package com.msa.android.presentation.screens.home.banner

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * البانر الإعلاني **التاني** — شريط ثابت في الرئيسية تحت صف الفجوة
 * السعرية.
 *
 * الأول هو الـ popup اللي بيطلع في نص الشاشة أول ما التطبيق يفتح
 * (`HomeBannerPopupHost`). الاتنين بيتحكّم فيهم نفس شاشة «بانرات
 * الرئيسية» في اللوحة، بس كل واحد له تبويب مستقل.
 *
 * ## ليه ViewModel لوحده؟
 *
 * `BannerViewModel` مسؤول عن الـ popup: بيجيب بانرات `popup` وبيفتح
 * النافذة مرة واحدة كل تشغيلة. لو ضفنا الشريط جواه كان الطلبين
 * هيتربطوا ببعض — تحديث الشريط كان ممكن يفتح الـ popup تاني، وفشل
 * الشريط كان هيمنع الـ popup. الفصل بيخلّي كل واحد يقع لوحده.
 *
 * ## مفيش مساحة محجوزة
 *
 * لو مفيش بانرات نشطة في المكان ده، الكومبوزابل **مبيرسمش حاجة
 * خالص** — لا الشريط ولا المسافات اللي حواليه. ده مهم دلوقتي أكتر من
 * الأول: الرئيسية بقت بتحاول تترص في ارتفاع الشاشة، وشريط فاضي
 * بارتفاع ٩٠ نقطة كان هيصغّر باقي الأرقام على الفاضي.
 */
@HiltViewModel
class HomeInlineBannerViewModel @Inject constructor(
    private val repo: BannerRepository
) : ViewModel() {

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            // null = الشبكة وقعت. مفيش كاش، فبنسيب الشريط مخفي —
            // أحسن من خانة سودا مكان الإعلان.
            _banners.value = repo.fetch(BannerPlacement.HOME_INLINE) ?: return@launch
        }
    }
}

/**
 * الشريط نفسه.
 *
 * @param height ارتفاع الشريط. الرئيسية بتمرّر ارتفاع مصغّر على
 *   الشاشات الصغيّرة عشان الصفحة تفضل من غير سكرول.
 */
@Composable
fun HomeInlineBanner(
    modifier: Modifier = Modifier,
    height: Dp = 96.dp,
    onOpenLink: (String) -> Unit = {},
    vm: HomeInlineBannerViewModel = hiltViewModel()
) {
    val items by vm.banners.collectAsStateWithLifecycle()

    if (items.isEmpty()) return

    var fullScreenItem by remember { mutableStateOf<Banner?>(null) }

    BannerSlider(
        items = items,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        // وقّف التقليب وإحنا فاتحين الفل سكرين عشان ما يستهلكش داتا
        // على الفاضي ورا الشاشة.
        paused = fullScreenItem != null,
        cornerRadius = 12.dp,
        onTap = { item ->
            /*
             * اللينك له الأولوية هنا — على عكس الـ popup.
             *
             * الـ popup فيه زرار «اعرف أكثر» منفصل، فالضغط على الصورة
             * نفسها معناه «كبّرها». الشريط مالوش زرار، فالضغط الوحيد
             * المتاح لازم يودّي على اللينك لو البانر له لينك.
             */
            val link = item.linkUrl
            if (!link.isNullOrBlank()) onOpenLink(link) else fullScreenItem = item
        }
    )

    fullScreenItem?.let { item ->
        BannerFullScreenViewer(
            item = item,
            onDismiss = { fullScreenItem = null }
        )
    }
}
