package com.msa.android.domain.model

import java.time.Instant

/**
 * اجتماع للجنة السوق المفتوحة الفيدرالية (FOMC).
 *
 * الأوقات كلها [Instant] — يعني لحظة مطلقة من غير منطقة زمنية. التحويل
 * لتوقيت الجهاز بيحصل في طبقة العرض بس (`FomcFormatter`)، فالمستخدم في
 * القاهرة والمستخدم في لندن كل واحد بيشوف وقته الصح من غير أي تثبيت
 * لمنطقة زمنية في الـ UI.
 */
data class FomcEvent(
    val id: Long,
    val externalId: String?,
    val title: String?,

    val meetingStartAt: Instant?,
    val meetingEndAt: Instant?,
    val decisionAt: Instant?,
    val pressConferenceAt: Instant?,
    val minutesAt: Instant?,

    val federalFundsRate: Double?,
    val rateLowerBound: Double?,
    val rateUpperBound: Double?,
    val previousRate: Double?,
    val rateChange: Double?,

    val hasPressConference: Boolean,
    val hasProjections: Boolean,

    val statementUrl: String?,
    val minutesUrl: String?,
    val sourceUrl: String?,

    /** التوقيت الرسمي للحدث — بيتعرض كسطر توضيحي جنب الوقت المحلي */
    val officialTimezone: String?,

    /** الحالة زي ما السيرفر حسبها لحظة الرد — مرجع احتياطي بس */
    private val serverStatus: String?
) {
    /**
     * الحالة بتتحسب محلياً من الوقت الحالي، مش من رد السيرفر.
     *
     * السبب: الرد ممكن يكون متخزّن في الكاش من ساعة، فلو اعتمدنا على
     * `serverStatus` كان ممكن اجتماع خلص من ساعة يفضل ظاهر "قادم"
     * والعدّاد التنازلي يمشي بالسالب. رد السيرفر بيتستخدم بس لو
     * المواعيد نفسها ناقصة.
     */
    fun statusAt(now: Instant = Instant.now()): FomcStatus {
        val start = meetingStartAt ?: return FomcStatus.from(serverStatus)

        if (now.isBefore(start)) return FomcStatus.UPCOMING

        // الاجتماع يفضل "جاري" لحد ما القرار يصدر — مش لحد نهاية اليوم.
        // نفس منطق `FomcEvent::getStatusAttribute()` في لارافيل.
        val closesAt = decisionAt ?: meetingEndAt ?: start

        return if (now.isAfter(closesAt)) FomcStatus.COMPLETED else FomcStatus.IN_PROGRESS
    }

    /**
     * الوقت اللي العدّاد التنازلي بيعدّ ناحيته: لحظة صدور القرار لو
     * معروفة، وإلا بداية الاجتماع.
     */
    val countdownTarget: Instant? get() = decisionAt ?: meetingStartAt

    /**
     * الثواني الفاضلة للعدّاد — `null` لو الحدث عدّى أو الموعد مش معروف،
     * عشان الواجهة تعرف تخفي العدّاد بدل ما تعرض رقم بالسالب.
     */
    fun secondsUntil(now: Instant = Instant.now()): Long? {
        val target = countdownTarget ?: return null
        val seconds = target.epochSecond - now.epochSecond

        return if (seconds > 0) seconds else null
    }

    /**
     * اتجاه قرار الفائدة — بيتحسب من `rateChange` لو موجود، وإلا من
     * الفرق بين السعر الحالي والسابق.
     */
    val rateDirection: RateDirection
        get() {
            val change = rateChange
                ?: (federalFundsRate ?: return RateDirection.NONE)
                    .minus(previousRate ?: return RateDirection.NONE)

            return when {
                change > 0.0001  -> RateDirection.HIKE
                change < -0.0001 -> RateDirection.CUT
                else             -> RateDirection.HOLD
            }
        }
}

enum class FomcStatus {
    UPCOMING, IN_PROGRESS, COMPLETED;

    companion object {
        fun from(raw: String?): FomcStatus = when (raw?.lowercase()) {
            "in_progress" -> IN_PROGRESS
            "completed"   -> COMPLETED
            else          -> UPCOMING
        }
    }
}

/** رفع / خفض / تثبيت — الواجهة بتلوّن وتترجم على أساسه */
enum class RateDirection { HIKE, CUT, HOLD, NONE }
