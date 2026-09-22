package com.msa.android.domain.model

/**
 * فرع من فروع الشركة.
 *
 * الاسم والعنوان بيتخزّنوا باللغتين، و[name]/[address] بيختاروا حسب
 * لغة الواجهة وقت العرض — فتبديل اللغة مبيحتاجش طلب شبكة جديد.
 */
data class Branch(
    val id: Long,

    val nameAr: String,
    val nameEn: String,
    val addressAr: String?,
    val addressEn: String?,
    val cityAr: String?,
    val cityEn: String?,

    val latitude: Double?,
    val longitude: Double?,

    val phone: String?,
    val whatsapp: String?,

    val workingHoursAr: String?,
    val workingHoursEn: String?,

    val imageUrl: String?,
    val directionsUrl: String?,

    /** المسافة بالكيلومتر — `null` لو التطبيق ما بعتش موقع المستخدم */
    val distanceKm: Double?
) {
    fun name(arabic: Boolean): String =
        (if (arabic) nameAr else nameEn).ifBlank { if (arabic) nameEn else nameAr }

    fun address(arabic: Boolean): String? =
        (if (arabic) addressAr else addressEn)?.takeIf { it.isNotBlank() }
            ?: (if (arabic) addressEn else addressAr)?.takeIf { it.isNotBlank() }

    fun city(arabic: Boolean): String? =
        (if (arabic) cityAr else cityEn)?.takeIf { it.isNotBlank() }
            ?: (if (arabic) cityEn else cityAr)?.takeIf { it.isNotBlank() }

    fun workingHours(arabic: Boolean): String? =
        (if (arabic) workingHoursAr else workingHoursEn)?.takeIf { it.isNotBlank() }

    /** الفرع اللي مالوش إحداثيات مش بيتعرضله مسافة ولا خريطة */
    val hasLocation: Boolean get() = latitude != null && longitude != null
}
