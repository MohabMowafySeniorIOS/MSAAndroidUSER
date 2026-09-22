package com.msa.android.domain.model

import java.time.Instant
import java.time.LocalDate

/** أهمية الحدث — بتحدد لون الشارة وترتيب الأحداث في نفس اللحظة */
enum class EventImpact(val key: String) {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low"),
    HOLIDAY("holiday");

    companion object {
        /** أي قيمة مش معروفة بترجع LOW بدل ما تكسر الشاشة */
        fun from(value: String?): EventImpact =
            entries.firstOrNull { it.key.equals(value, ignoreCase = true) } ?: LOW
    }
}

/**
 * حدث واحد في التقويم الاقتصادي.
 *
 * ## القيمة نص ورقم
 *
 * [actual] نص زي ما صدر («$146.3B») و[actualValue] رقم (146.3).
 * الصف بيعرض النص عشان الرمز والوحدة يبانوا، والرسم البياني بيرسم
 * الرقم. `null` في الاتنين معناها **لسه ما صدرش** — مش صفر.
 */
data class EconomicEvent(
    val id: Long,
    val title: String,
    val eventKey: String?,

    val currency: String?,
    /** حرفين ISO — [com.msa.android.presentation.common.flagEmoji] بيحوّلهم لعلم */
    val countryCode: String?,

    val occursAt: Instant,
    /** مفيش ساعة محددة — الشاشة بتعرض «طول اليوم» مش ١٢:٠٠ ص */
    val allDay: Boolean,

    val impact: EventImpact,

    val actual: String?,
    val forecast: String?,
    val previous: String?,

    val actualValue: Double?,
    val forecastValue: Double?,
    val previousValue: Double?,

    val unit: String?,
    val category: String?,
    val description: String?
) {
    /** صدر ولا لسه؟ — بيحدد لون الرقم وشكل الصف */
    val isReleased: Boolean get() = actual != null

    /**
     * الفعلي أحسن ولا أوحش من المتوقّع؟
     *
     * بيرجع `null` لو واحد منهم ناقص — ولون محايد أصدق من أخضر
     * على مقارنة ما حصلتش.
     *
     * ⚠️ «أعلى» مش دايماً «أحسن»: تضخم أعلى من المتوقّع خبر وحش.
     * عشان كده الاسم `isAboveForecast` مش `isGood` — الشاشة بتعرض
     * الاتجاه والمستخدم بيفسّره.
     */
    val isAboveForecast: Boolean?
        get() {
            val a = actualValue ?: return null
            val f = forecastValue ?: return null
            return if (a == f) null else a > f
        }
}

/** يوم وأحداثه — التجميع جاي من السيرفر */
data class CalendarDay(
    val date: LocalDate,
    val events: List<EconomicEvent>
)

/** نقطة على الرسم البياني */
data class EventPoint(
    val occursAt: Instant,
    val label: String?,
    val value: Double,
    val forecast: Double?
)

/** سلسلة المؤشر — الرسم البياني وتبويب التاريخ */
data class EventHistory(
    val eventKey: String?,
    val title: String?,
    val unit: String?,
    val points: List<EventPoint>,
    val releases: List<EconomicEvent>
)

/** الحدث + الإصدار الجاي لنفس المؤشر */
data class EventDetails(
    val event: EconomicEvent,
    val next: EconomicEvent?
)

/** عملة في شاشة الفلتر */
data class CalendarCurrency(
    val code: String,
    val countryCode: String?
)
