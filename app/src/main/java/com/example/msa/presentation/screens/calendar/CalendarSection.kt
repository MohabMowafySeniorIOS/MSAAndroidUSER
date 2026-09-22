package com.msa.android.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.domain.model.EventImpact
import com.msa.android.presentation.common.flagEmoji
import com.msa.android.presentation.theme.GoldenBorder

/**
 * قسم التقويم الاقتصادي جوه شاشة «مواعيد الفيدرالي».
 *
 * ## ليه قسم مش شاشة؟
 *
 * الاتنين نفس السياق: «إمتى الخبر اللي هيحرّك الدهب». شاشة منفصلة
 * كانت هتحتاج المستخدم يعرف إنها موجودة أصلاً؛ تبويب جنب «القادمة»
 * و«السابقة» بيخلّيه يلاقيها وهو بيدوّر على موعد الاجتماع.
 *
 * القسم ده بيرجّع عناصر `LazyColumn` (مش `LazyColumn` بنفسه) عشان
 * يترص جوه قايمة الشاشة الموجودة من غير قايمتين متداخلتين — التداخل
 * ده بيكسّر السكرول في كومبوز.
 */
fun LazyListScope.calendarSection(
    state: CalendarViewModel.State,
    onSelectRange: (CalendarRange) -> Unit,
    onOpenFilter: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    onRetry: () -> Unit
) {
    item(key = "calendar_tabs") {
        CalendarRangeTabs(
            selected = state.range,
            hasFilter = state.hasFilter,
            onSelect = onSelectRange,
            onOpenFilter = onOpenFilter
        )
        Spacer(Modifier.height(6.dp))
    }

    when {
        state.loading && state.days.isEmpty() -> item(key = "calendar_loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldenBorder)
            }
        }

        state.failed -> item(key = "calendar_failed") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.calendar_failed),
                    color = Color.White,
                    fontSize = 15.sp
                )
                com.msa.android.presentation.common.components.GoldButton(
                    text = stringResource(R.string.retry),
                    big = false,
                    onClick = onRetry
                )
            }
        }

        else -> {
            val days = state.visibleDays

            if (days.isEmpty()) {
                item(key = "calendar_empty") {
                    Text(
                        // رسالة مختلفة لما يكون فيه فلتر: «مفيش أحداث»
                        // على يوم مفلتر بتخلّي المستخدم يفتكر إن
                        // التقويم بايظ بدل ما يفك الفلتر
                        text = stringResource(
                            if (state.hasFilter) R.string.calendar_empty_filtered
                            else R.string.calendar_empty
                        ),
                        color = Color(0xFF999999),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                    )
                }
            }

            days.forEach { day ->
                item(key = "day_${day.date}") {
                    CalendarDayHeader(day.date)
                }

                items(
                    items = day.events,
                    key = { "event_${it.id}" }
                ) { event ->
                    CalendarEventRow(event = event, onClick = { onOpenEvent(event.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * تبويبات المدى + زرار الفلتر.
 *
 * التبويبات بتتحط في صف بيسكرول أفقياً: «الأسبوع ده» بالعربي أطول من
 * «This Week»، وعلى شاشة ٣٦٠dp الأربع تبويبات ثابتة كانت هتتقص.
 */
@Composable
private fun CalendarRangeTabs(
    selected: CalendarRange,
    hasFilter: Boolean,
    onSelect: (CalendarRange) -> Unit,
    onOpenFilter: () -> Unit
) {
    val tabs = listOf(
        CalendarRange.YESTERDAY to R.string.calendar_yesterday,
        CalendarRange.TODAY     to R.string.calendar_today,
        CalendarRange.TOMORROW  to R.string.calendar_tomorrow,
        CalendarRange.WEEK      to R.string.calendar_this_week
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEach { (range, labelRes) ->
                val active = range == selected

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (active) GoldenBorder else Color(0x66000000))
                        .border(
                            1.dp,
                            GoldenBorder.copy(alpha = if (active) 1f else 0.3f),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onSelect(range) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(labelRes),
                        color = if (active) Color.Black else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        // زرار الفلتر — نقطة ذهبية فوقه لما يكون شغّال، عشان المستخدم
        // ما يقعدش يدوّر على أحداث مخفية من غير ما يعرف السبب
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .border(1.dp, GoldenBorder.copy(alpha = 0.4f), CircleShape)
                .clickable { onOpenFilter() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚙",
                color = if (hasFilter) GoldenBorder else Color(0xFFCCCCCC),
                fontSize = 16.sp
            )

            if (hasFilter) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(GoldenBorder)
                )
            }
        }
    }
}

/**
 * ورقة الفلتر — الأهمية والعملة.
 *
 * العملات جاية من السيرفر مش مكتوبة في التطبيق: لو المصدر ضاف عملة
 * أو وقّف واحدة، القايمة بتتحدّث لوحدها من غير تحديث للتطبيق.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CalendarFilterSheet(
    state: CalendarViewModel.State,
    onToggleImpact: (EventImpact) -> Unit,
    onToggleCurrency: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF141010))
                .border(1.dp, GoldenBorder.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.calendar_filter),
                color = GoldenBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // ── الأهمية ──────────────────────────────────────────
            Text(
                text = stringResource(R.string.calendar_filter_impact),
                color = Color(0xFFCCCCCC),
                fontSize = 13.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(EventImpact.HIGH, EventImpact.MEDIUM, EventImpact.LOW).forEach { impact ->
                    val active = impact in state.impacts
                    val color = impactColor(impact)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) color.copy(alpha = 0.25f) else Color(0x33FFFFFF))
                            .border(
                                1.dp,
                                color.copy(alpha = if (active) 0.9f else 0.25f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggleImpact(impact) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = impactLabel(impact),
                            color = if (active) color else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── العملة ───────────────────────────────────────────
            if (state.availableCurrencies.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_filter_currency),
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp
                )

                // `FlowRow` بدل شبكة ثابتة: عدد العملات بيتغيّر حسب
                // اللي المصدر بيبعته، وعمود ثابت كان هيسيب فراغ أو يقص
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.availableCurrencies.forEach { currency ->
                        val active = currency.code in state.currencies

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (active) GoldenBorder.copy(alpha = 0.22f) else Color(0x33FFFFFF))
                                .border(
                                    1.dp,
                                    GoldenBorder.copy(alpha = if (active) 0.9f else 0.2f),
                                    RoundedCornerShape(9.dp)
                                )
                                .clickable { onToggleCurrency(currency.code) }
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val flag = flagEmoji(currency.countryCode)
                            if (flag.isNotEmpty()) Text(text = flag, fontSize = 13.sp)

                            Text(
                                text = currency.code,
                                color = if (active) GoldenBorder else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GoldenBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onClear() }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.calendar_filter_reset),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldenBorder)
                        .clickable { onDismiss() }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.calendar_filter_apply),
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
