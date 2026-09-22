package com.msa.android.data.source.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * اجتماعات الفيدرالي — الرد جاي من مسارات `/api/v1/fomc/`.
 *
 * كل الأوقات بترجع UTC بصيغة ISO-8601 (`2026-09-16T18:00:00Z`) والتطبيق
 * هو اللي بيعرضها بتوقيت الجهاز. مفيش أي نص واجهة في الرد — `status`
 * بيرجع كمفتاح (`upcoming`) واللي بيترجمه هو `strings.xml`.
 *
 * كل الحقول nullable بقصد: السيرفر بيرجّع الموعد أول ما يتعلن، والفائدة
 * والبيان والمحضر بيتملوا بعد الاجتماع. لو خلّيناها مطلوبة كان أول
 * اجتماع قادم هيكسّر الـ parser.
 */
@JsonClass(generateAdapter = true)
data class FomcEventDto(
    val id: Long,
    @Json(name = "external_id") val externalId: String? = null,
    val title: String? = null,

    @Json(name = "meeting_start_at")    val meetingStartAt: String? = null,
    @Json(name = "meeting_end_at")      val meetingEndAt: String? = null,
    @Json(name = "decision_at")         val decisionAt: String? = null,
    @Json(name = "press_conference_at") val pressConferenceAt: String? = null,
    @Json(name = "minutes_at")          val minutesAt: String? = null,

    @Json(name = "federal_funds_rate") val federalFundsRate: Double? = null,
    @Json(name = "rate_lower_bound")   val rateLowerBound: Double? = null,
    @Json(name = "rate_upper_bound")   val rateUpperBound: Double? = null,
    @Json(name = "previous_rate")      val previousRate: Double? = null,
    @Json(name = "rate_change")        val rateChange: Double? = null,

    val status: String? = null,
    @Json(name = "has_press_conference") val hasPressConference: Boolean = false,
    @Json(name = "has_projections")      val hasProjections: Boolean = false,

    @Json(name = "statement_url") val statementUrl: String? = null,
    @Json(name = "minutes_url")   val minutesUrl: String? = null,
    @Json(name = "source_url")    val sourceUrl: String? = null,

    /** التوقيت الرسمي للحدث (America/New_York) — للعرض التوضيحي بس */
    val timezone: String? = null
)

/**
 * `/fomc/next` بيرجّع `data: null` لو مفيش اجتماع قادم في القاعدة.
 * [DataEnvelope] بتاعتنا مش nullable، فمحتاجين غلاف منفصل هنا وإلا
 * Moshi هيرمي استثناء بدل ما يدّينا "مفيش اجتماع".
 */
@JsonClass(generateAdapter = true)
data class FomcNextEnvelope(
    val success: Boolean = true,
    val data: FomcEventDto? = null
)
