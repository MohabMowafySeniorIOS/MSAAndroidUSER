package com.msa.android.presentation.screens.home.banner

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * موديلات بانرات الرئيسية — متظبطة على الـ response الحقيقي من
 * `https://api.msagold.com/api/v1/banners`:
 *
 * ```json
 * {
 *   "meta": { "current_page": 1, "per_page": 15, "total": 1, ... },
 *   "data": [
 *     {
 *       "id": 4,
 *       "name": "MSA ",
 *       "media_type": "image",
 *       "media_url":  "https://api.msagold.com/storage/14/01M18....jpeg",
 *       "thumb_url":  "https://api.msagold.com/storage/14/conversions/01M18...-thumb.jpg",
 *       "order": 0,
 *       "starts_at": null,
 *       "ends_at": null,
 *       "created_at": "2026-08-29T14:30:46.000000Z",
 *       "updated_at": "2026-08-29T14:30:46.000000Z",
 *       "link":   { "type": "none",   "url": null },
 *       "status": { "value": "active", "label": { "ar": "نشط", "en": "Active" } }
 *     }
 *   ],
 *   "links": { "first": "...", "last": "...", "next": null, "prev": null }
 * }
 * ```
 *
 * كل حقول الـ DTO nullable — الباك اند ممكن يبعت حقل ناقص وGson بيحط
 * null حتى لو النوع مش optional في Kotlin، وده بيعمل كراش صعب تلاقيه.
 * بنستقبل كله nullable وبنحوّله لموديل نضيف (`Banner`) في mapper واحد.
 */

// ─────────────────────────────────────────────────────────────
// Response
// ─────────────────────────────────────────────────────────────

data class BannersResponseDto(
    @SerializedName("data") val data: List<BannerDto>? = null,
    @SerializedName("meta") val meta: BannerMetaDto? = null,
    @SerializedName("links") val links: BannerPageLinksDto? = null
)

/**
 * الـ pagination. الافتراضي `per_page = 15`، وإحنا بنطلب ٥٠ من الـ API
 * عشان كل البانرات تيجي في صفحة واحدة (شوف `BannerApi.kt`).
 * لو عدد البانرات عدّى ده فعلاً، هتحتاج تجيب الصفحات الباقية من `links.next`.
 */
data class BannerMetaDto(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    @SerializedName("total") val total: Int? = null
)

data class BannerPageLinksDto(
    @SerializedName("first") val first: String? = null,
    @SerializedName("last") val last: String? = null,
    @SerializedName("next") val next: String? = null,
    @SerializedName("prev") val prev: String? = null
)

// ─────────────────────────────────────────────────────────────
// Banner DTO
// ─────────────────────────────────────────────────────────────

data class BannerDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("media_type") val mediaType: String? = null,
    @SerializedName("media_url") val mediaUrl: String? = null,
    @SerializedName("thumb_url") val thumbUrl: String? = null,
    @SerializedName("order") val order: Int? = null,
    @SerializedName("starts_at") val startsAt: String? = null,
    @SerializedName("ends_at") val endsAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("link") val link: BannerLinkDto? = null,
    @SerializedName("status") val status: BannerStatusDto? = null
)

data class BannerLinkDto(
    /** "none" لما البانر مالوش لينك، أو "url" لما يكون عنده. */
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null
)

data class BannerStatusDto(
    /** "active" / "inactive" */
    @SerializedName("value") val value: String? = null,
    @SerializedName("label") val label: BannerStatusLabelDto? = null
)

data class BannerStatusLabelDto(
    @SerializedName("ar") val ar: String? = null,
    @SerializedName("en") val en: String? = null
)

// ─────────────────────────────────────────────────────────────
// Domain model
// ─────────────────────────────────────────────────────────────

enum class BannerMediaType { IMAGE, VIDEO }

/**
 * البانر بعد التنضيف. أي عنصر مبيعديش الـ mapping (مفيش `id` أو
 * `media_url`) بيتشال خالص بدل ما يبان كخانة سودا في السلايدر.
 */
data class Banner(
    val id: Int,
    val name: String,
    val mediaType: BannerMediaType,
    val mediaUrl: String,
    val thumbUrl: String?,
    val order: Int,
    /** null لو `link.type == "none"` أو الـ url فاضي. */
    val linkUrl: String?,
    /** "active" / "inactive" / null لو الباك اند مبعتش status. */
    val statusValue: String?,
    /** epoch ms، أو null يعني "من غير تاريخ بداية". */
    val startsAt: Long?,
    /** epoch ms، أو null يعني "مفيش نهاية". */
    val endsAt: Long?
)

// ─────────────────────────────────────────────────────────────
// Mapping
// ─────────────────────────────────────────────────────────────

fun BannerDto.toDomainOrNull(): Banner? {

    val safeId = id ?: return null
    val safeUrl = mediaUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    val type = when (mediaType?.lowercase(Locale.US)) {
        "video" -> BannerMediaType.VIDEO
        else -> BannerMediaType.IMAGE
    }

    // الباك اند بيبعت "none" لما مفيش لينك — بنعاملها كأنها null عشان
    // زرار "اعرف أكثر" في الـ popup ما يظهرش على الفاضي.
    val resolvedLink = link
        ?.takeIf { !it.type.equals("none", ignoreCase = true) }
        ?.url
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    return Banner(
        id = safeId,
        // الاسم جاي من الداشبورد وساعات بيبقى فيه مسافات زايدة ("MSA ").
        name = name?.trim().orEmpty(),
        mediaType = type,
        mediaUrl = safeUrl,
        thumbUrl = thumbUrl?.trim()?.takeIf { it.isNotEmpty() },
        order = order ?: 0,
        linkUrl = resolvedLink,
        statusValue = status?.value?.trim()?.lowercase(Locale.US),
        startsAt = parseApiDate(startsAt),
        endsAt = parseApiDate(endsAt)
    )
}

fun List<BannerDto>.toDomain(): List<Banner> =
    mapNotNull { it.toDomainOrNull() }.sortedBy { it.order }

/**
 * فلترة محلية: بنعرض البانر بس لو حالته active والوقت الحالي جوه
 * الفترة بتاعته.
 *
 * الـ API على الأغلب بيفلتر بالفعل، بس الفلترة هنا كمان بتحمينا من
 * إن حد يوقّف بانر من الداشبورد أو فترته تخلص والتطبيق لسه مفتوح.
 *
 * أي حقل ناقص = مبنفلترش بيه، عشان بانر بحقول ناقصة ما يختفيش بالغلط.
 */
fun List<Banner>.activeNow(now: Long = System.currentTimeMillis()): List<Banner> =
    filter { banner ->
        val statusOk = banner.statusValue == null || banner.statusValue == "active"
        val startedOk = banner.startsAt == null || banner.startsAt <= now
        val notEndedOk = banner.endsAt == null || banner.endsAt >= now
        statusOk && startedOk && notEndedOk
    }

/**
 * التواريخ جاية بصيغة ISO-8601 بالميكروثانية و UTC:
 * `2026-08-29T14:30:46.000000Z`
 *
 * بنستخدم SimpleDateFormat مش java.time عشان تشتغل على أي minSdk من
 * غير ما تحتاج desugaring. بنقص الكسور العشرية الأول لأن SimpleDateFormat
 * مبتفهمش ٦ خانات.
 */
private fun parseApiDate(raw: String?): Long? {

    val value = raw?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", true) }
        ?: return null

    return try {
        val cleaned = value
            .substringBefore('.')        // نشيل .000000
            .removeSuffix("Z")
            .replace(' ', 'T')           // لو الباك اند بعت "yyyy-MM-dd HH:mm:ss"

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        format.parse(cleaned)?.time

    } catch (t: Throwable) {
        // تاريخ بصيغة غريبة = بنعتبره "من غير تاريخ" بدل ما نخفي البانر.
        null
    }
}
