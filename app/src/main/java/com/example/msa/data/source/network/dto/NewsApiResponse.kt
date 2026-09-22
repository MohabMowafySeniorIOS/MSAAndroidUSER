package com.msa.android.data.source.network.dto

import com.squareup.moshi.JsonClass

/**
 * REST response from `https://msagold.com/api/v1/news` — straight port of the
 * iOS `NewsModel`:
 *
 *     struct NewsModel: Codable {
 *         let data:  [NewsData]?
 *         let links: Links?
 *         let meta:  Meta?
 *     }
 *
 * Field names match the wire format exactly (snake_case), so no `@Json`
 * annotations needed; Moshi infers them from the property names below.
 */
@JsonClass(generateAdapter = true)
data class NewsApiResponse(
    val data: List<NewsDataDto>? = null,
    val links: LinksDto? = null,
    val meta: MetaDto? = null
)

/**
 * Single news entry from the API — corresponds to iOS `NewsData`. The actual
 * `title` and `description` are bilingual objects (`{ar: …, en: …}`) and the
 * map() helper picks one based on the current app language, exactly like iOS.
 */
@JsonClass(generateAdapter = true)
data class NewsDataDto(
    val id: Int? = null,
    val title: LocalizedTextDto? = null,
    val description: LocalizedTextDto? = null,
    val media_url: String? = null,
    val thumb_url: String? = null,
    val media_type: String? = null,
    val status: StatusDto? = null,
    val starts_at: String? = null,
    val ends_at: String? = null,
    val order: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

/** `{ "ar": "نص عربي", "en": "English text" }` — used for title + description. */
@JsonClass(generateAdapter = true)
data class LocalizedTextDto(
    val ar: String? = null,
    val en: String? = null
) {
    /** Picks the right locale; falls back to the other one if missing. */
    fun forLanguage(lang: String): String? = when (lang) {
        "ar" -> ar ?: en
        else -> en ?: ar
    }
}

@JsonClass(generateAdapter = true)
data class StatusDto(
    val value: String? = null,
    val label: LocalizedTextDto? = null
)

@JsonClass(generateAdapter = true)
data class LinksDto(
    val url: String? = null,
    val label: String? = null,
    val page: Int? = null,
    val active: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class MetaDto(
    val current_page: Int? = null,
    val from: Int? = null,
    val last_page: Int? = null,
    val links: List<LinksDto>? = null,
    val path: String? = null,
    val per_page: Int? = null,
    val to: Int? = null,
    val total: Int? = null
)
