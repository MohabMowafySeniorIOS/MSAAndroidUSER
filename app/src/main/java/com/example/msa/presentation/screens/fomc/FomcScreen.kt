package com.msa.android.presentation.screens.fomc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.domain.model.FomcEvent
import com.msa.android.presentation.common.FomcFormatter
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.screens.appstatus.ScreenMaintenanceCard
import com.msa.android.presentation.screens.appstatus.ScreenMaintenanceViewModel
import com.msa.android.presentation.screens.calendar.calendarSection
import com.msa.android.presentation.theme.GoldenBorder

/**
 * اجتماعات الفيدرالي والتقويم الاقتصادي.
 *
 * فوق: الاجتماع القادم بعدّاد تنازلي. تحت: التقويم الاقتصادي.
 *
 * تبويبتي «القادمة» و«السابقة» كانوا هنا واتشالوا: قايمة اجتماعات
 * الفيدرالي ٨ صفوف في السنة، والمستخدم اللي فاتح الشاشة دي عايز يعرف
 * «إيه الخبر الجاي» — وde التقويم. الاجتماع القادم نفسه لسه فوق،
 * والضغط عليه بيفتح تفاصيله.
 *
 * الشاشة بتقرا من السيرفر بس، والسيرفر بيقرا من قاعدة بياناته — يعني
 * لو المصدر الخارجي وقع، الشاشة بتفضل تعرض آخر بيانات صحيحة بدل ما
 * تفضى.
 */
@Composable
fun FomcScreen(
    onBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    /** حدث في التقويم الاقتصادي — شاشة تفاصيل مختلفة عن الاجتماعات */
    onOpenCalendarEvent: (Long) -> Unit = {},
    /**
     * مراقب أعلام الصيانة في `appVersion/appVersion`.
     *
     * مستنّي `FedralliMaintain`. مراقب حيّ، يعني قلب المفتاح من لوحة
     * Firebase بيغيّر الشاشة للمستخدم اللي **فاتحها دلوقتي** من غير ما
     * يقفل التطبيق ويفتحه.
     */
    maintenanceVm: ScreenMaintenanceViewModel = hiltViewModel()
) {
    val maintenance by maintenanceVm.state.collectAsStateWithLifecycle()
    val federal = maintenance.federal

    /*
     * الصيانة بتستبدل **جسم الشاشة بس**، والهيدر بزرار الرجوع فوقه
     * فاضل مكانه.
     *
     * لو غطّينا الشاشة كلها زي ما بيحصل في التبويبات، المستخدم اللي
     * جاي من الرئيسية كان هيتحبس في صفحة مالهاش خروج — التبويبات
     * مالهاش المشكلة دي لأن شريطها السفلي برّه الجيت.
     */
    if (federal.isUnderMaintenance) {
        MSABackground {
            Column(Modifier.fillMaxSize()) {
                MSATopBar(
                    title = stringResource(R.string.fomc_title),
                    showBack = true,
                    onBack = onBack
                )
                ScreenMaintenanceCard(
                    title = federal.title.ifBlank { stringResource(R.string.maintenance_screen_title) },
                    message = federal.message.ifBlank { stringResource(R.string.maintenance_screen_message) },
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                )
            }
        }
        return
    }

    FomcContent(
        onBack = onBack,
        onOpenEvent = onOpenEvent,
        onOpenCalendarEvent = onOpenCalendarEvent
    )
}

/**
 * جسم الشاشة الحقيقي.
 *
 * اتفصل عن [FomcScreen] عشان الـ ViewModels بتاعته **ما تتعملش أصلاً**
 * والشاشة في صيانة: `hiltViewModel()` في البارامترات بيتنفّذ وقت
 * النداء، فطول ما الدالة دي مش متنادية مفيش نداء للسيرفر ولا عدّاد
 * ثانية شغّال ورا كارت ساكن. ده نفس سلوك [ScreenMaintenanceGate] مع
 * التبويبات — بياخد المحتوى كـ lambda وما بينادهاش.
 */
@Composable
private fun FomcContent(
    onBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    onOpenCalendarEvent: (Long) -> Unit,
    vm: FomcViewModel = hiltViewModel(),
    calendarVm: com.msa.android.presentation.screens.calendar.CalendarViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val calendarState by calendarVm.state.collectAsStateWithLifecycle()

    // ورقة الفلتر — حالة عرض بس، الفلاتر نفسها في الـ ViewModel
    var filterOpen by rememberSaveable { mutableStateOf(false) }

    if (filterOpen) {
        com.msa.android.presentation.screens.calendar.CalendarFilterSheet(
            state = calendarState,
            onToggleImpact = calendarVm::toggleImpact,
            onToggleCurrency = calendarVm::toggleCurrency,
            onClear = calendarVm::clearFilters,
            onDismiss = { filterOpen = false }
        )
    }

    MSABackground {
        Column(Modifier.fillMaxSize()) {

            MSATopBar(
                title = stringResource(R.string.fomc_title),
                showBack = true,
                onBack = onBack
            )

            /*
             * الشاشة بقت **التقويم الاقتصادي** مع كارت الاجتماع
             * القادم فوقه.
             *
             * تبويبتي «القادمة» و«السابقة» اتشالوا: قايمة اجتماعات
             * الفيدرالي ٨ صفوف في السنة، والمستخدم اللي فاتح الشاشة
             * دي عايز يعرف «إيه الخبر الجاي» — وde التقويم.
             * الاجتماع القادم نفسه لسه فوق بعدّاده، والضغط عليه
             * بيفتح تفاصيله.
             */
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                /*
                 * الكارت بيظهر لما بيانات الفيدرالي توصل، ومبيظهرش
                 * قبل كده ولا لو الطلب فشل.
                 *
                 * مفيش لودينج على مستوى الشاشة كلها: التقويم مصدره
                 * مختلف تماماً وبيتحمّل لوحده، فلو استنينا الفيدرالي
                 * كان عطل فيه هيقفل التقويم من غير أي سبب.
                 */
                state.next?.let { next ->
                    item(key = "next") {
                        NextMeetingHero(
                            event = next,
                            secondsRemaining = state.secondsUntilNext,
                            onClick = { onOpenEvent(next.id) }
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }

                calendarSection(
                    state = calendarState,
                    onSelectRange = calendarVm::setRange,
                    onOpenFilter = { filterOpen = true },
                    onOpenEvent = onOpenCalendarEvent,
                    onRetry = calendarVm::refresh
                )

                item(key = "calendar_source") {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.calendar_source_note),
                        color = Color(0xFF9E9E9E),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * رأس الشاشة — الاجتماع القادم بعدّاد.
 *
 * الحدث كله قابل للضغط عشان المستخدم يوصل لتفاصيله من غير ما يدوّر
 * عليه تاني في القايمة تحت.
 */
@Composable
private fun NextMeetingHero(
    event: FomcEvent,
    secondsRemaining: Long?,
    onClick: () -> Unit
) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x80000000))
            .border(1.dp, GoldenBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.fomc_next_meeting),
            color = GoldenBorder,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = FomcFormatter.range(ctx, event.meetingStartAt, event.meetingEndAt),
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        FomcStatusChip(event.statusAt())

        FomcCountdown(secondsRemaining)

        event.decisionAt?.let {
            Text(
                text = stringResource(
                    R.string.fomc_decision_at_value,
                    FomcFormatter.dateTime(ctx, it)
                ),
                color = Color(0xFFCCCCCC),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // التوقيت الرسمي كسطر توضيحي — الوقت فوق معروض بتوقيت الجهاز
        event.officialTimezone?.let { tz ->
            Text(
                text = stringResource(R.string.fomc_official_timezone, tz),
                color = Color(0xFF9E9E9E),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
