package com.msa.android.data.source.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * محتوى شاشة «المزيد» — كل رد بيرجع اللغتين مع بعض، والتطبيق بيختار
 * حسب لغة الواجهة. كده تغيير اللغة مبيحتاجش طلب جديد للشبكة.
 */

@JsonClass(generateAdapter = true)
data class PageDto(
    val slug: String,
    @Json(name = "title_ar") val titleAr: String?,
    @Json(name = "title_en") val titleEn: String?,
    @Json(name = "body_ar") val bodyAr: String?,
    @Json(name = "body_en") val bodyEn: String?
)

@JsonClass(generateAdapter = true)
data class FaqDto(
    val id: Long,
    @Json(name = "question_ar") val questionAr: String?,
    @Json(name = "question_en") val questionEn: String?,
    @Json(name = "answer_ar") val answerAr: String?,
    @Json(name = "answer_en") val answerEn: String?
)

@JsonClass(generateAdapter = true)
data class ContactDto(
    val phone: String? = null,
    val whatsapp: String? = null,
    val email: String? = null,
    @Json(name = "address_ar") val addressAr: String? = null,
    @Json(name = "address_en") val addressEn: String? = null,
    @Json(name = "working_hours_ar") val workingHoursAr: String? = null,
    @Json(name = "working_hours_en") val workingHoursEn: String? = null,
    val social: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class ContentBundleDto(
    val pages: Map<String, PageDto>? = null,
    val faqs: List<FaqDto>? = null,
    val contact: ContactDto? = null
)

/** لارافيل بيلفّ كل رد في `data` */
@JsonClass(generateAdapter = true)
data class DataEnvelope<T>(val data: T)

/* ── السبائك ────────────────────────────────────────────────────────
 * الشجرة كاملة في رد واحد: المعادن ← الشركات ← المنتجات.
 * كانت بتيجي من Firestore بثلاث استعلامات متتابعة.
 */

@JsonClass(generateAdapter = true)
data class BullionProductDto(
    val id: Long,
    @Json(name = "name_ar") val nameAr: String?,
    @Json(name = "name_en") val nameEn: String?,
    val weight: Double = 0.0,
    val manufacturing: Double = 0.0,
    @Json(name = "cash_back") val cashBack: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class BullionCompanyDto(
    val id: Long,
    val name: String,
    @Json(name = "image_url") val imageUrl: String? = null,
    val products: List<BullionProductDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BullionMetalDto(
    val slug: String,
    @Json(name = "name_ar") val nameAr: String?,
    @Json(name = "name_en") val nameEn: String?,
    val companies: List<BullionCompanyDto> = emptyList()
)

/* ── التحقّق من كود سبيكة ────────────────────────────────────────────
 * `POST bullion-codes/verify` — الرد بيرجع ٢٠٠ دايماً حتى للكود
 * المجهول، لأن ده نتيجة تحقّق مش خطأ في الطلب. `status` هو اللي
 * بيفرّق.
 */

@JsonClass(generateAdapter = true)
data class BullionVerifyRequest(
    /** النص اللي الكاميرا قرته زي ما هو — السيرفر بيطبّعه */
    @Json(name = "code") val code: String,
)

@JsonClass(generateAdapter = true)
data class BullionVerifyDto(
    /** valid | void | unknown */
    @Json(name = "status")  val status: String,
    @Json(name = "valid")   val valid: Boolean,
    @Json(name = "title")   val title: String,
    @Json(name = "message") val message: String,
    /** بيرجع للكود السليم بس — الملغي والمجهول بيرجّعوا `null` */
    @Json(name = "bullion") val bullion: BullionVerifyDetailsDto? = null,
)

@JsonClass(generateAdapter = true)
data class BullionVerifyDetailsDto(
    @Json(name = "code")          val code: String,
    @Json(name = "metal")         val metal: String? = null,
    @Json(name = "karat")         val karat: Int? = null,
    @Json(name = "weight_grams")  val weightGrams: Double? = null,
    @Json(name = "serial")        val serial: String? = null,
    @Json(name = "registered_at") val registeredAt: String? = null,
)
