package com.msa.android.data.source.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * التقويم الاقتصادي — الرد جاي من مسارات `/api/v1/calendar/`.
 *
 * كل الأوقات UTC بصيغة ISO-8601 (`2026-09-16T12:30:00Z`) والتطبيق هو
 * اللي بيعرضها بتوقيت الجهاز. مفيش أي نص واجهة في الرد: `impact`
 * بيرجع كمفتاح (`high`) واللي بيترجمه هو `strings.xml`.
 *
 * ## ليه القيمة مرتين؟
 *
 * `actual` نص زي ما صدر («$146.3B»، «0.1%») و`actual_value` رقم
 * (146.3). الصف بيعرض النص، والرسم البياني بيرسم الرقم. لو كان فيه
 * واحد بس، يا العرض هيبقى غلط يا الرسم مستحيل.
 *
 * ## `null` معناها «لسه ما صدرش» مش صفر
 *
 * صفر في «معدل التضخم» خبر ضخم، وفاضي معناه الرقم لسه ما نزلش.
 * عشان كده كل الأرقام هنا nullable والشاشة بتعرض «—».
 */

@JsonClass(generateAdapter = true)
data class CalendarEventDto(
    val id: Long,
    val title: String? = null,

    /** مفتاح المؤشر — ثابت عبر كل إصداراته، وبيستخدم في طلب التاريخ */
    @Json(name = "event_key") val eventKey: String? = null,

    val currency: String? = null,
    /** حرفين ISO — التطبيق بيحوّلهم لعلم */
    @Json(name = "country_code") val countryCode: String? = null,

    @Json(name = "occurs_at") val occursAt: String? = null,
    /** مفيش ساعة محددة («All Day» / «Tentative») */
    @Json(name = "all_day") val allDay: Boolean = false,

    /** high / medium / low / holiday */
    val impact: String? = null,

    val actual: String? = null,
    val forecast: String? = null,
    val previous: String? = null,

    @Json(name = "actual_value")   val actualValue: Double? = null,
    @Json(name = "forecast_value") val forecastValue: Double? = null,
    @Json(name = "previous_value") val previousValue: Double? = null,

    val unit: String? = null,
    val category: String? = null,
    val description: String? = null,

    @Json(name = "source_url") val sourceUrl: String? = null
)

/**
 * يوم واحد وأحداثه.
 *
 * السيرفر هو اللي بيجمّع مش التطبيق. التجميع في التطبيق كان هيقطع
 * اليوم في نص الصفحة ويخلّي عنوان اليوم يظهر مرتين.
 */
@JsonClass(generateAdapter = true)
data class CalendarDayDto(
    val date: String? = null,
    val events: List<CalendarEventDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CalendarEventsDto(
    val from: String? = null,
    val to: String? = null,
    val total: Int = 0,
    val days: List<CalendarDayDto> = emptyList()
)

/** `/calendar/events/{id}` — الحدث + الإصدار الجاي لنفس المؤشر */
@JsonClass(generateAdapter = true)
data class CalendarDetailsDto(
    val event: CalendarEventDto,
    /** null لو مفيش إصدار جاي معلن */
    val next: CalendarEventDto? = null
)

/** نقطة على الرسم البياني */
@JsonClass(generateAdapter = true)
data class CalendarPointDto(
    @Json(name = "occurs_at") val occursAt: String? = null,
    val actual: String? = null,
    val value: Double = 0.0,
    val forecast: Double? = null
)

@JsonClass(generateAdapter = true)
data class CalendarHistoryDto(
    @Json(name = "event_key") val eventKey: String? = null,
    val title: String? = null,
    val unit: String? = null,

    /** الإصدارات اللي ليها رقم فعلي بس — دي اللي بتترسم */
    val points: List<CalendarPointDto> = emptyList(),

    /** كل الإصدارات بما فيها اللي لسه ما صدرتش — لتبويب «التاريخ» */
    val releases: List<CalendarEventDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CalendarCurrencyDto(
    val code: String? = null,
    @Json(name = "country_code") val countryCode: String? = null
)

@JsonClass(generateAdapter = true)
data class CalendarFiltersDto(
    val currencies: List<CalendarCurrencyDto> = emptyList(),
    val impacts: List<String> = emptyList()
)
