package com.msa.android.presentation.common

import android.content.Context
import com.msa.android.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * عرض أوقات اجتماعات الفيدرالي.
 *
 * القاعدة الأساسية هنا: **مفيش أي منطقة زمنية ثابتة في الواجهة**.
 * السيرفر بيرجّع UTC، وإحنا بنحوّل لـ [ZoneId.systemDefault] — يعني
 * توقيت جهاز المستخدم. المستخدم في القاهرة بيشوف 8 م والمستخدم في لندن
 * بيشوف 6 م لنفس اللحظة، وده المطلوب.
 *
 * التنسيق بيستخدم لغة الواجهة الحالية (من `context.resources`) مش
 * `Locale.getDefault()` — عشان لو المستخدم مغيّر لغة التطبيق لعربي
 * والجهاز إنجليزي، التاريخ يتعرض عربي زي باقي الشاشة.
 */
object FomcFormatter {

    private fun locale(context: Context): Locale =
        context.resources.configuration.locales[0]

    private fun zone(): ZoneId = ZoneId.systemDefault()

    /** مثال: ١٦ سبتمبر ٢٠٢٦ */
    fun date(context: Context, instant: Instant?): String {
        instant ?: return "—"

        return DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .withLocale(locale(context))
            .withZone(zone())
            .format(instant)
    }

    /** مثال: ١٦ سبتمبر ٢٠٢٦، ٨:٠٠ م */
    fun dateTime(context: Context, instant: Instant?): String {
        instant ?: return "—"

        return DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale(context))
            .withZone(zone())
            .format(instant)
    }

    /** الوقت بس — للسطر التاني في الكروت المضغوطة */
    fun time(context: Context, instant: Instant?): String {
        instant ?: return "—"

        return DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale(context))
            .withZone(zone())
            .format(instant)
    }

    /**
     * مدى الاجتماع: "١٥ – ١٦ سبتمبر ٢٠٢٦"، أو تاريخ واحد لو الاجتماع
     * يوم واحد.
     */
    fun range(context: Context, start: Instant?, end: Instant?): String {
        start ?: return "—"
        if (end == null) return date(context, start)

        val z = zone()
        val startDay = start.atZone(z).toLocalDate()
        val endDay = end.atZone(z).toLocalDate()

        if (startDay == endDay) return date(context, start)

        val dayOnly = DateTimeFormatter.ofPattern("d", locale(context)).withZone(z)

        // نفس الشهر → "١٥ – ١٦ سبتمبر ٢٠٢٦" بدل تكرار الشهر مرتين
        return if (startDay.month == endDay.month && startDay.year == endDay.year) {
            "${dayOnly.format(start)} – ${date(context, end)}"
        } else {
            "${date(context, start)} – ${date(context, end)}"
        }
    }

    /**
     * العدّاد التنازلي: "٣ ي ١٢ س ٤ د" — وحدات مترجمة، والأرقام
     * بتتنسّق بلغة الواجهة.
     *
     * بنعرض يوم/ساعة/دقيقة بس من غير ثواني لما يكون فاضل أكتر من ساعة:
     * عدّاد الثواني في حدث بعيد بأسابيع بيلفت النظر من غير فايدة
     * وبيخلّي الشاشة تعيد الرسم كل ثانية على الفاضي.
     */
    fun countdown(context: Context, totalSeconds: Long?): String? {
        if (totalSeconds == null || totalSeconds <= 0) return null

        val days = totalSeconds / 86_400
        val hours = (totalSeconds % 86_400) / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60

        val loc = locale(context)

        fun num(value: Long) = String.format(loc, "%d", value)

        return when {
            days > 0 -> context.getString(
                R.string.fomc_countdown_dhm, num(days), num(hours), num(minutes)
            )
            hours > 0 -> context.getString(
                R.string.fomc_countdown_hms, num(hours), num(minutes), num(seconds)
            )
            else -> context.getString(
                R.string.fomc_countdown_ms, num(minutes), num(seconds)
            )
        }
    }

    /**
     * نسبة الفائدة — "٤٫٢٥٪".
     *
     * بنقص الأصفار الزايدة: الفيدرالي بيتحرك بربع نقطة، فـ "4.25" و
     * "4.5" هما الشكلين الطبيعيين، و"4.500" بتبان غلط.
     */
    fun rate(context: Context, value: Double?): String {
        value ?: return "—"

        // NumberFormat مش String.format: بيدّي فاصلة عشرية صح حسب اللغة
        // وبيشيل الأصفار الزايدة لوحده — "4.25" و"4.5" مش "4.500".
        val formatter = java.text.NumberFormat.getNumberInstance(locale(context)).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 3
        }

        return context.getString(R.string.fomc_rate_percent, formatter.format(value))
    }

    /** نطاق الفائدة المستهدف — "٤٫٠٠٪ – ٤٫٢٥٪" */
    fun rateRange(context: Context, lower: Double?, upper: Double?): String? {
        if (lower == null || upper == null) return null

        return "${rate(context, lower)} – ${rate(context, upper)}"
    }

    /** التغيير بإشارة صريحة — "+٠٫٢٥٪" أو "−٠٫٢٥٪" */
    fun rateChange(context: Context, value: Double?): String? {
        if (value == null || kotlin.math.abs(value) < 0.0001) return null

        val sign = if (value > 0) "+" else "−"

        return sign + rate(context, kotlin.math.abs(value))
    }
}
