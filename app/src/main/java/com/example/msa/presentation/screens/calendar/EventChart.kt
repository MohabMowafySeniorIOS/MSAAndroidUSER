package com.msa.android.presentation.screens.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.domain.model.EventPoint
import com.msa.android.presentation.common.CalendarFormatter
import com.msa.android.presentation.theme.GoldenBorder
import kotlin.math.abs

/**
 * رسم سلسلة المؤشر.
 *
 * ## ليه Canvas بدل مكتبة رسم؟
 *
 * الرسم ده خط واحد وأعمدة بسيطة. أي مكتبة رسم بتضيف ميجا+ للـ APK
 * ونمط ألوان مالوش علاقة بالتطبيق، وهنقضي وقت نطفّي منه حاجات أكتر
 * من وقت رسمه بإيدينا.
 *
 * ## الاتجاه
 *
 * التطبيق عربي (RTL) بس **الزمن بيمشي من الشمال لليمين في أي لغة** —
 * ده عرف عالمي في الرسوم البيانية. عشان كده بنعكس المحور يدوياً في
 * RTL: النقطة الأقدم على الشمال والأحدث على اليمين زي ما المستخدم
 * متوقّع من أي شارت.
 *
 * ## المدى الرأسي
 *
 * بيتحسب من القيم نفسها مش من صفر. مؤشر بيتحرك بين 4.1% و4.3% لو
 * رسمناه من صفر بيبقى خط مستقيم — والحركة اللي المستخدم فاتح الشاشة
 * عشانها بتختفي. فيه هامش ١٠٪ فوق وتحت عشان النقاط ما تلزقش بالحواف.
 */
@Composable
fun EventChart(
    points: List<EventPoint>,
    unit: String?,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    // نقطة واحدة مش رسم — خط من غير طول مالوش معنى
    if (points.size < 2) return

    val values = points.map { it.value }
    val rawMin = values.min()
    val rawMax = values.max()

    /*
     * سلسلة كل قيمها واحدة (مؤشر ثابت زي سعر فائدة ما اتغيّرش).
     *
     * `max - min = 0` معناها قسمة على صفر. بنفتح مدى صناعي حوالين
     * القيمة عشان الخط يظهر في النص بدل ما يختفي أو يعمل NaN.
     */
    val span = (rawMax - rawMin).takeIf { it > 0.0 }
        ?: (abs(rawMax).takeIf { it > 0.0 }?.times(0.1) ?: 1.0)

    val padding = span * 0.1
    val min = rawMin - padding
    val max = rawMax + padding
    val range = (max - min).takeIf { it > 0.0 } ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {

        // القيمة الأعلى والأدنى — بديل محور رأسي كامل، وأوضح منه
        // على عرض الموبايل
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = format(rawMax, unit),
                color = Color(0xFF888888),
                fontSize = 10.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(vertical = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(144.dp)) {

                val w = size.width
                val h = size.height

                fun xOf(index: Int): Float {
                    val t = index.toFloat() / (points.size - 1).toFloat()
                    // الزمن من الشمال لليمين حتى في RTL
                    return if (rtl) w - t * w else t * w
                }

                fun yOf(value: Double): Float =
                    (h - ((value - min) / range * h)).toFloat()

                // خطوط شبكة أفقية — ٣ خطوط كفاية للقراءة من غير زحمة
                repeat(3) { i ->
                    val y = h * (i + 1) / 4f
                    drawLine(
                        color = Color.White.copy(alpha = 0.07f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // خط القيم
                val path = Path()

                points.forEachIndexed { i, p ->
                    val x = xOf(i)
                    val y = yOf(p.value)

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = GoldenBorder,
                    style = Stroke(width = 2.5f)
                )

                // نقطة على كل إصدار — بتخلّي المستخدم يعدّ الإصدارات
                points.forEachIndexed { i, p ->
                    drawCircle(
                        color = GoldenBorder,
                        radius = 3.5f,
                        center = Offset(xOf(i), yOf(p.value))
                    )
                }

                /*
                 * آخر نقطة أكبر وبلون مميّز.
                 *
                 * دي القيمة اللي نزلت آخر مرة — أهم رقم على الرسم،
                 * ومن غير التمييز ده المستخدم لازم يتتبّع الخط عشان
                 * يلاقيها.
                 */
                points.lastOrNull()?.let { last ->
                    val x = xOf(points.lastIndex)
                    val y = yOf(last.value)

                    drawCircle(color = Color.White, radius = 5.5f, center = Offset(x, y))
                    drawCircle(color = GoldenBorder, radius = 3f, center = Offset(x, y))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = format(rawMin, unit),
                color = Color(0xFF888888),
                fontSize = 10.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(4.dp))

        // محور التواريخ — الأول والآخر بس. ٢٤ تاريخ على عرض الموبايل
        // بيبقوا خط أسود مش معلومة.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val first = CalendarFormatter.chartLabel(ctx, points.first().occursAt)
            val last = CalendarFormatter.chartLabel(ctx, points.last().occursAt)

            // في RTL الصف بيتقلب لوحده، فبنقلب النصين عشان الأقدم
            // يفضل على الشمال زي الرسم
            Text(
                text = if (rtl) last else first,
                color = Color(0xFF888888),
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (rtl) first else last,
                color = Color(0xFF888888),
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        // آخر قيمة بالنص الأصلي — الرسم بيدي الاتجاه، والرقم ده
        // بيدي الدقة
        points.lastOrNull()?.label?.let { label ->
            Text(
                text = label,
                color = GoldenBorder,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * رقم المحور.
 *
 * بنقصّه لخانتين عشريتين: المؤشرات الاقتصادية بتتنشر بخانة أو اتنين،
 * وعرض `4.099999999` على المحور بيبان كأنه عطل.
 */
private fun format(value: Double, unit: String?): String {
    val rounded = if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }

    return rounded + (unit ?: "")
}
