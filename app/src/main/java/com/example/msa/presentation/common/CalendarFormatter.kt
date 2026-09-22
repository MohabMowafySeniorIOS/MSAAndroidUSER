package com.msa.android.presentation.common

import android.content.Context
import com.msa.android.R
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * علم الدولة من كود ISO بحرفين.
 *
 * `US` → 🇺🇸. الحروف بتتحوّل لرموز «المؤشر الإقليمي» في يونيكود،
 * والنظام بيرسمهم كعلم.
 *
 * ## ليه إيموجي مش صور؟
 *
 * ٢٥ علم × ٣ كثافات = ٧٥ ملف في الـ APK لحاجة النظام بيرسمها ببلاش،
 * وأي عملة جديدة من السيرفر كانت هتحتاج تحديث للتطبيق.
 *
 * ## ولو الجهاز مش بيرسم الأعلام؟
 *
 * بعض أجهزة أندرويد (خصوصاً القديمة وبعض الشركات الصينية) بتعرض
 * الحرفين بدل العلم. عشان كده الصف **بيعرض كود العملة جنب العلم
 * دايماً** — يعني حتى لو العلم ما ظهرش، المستخدم لسه شايف `USD`
 * ومفيش معلومة ضاعت.
 */
fun flagEmoji(countryCode: String?): String {
    val code = countryCode?.trim()?.uppercase(Locale.US) ?: return ""

    if (code.length != 2 || !code.all { it in 'A'..'Z' }) return ""

    // 0x1F1E6 هو رمز المؤشر الإقليمي للحرف A
    val base = 0x1F1E6 - 'A'.code

    return String(Character.toChars(base + code[0].code)) +
           String(Character.toChars(base + code[1].code))
}

/**
 * تنسيقات شاشة التقويم.
 *
 * كل حاجة بتتعرض **بتوقيت الجهاز**: السيرفر بيرجّع UTC، والمستخدم في
 * القاهرة المفروض يشوف الساعة اللي هيسمع فيها الخبر مش ساعة لندن.
 */
object CalendarFormatter {

    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    /** الساعة بتوقيت الجهاز، أو «طول اليوم» للأحداث اللي مالهاش ميعاد */
    fun time(ctx: Context, at: Instant, allDay: Boolean): String =
        if (allDay) ctx.getString(R.string.calendar_all_day)
        else timeFmt.format(at.atZone(ZoneId.systemDefault()))

    /** اليوم اللي الحدث ده واقع فيه — بتوقيت الجهاز */
    fun localDate(at: Instant): LocalDate =
        at.atZone(ZoneId.systemDefault()).toLocalDate()

    /**
     * عنوان اليوم: «النهاردة» / «بكرة» / «امبارح»، وإلا التاريخ كامل.
     *
     * الكلمات النسبية دي بتخلّي المستخدم يعرف مكانه من غير ما يحسب
     * التاريخ — وهي نفس اللغة اللي التبويبات فوق مكتوبة بيها.
     */
    fun dayHeader(ctx: Context, date: LocalDate): String {
        val today = LocalDate.now(ZoneId.systemDefault())

        return when (date) {
            today                -> ctx.getString(R.string.calendar_today)
            today.plusDays(1)    -> ctx.getString(R.string.calendar_tomorrow)
            today.minusDays(1)   -> ctx.getString(R.string.calendar_yesterday)
            else -> DateTimeFormatter
                .ofPattern("EEEE، d MMMM yyyy", localeOf(ctx))
                .format(date)
        }
    }

    /**
     * الوقت الفاضل للإصدار الجاي: «٣ س ١٥ د».
     *
     * بيرجّع `null` لو الميعاد عدّى — عدّاد بالسالب معناه بيانات غلط،
     * والصح إنه يختفي.
     */
    fun timeLeft(ctx: Context, target: Instant, now: Instant = Instant.now()): String? {
        val d = Duration.between(now, target)

        if (d.isNegative || d.isZero) return null

        val days = d.toDays()
        val hours = d.toHours() % 24
        val minutes = d.toMinutes() % 60

        return when {
            days > 0  -> ctx.getString(R.string.calendar_left_days, days, hours)
            hours > 0 -> ctx.getString(R.string.calendar_left_hours, hours, minutes)
            else      -> ctx.getString(R.string.calendar_left_minutes, minutes)
        }
    }

    /**
     * تاريخ مختصر لمحور الرسم البياني: «سبتمبر ٢٦».
     *
     * الشهر والسنة بس: الرسم بيعرض ٢٤ نقطة على عرض الموبايل،
     * والتاريخ الكامل مستحيل يتقرا.
     */
    fun chartLabel(ctx: Context, at: Instant): String =
        DateTimeFormatter
            .ofPattern("MMM yy", localeOf(ctx))
            .format(at.atZone(ZoneId.systemDefault()))

    /** تاريخ صف في تبويب «التاريخ» */
    fun releaseDate(ctx: Context, at: Instant): String =
        DateTimeFormatter
            .ofPattern("d MMM yyyy", localeOf(ctx))
            .format(at.atZone(ZoneId.systemDefault()))

    /**
     * لغة العرض من إعدادات التطبيق مش من النظام.
     *
     * المستخدم ممكن يكون مختار عربي جوه التطبيق وجهازه إنجليزي —
     * `ctx.resources` بيعكس اختيار التطبيق لأن التطبيق بيلف الـ
     * context بلغته.
     */
    private fun localeOf(ctx: Context): Locale =
        ctx.resources.configuration.locales[0] ?: Locale.getDefault()
}
