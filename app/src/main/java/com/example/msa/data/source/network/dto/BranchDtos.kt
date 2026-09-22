package com.msa.android.data.source.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * فرع من `/api/v1/branches`.
 *
 * الاسم والعنوان بيرجعوا **باللغتين** — نفس نمط `/content` و
 * `/bullions`. تبديل لغة التطبيق مبيحتاجش طلب شبكة جديد.
 *
 * `distance_km` بيرجع بس لو الطلب بعت إحداثيات المستخدم؛ الترتيب
 * بالمسافة بيحصل في السيرفر عشان منطق واحد مشترك بين iOS وأندرويد.
 */
@JsonClass(generateAdapter = true)
data class BranchDto(
    val id: Long,

    @Json(name = "name_ar") val nameAr: String? = null,
    @Json(name = "name_en") val nameEn: String? = null,
    @Json(name = "address_ar") val addressAr: String? = null,
    @Json(name = "address_en") val addressEn: String? = null,
    @Json(name = "city_ar") val cityAr: String? = null,
    @Json(name = "city_en") val cityEn: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,

    val phone: String? = null,
    val whatsapp: String? = null,

    @Json(name = "working_hours_ar") val workingHoursAr: String? = null,
    @Json(name = "working_hours_en") val workingHoursEn: String? = null,

    @Json(name = "image_url")      val imageUrl: String? = null,
    @Json(name = "directions_url") val directionsUrl: String? = null,

    @Json(name = "distance_km") val distanceKm: Double? = null,

    @Json(name = "sort_order") val sortOrder: Int = 0
)

/** `/branches/nearest` بيرجّع `data: null` لو مفيش فروع */
@JsonClass(generateAdapter = true)
data class BranchNearestEnvelope(
    val success: Boolean = true,
    val data: BranchDto? = null
)
