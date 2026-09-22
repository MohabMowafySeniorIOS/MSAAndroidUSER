package com.msa.android.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.msa.android.R
import com.msa.android.domain.model.EconomicEvent
import com.msa.android.domain.model.EventDetails
import com.msa.android.domain.model.EventHistory
import com.msa.android.domain.repository.CalendarRepository
import com.msa.android.presentation.common.CalendarFormatter
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.common.flagEmoji
import com.msa.android.presentation.theme.GoldenBorder
import com.msa.android.presentation.theme.GreenTrend
import com.msa.android.presentation.theme.RedTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val repo: CalendarRepository
) : ViewModel() {

    data class State(
        val loading: Boolean = true,
        val failed: Boolean = false,
        val details: EventDetails? = null,
        val history: EventHistory? = null,
        /** بيتحدّث كل ثانية عشان العدّاد التنازلي يمشي */
        val now: Instant = Instant.now()
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failed = false) }

            val details = repo.details(id).getOrNull()

            if (details == null) {
                _state.update { it.copy(loading = false, failed = true) }
                return@launch
            }

            _state.update { it.copy(loading = false, details = details) }

            /*
             * التاريخ بيتجاب بعد التفاصيل مش معاها.
             *
             * الأرقام فوق هي اللي المستخدم فتح الشاشة عشانها؛ لو
             * استنينا الطلبين مع بعض، طلب التاريخ البطيء كان هيأخّر
             * ظهور معلومة جاهزة. وفشل التاريخ ما بيعطّلش الشاشة —
             * تبويب الرسم بس اللي بيبقى فاضي.
             */
            repo.history(id).onSuccess { h ->
                _state.update { it.copy(history = h) }
            }
        }

        // العدّاد التنازلي
        viewModelScope.launch {
            while (true) {
                _state.update { it.copy(now = Instant.now()) }
                delay(1000)
            }
        }
    }
}

private enum class DetailsTab { CHART, HISTORY }

/**
 * تفاصيل حدث في التقويم الاقتصادي.
 *
 * ```
 * ┌─────────────────────────────────┐
 * │ الدولة 🇺🇸  الأهمية [عالي]  USD │
 * ├────────────────┬────────────────┤
 * │ آخر إصدار      │ الإصدار القادم │
 * │ سابق   4.1%    │ التاريخ  ١٦ سب │
 * │ متوقّع 4.2%    │ المتبقي  ١٨ د  │
 * │ فعلي   4.3%    │ متوقّع   —     │
 * ├────────────────┴────────────────┤
 * │ التصنيف: التضخم   الوحدة: %     │
 * │ وصف المؤشر… اعرض المزيد         │
 * ├─────────────────────────────────┤
 * │ [الرسم البياني] [التاريخ]        │
 * └─────────────────────────────────┘
 * ```
 */
@Composable
fun EventDetailsScreen(
    eventId: Long,
    onBack: () -> Unit,
    vm: EventDetailsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) { vm.load(eventId) }

    var tab by remember { mutableStateOf(DetailsTab.CHART) }
    var expanded by remember { mutableStateOf(false) }

    MSABackground {
        Column(Modifier.fillMaxSize()) {

            MSATopBar(
                title = state.details?.event?.title
                    ?: stringResource(R.string.calendar_title),
                showBack = true,
                onBack = onBack
            )

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldenBorder)
                }

                state.failed || state.details == null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.calendar_failed),
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                else -> {
                    val details = state.details!!
                    val event = details.event

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "header") { EventHeaderCard(event) }

                        item(key = "releases") {
                            ReleasesCard(
                                event = event,
                                next = details.next,
                                now = state.now
                            )
                        }

                        // التصنيف والوصف بيظهروا **بس** لو فيهم قيمة.
                        // المصدر المجاني مبيبعتهمش، والقسم الفاضي كان
                        // هيبان كأن فيه بيانات ناقصة.
                        if (event.category != null || event.unit != null || event.description != null) {
                            item(key = "about") {
                                AboutCard(
                                    event = event,
                                    expanded = expanded,
                                    onToggle = { expanded = !expanded }
                                )
                            }
                        }

                        item(key = "tabs") {
                            DetailsTabs(selected = tab, onSelect = { tab = it })
                        }

                        val history = state.history

                        if (history == null || (tab == DetailsTab.CHART && history.points.size < 2)) {
                            item(key = "no_history") { NoHistoryCard() }
                        } else if (tab == DetailsTab.CHART) {
                            item(key = "chart") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x66000000))
                                        .border(1.dp, GoldenBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    EventChart(points = history.points, unit = history.unit)
                                }
                            }
                        } else {
                            items(
                                count = history.releases.size,
                                key = { i -> "release_${history.releases[i].id}" }
                            ) { i ->
                                ReleaseRow(history.releases[i])
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── كروت الشاشة ───────────────────────────────────────────────────

@Composable
private fun EventHeaderCard(event: EconomicEvent) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LabelValue(
                label = stringResource(R.string.calendar_country),
                value = flagEmoji(event.countryCode).ifEmpty { event.currency.orEmpty() },
                modifier = Modifier.weight(1f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.calendar_impact),
                    color = Color(0xFF888888),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                ImpactChip(event.impact)
            }
            LabelValue(
                label = stringResource(R.string.calendar_symbol),
                value = event.currency.orEmpty(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * آخر إصدار جنب الإصدار القادم.
 *
 * العمودين جنب بعض مقصودين: المستخدم بيقارن الرقم اللي صدر بالرقم
 * المتوقّع في المرة الجاية، والمقارنة دي أصعب لو كانوا تحت بعض.
 */
@Composable
private fun ReleasesCard(event: EconomicEvent, next: EconomicEvent?, now: Instant) {
    val ctx = LocalContext.current

    Card {
        Row(modifier = Modifier.fillMaxWidth()) {

            // ── آخر إصدار ──────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.calendar_latest_release),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Line(stringResource(R.string.calendar_previous), event.previous)
                Line(stringResource(R.string.calendar_consensus), event.forecast)
                Line(
                    label = stringResource(R.string.calendar_actual),
                    value = event.actual,
                    // نفس قاعدة الصف: أخضر = أعلى من المتوقّع،
                    // مش «خبر كويس»
                    color = when (event.isAboveForecast) {
                        true  -> GreenTrend
                        false -> RedTrend
                        null  -> Color.White
                    }
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(110.dp)
                    .background(GoldenBorder.copy(alpha = 0.2f))
            )

            Spacer(Modifier.width(14.dp))

            // ── الإصدار القادم ─────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.calendar_next_release),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (next == null) {
                    Text(
                        text = stringResource(R.string.calendar_no_next_release),
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                } else {
                    Line(
                        stringResource(R.string.calendar_date),
                        CalendarFormatter.releaseDate(ctx, next.occursAt)
                    )
                    Line(
                        stringResource(R.string.calendar_time_left),
                        // `null` لما الميعاد يعدّي — العدّاد بيختفي
                        // بدل ما يعرض رقم بالسالب
                        CalendarFormatter.timeLeft(ctx, next.occursAt, now),
                        color = GoldenBorder
                    )
                    Line(stringResource(R.string.calendar_consensus), next.forecast)
                }
            }
        }
    }
}

@Composable
private fun AboutCard(event: EconomicEvent, expanded: Boolean, onToggle: () -> Unit) {
    Card {
        Row(modifier = Modifier.fillMaxWidth()) {
            event.category?.let {
                LabelValue(
                    label = stringResource(R.string.calendar_category),
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
            event.unit?.let {
                LabelValue(
                    label = stringResource(R.string.calendar_unit),
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        event.description?.let { description ->
            Spacer(Modifier.height(10.dp))

            Text(
                text = description,
                color = Color(0xFFCCCCCC),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                // مقفول على ٣ سطور لحد ما المستخدم يفتحه — الوصف
                // ممكن يبقى فقرة كاملة تدفن الرسم البياني تحتها
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(
                    if (expanded) R.string.calendar_show_less else R.string.calendar_show_more
                ),
                color = GoldenBorder,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onToggle() }
            )
        }
    }
}

@Composable
private fun DetailsTabs(selected: DetailsTab, onSelect: (DetailsTab) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            DetailsTab.CHART to R.string.calendar_chart,
            DetailsTab.HISTORY to R.string.calendar_history
        ).forEach { (value, labelRes) ->
            val active = value == selected

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) GoldenBorder else Color(0x66000000))
                    .border(
                        1.dp,
                        GoldenBorder.copy(alpha = if (active) 1f else 0.3f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(labelRes),
                    color = if (active) Color.Black else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * مفيش تاريخ لسه.
 *
 * السطر التاني مهم: المصدر بيدي الأسبوع الحالي بس، والتاريخ بيتكوّن
 * من أرشيفنا إصدار ورا إصدار. من غير التوضيح ده، شاشة فاضية مكتوب
 * عليها «مفيش بيانات» بتبان كعطل مش كحاجة بتتبني.
 */
@Composable
private fun NoHistoryCard() {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.calendar_no_history),
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = stringResource(R.string.calendar_history_building),
                color = Color(0xFF999999),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReleaseRow(event: EconomicEvent) {
    val ctx = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x44000000))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = CalendarFormatter.releaseDate(ctx, event.occursAt),
            color = Color(0xFFCCCCCC),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = event.actual ?: stringResource(R.string.calendar_not_released),
            color = if (event.actual == null) Color(0xFF666666) else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── عناصر صغيرة ───────────────────────────────────────────────────

@Composable
private fun Card(
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        content = content
    )
}

@Composable
private fun LabelValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = Color(0xFF888888), fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** سطر «لافتة ← قيمة». الفاضي «—» مش صفر. */
@Composable
private fun Line(label: String, value: String?, color: Color = Color.White) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value ?: "—",
            color = if (value == null) Color(0xFF666666) else color,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
