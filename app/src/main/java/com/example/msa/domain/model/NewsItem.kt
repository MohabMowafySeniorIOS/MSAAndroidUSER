package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Mirrors iOS NewsItem struct exactly:
 *   struct NewsItem: Codable {
 *       let title: String?
 *       let description: String?
 *       let url: String?
 *       let urlToImage: String?
 *       let publishedAt: String?
 *       let source: Source?
 *   }
 *
 * Source collection: `news` (ordered by publishedAt desc).
 */
@Parcelize
data class NewsItem(
    val id: String = "",
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,    // ISO8601 string from Firestore
    val sourceName: String? = null
) : Parcelable
