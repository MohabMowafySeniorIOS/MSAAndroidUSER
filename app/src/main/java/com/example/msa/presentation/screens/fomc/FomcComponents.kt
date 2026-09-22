package com.msa.android.presentation.screens.fomc

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.domain.model.FomcEvent
import com.msa.android.domain.model.FomcStatus
import com.msa.android.domain.model.RateDirection
import com.msa.android.presentation.common.FomcFormatter
import com.msa.android.presentation.theme.GoldenBorder
import com.msa.android.presentation.theme.GreenTrend
import com.msa.android.presentation.theme.RedTrend

/**
 * العناصر المشتركة بين شاشة الاجتماعات، شاشة التفاصيل، وكارت الرئيسية.
 *
 * كل النصوص جاية من `strings.xml` — مفيش أي نص مكتوب في الكود هنا،
 * والحالة (`upcoming`) بتوصل كـ enum من الموديل مش كنص من السيرفر.
 */

/** شارة الحالة — قادم / جاري / منتهي */
@Composable
fun FomcStatusChip(status: FomcStatus) {
    val (labelRes, color) = when (status) {
        FomcStatus.UPCOMING    -> R.string.fomc_status_upcoming to GoldenBorder
        FomcStatus.IN_PROGRESS -> R.string.fomc_status_in_progress to GreenTrend
        FomcStatus.COMPLETED   -> R.string.fomc_status_completed to Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(labelRes),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * قرار الفائدة — الرقم مع اتجاه التغيير.
 *
 * بيرجع `null` (ما بيرسمش حاجة) لو الفائدة لسه ما صدرتش: كارت باهت
 * مكتوب فيه "—" في اجتماع قادم بيوحي إن فيه بيانات ناقصة، والصح إن
 * الحقل ما يظهرش أصلاً.
 */
@Composable
fun FomcRateBadge(event: FomcEvent, compact: Boolean = false) {
    val rate = event.federalFundsRate ?: return
    val ctx = LocalContext.current

    val directionColor = when (event.rateDirection) {
        RateDirection.HIKE -> RedTrend      // رفع الفائدة = ضغط على الذهب
        RateDirection.CUT  -> GreenTrend
        else               -> Color.White
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = FomcFormatter.rate(ctx, rate),
            color = Color.White,
            fontSize = if (compact) 16.sp else 22.sp,
            fontWeight = FontWeight.Bold
        )

        FomcFormatter.rateChange(ctx, event.rateChange)?.let { change ->
            Spacer(Modifier.width(8.dp))
            Text(
                text = change,
                color = directionColor,
                fontSize = if (compact) 13.sp else 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** صف "عنوان ← قيمة" المستخدم في شاشة التفاصيل */
@Composable
fun FomcDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFCCCCCC),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** كارت اجتماع في القايمة */
@Composable
fun FomcEventCard(
    event: FomcEvent,
    onClick: () -> Unit
) {
    val ctx = LocalContext.current
    val status = event.statusAt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = FomcFormatter.range(ctx, event.meetingStartAt, event.meetingEndAt),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            FomcStatusChip(status)
        }

        // موعد القرار — أهم سطر للمستخدم، فبيظهر في الكارت نفسه
        event.decisionAt?.let {
            Text(
                text = stringResource(
                    R.string.fomc_decision_at_value,
                    FomcFormatter.dateTime(ctx, it)
                ),
                color = Color(0xFFCCCCCC),
                fontSize = 13.sp
            )
        }

        if (status == FomcStatus.COMPLETED) {
            FomcRateBadge(event, compact = true)
        }

        if (event.hasProjections) {
            Text(
                text = stringResource(R.string.fomc_has_projections),
                color = GoldenBorder.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * العدّاد التنازلي — أرقام كبيرة تحت بعضها.
 *
 * بيختفي تماماً لو الوقت عدّى (`seconds == null`) بدل ما يعرض صفر أو
 * رقم بالسالب.
 */
@Composable
fun FomcCountdown(seconds: Long?, big: Boolean = true) {
    val ctx = LocalContext.current
    val text = FomcFormatter.countdown(ctx, seconds) ?: return

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.fomc_time_remaining),
            color = Color(0xFFCCCCCC),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = text,
            color = GoldenBorder,
            fontSize = if (big) 26.sp else 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** حالة فشل التحميل مع زرار إعادة محاولة */
@Composable
fun FomcErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.fomc_load_failed),
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
