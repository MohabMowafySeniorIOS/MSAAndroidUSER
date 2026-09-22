package com.msa.android.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.domain.model.EconomicEvent
import com.msa.android.domain.model.EventImpact
import com.msa.android.presentation.common.CalendarFormatter
import com.msa.android.presentation.common.flagEmoji
import com.msa.android.presentation.theme.GoldenBorder
import com.msa.android.presentation.theme.GreenTrend
import com.msa.android.presentation.theme.RedTrend

/**
 * عناصر شاشة التقويم المشتركة بين القايمة وشاشة التفاصيل.
 *
 * كل النصوص من `strings.xml` — الأهمية بتوصل كـ enum من الموديل مش
 * كنص من السيرفر، ونفس القاعدة اللي في `FomcComponents`.
 */

/** لون الأهمية — أحمر للعالي، ذهبي للمتوسط، أخضر للمنخفض، رمادي للعطلة */
fun impactColor(impact: EventImpact): Color = when (impact) {
    EventImpact.HIGH    -> RedTrend
    EventImpact.MEDIUM  -> GoldenBorder
    EventImpact.LOW     -> GreenTrend
    EventImpact.HOLIDAY -> Color(0xFF9E9E9E)
}

@Composable
fun impactLabel(impact: EventImpact): String = stringResource(
    when (impact) {
        EventImpact.HIGH    -> R.string.calendar_impact_high
        EventImpact.MEDIUM  -> R.string.calendar_impact_medium
        EventImpact.LOW     -> R.string.calendar_impact_low
        EventImpact.HOLIDAY -> R.string.calendar_impact_holiday
    }
)

/** شارة الأهمية — نفس شكل `FomcStatusChip` عشان الشاشتين يبقوا عيلة واحدة */
@Composable
fun ImpactChip(impact: EventImpact, compact: Boolean = false) {
    val color = impactColor(impact)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = impactLabel(impact),
            color = color,
            fontSize = if (compact) 10.sp else 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * علم + كود العملة.
 *
 * الكود بيتعرض **دايماً** جنب العلم مش بدله: بعض أجهزة أندرويد
 * مبترسمش أعلام الإيموجي وبتعرض الحرفين. لو اعتمدنا على العلم لوحده
 * كان المستخدم على الأجهزة دي مش هيعرف الحدث بتاع أنهي دولة.
 */
@Composable
fun CurrencyBadge(currency: String?, countryCode: String?) {
    val flag = flagEmoji(countryCode)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (flag.isNotEmpty()) {
            Text(text = flag, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
        }
        Text(
            text = currency.orEmpty(),
            color = Color(0xFFCCCCCC),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * خانة رقم واحدة: لافتة صغيرة فوق والقيمة تحت.
 *
 * القيمة الفاضية بتتعرض «—» مش «0». الفرق مش تجميلي: صفر في «معدل
 * التضخم» خبر ضخم، و«—» معناها الرقم لسه ما نزلش.
 */
@Composable
private fun ValueCell(
    label: String,
    value: String?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 9.sp,
            maxLines = 1
        )
        Text(
            text = value ?: "—",
            color = if (value == null) Color(0xFF666666) else color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * صف حدث في القايمة.
 *
 * ```
 * ┌──────────────────────────────────────────────┐
 * │ ٢١:٣٠   مؤتمر الفيدرالي الصحفي          🇺🇸 │
 * │ [عالي]  فعلي —  متوقّع —  سابق —        USD │
 * └──────────────────────────────────────────────┘
 * ```
 *
 * الشريط الملوّن على الحافة بيخلّي المستخدم يمسح القايمة بعينه ويلاقي
 * الأحداث المهمة من غير ما يقرا الشارة — زي الخط الأحمر في الصورة
 * الأصلية بالظبط.
 */
@Composable
fun CalendarEventRow(
    event: EconomicEvent,
    onClick: () -> Unit
) {
    val ctx = LocalContext.current
    val color = impactColor(event.impact)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // شريط الأهمية على الحافة
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(56.dp)
                .background(color)
        )

        Spacer(Modifier.width(10.dp))

        // الوقت + الشارة
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = CalendarFormatter.time(ctx, event.occursAt, event.allDay),
                color = Color.White,
                fontSize = if (event.allDay) 11.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            ImpactChip(event.impact, compact = true)
        }

        Spacer(Modifier.width(10.dp))

        // الاسم + الأرقام
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueCell(
                    label = stringResource(R.string.calendar_actual),
                    value = event.actual,
                    /*
                     * لون الفعلي بيتبع اتجاهه عن المتوقّع.
                     *
                     * ⚠️ أخضر هنا معناه «أعلى من المتوقّع» مش «خبر
                     * كويس»: تضخم أعلى من المتوقّع خبر وحش للدهب.
                     * الشاشة بتعرض الاتجاه، والمستخدم بيفسّره.
                     */
                    color = when (event.isAboveForecast) {
                        true  -> GreenTrend
                        false -> RedTrend
                        null  -> Color.White
                    },
                    modifier = Modifier.weight(1f)
                )
                ValueCell(
                    label = stringResource(R.string.calendar_consensus),
                    value = event.forecast,
                    color = Color(0xFFCCCCCC),
                    modifier = Modifier.weight(1f)
                )
                ValueCell(
                    label = stringResource(R.string.calendar_previous),
                    value = event.previous,
                    color = Color(0xFFCCCCCC),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        CurrencyBadge(event.currency, event.countryCode)
    }
}

/**
 * عنوان اليوم بين المجموعات.
 *
 * «النهاردة» و«بكرة» بدل التاريخ لما ينفع: المستخدم بيعرف مكانه من
 * غير ما يحسب، وهي نفس لغة التبويبات فوق.
 */
@Composable
fun CalendarDayHeader(date: java.time.LocalDate) {
    val ctx = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(GoldenBorder.copy(alpha = 0.2f))
        )
        Text(
            text = CalendarFormatter.dayHeader(ctx, date),
            color = GoldenBorder,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(GoldenBorder.copy(alpha = 0.2f))
        )
    }
}
