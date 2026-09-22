package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Mirrors iOS pages collection (Privacy, Terms, About, Refund).
 *
 * IMPORTANT — Field names match iOS verbatim (typo preserved):
 *   • title_ar / title_en
 *   • describtion_ar / describtion_en   ← typo from iOS, NOT "description"
 *
 * iOS source (AboutUsVC.swift):
 *   let title = ... ? data["title_ar"] : data["title_en"]
 *   let describtion = ... ? data["describtion_ar"] : data["describtion_en"]
 */
@Parcelize
data class PageContent(
    val id: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val contentAr: String = "",
    val contentEn: String = ""
) : Parcelable

/**
 * Document IDs match iOS ValideKey enum exactly:
 *   case .terms_of_use:   "terms"
 *   case .AboutUs:        "about"
 *   case .Privacy_policy: "privacy"
 *   case .Refund:         "refund"
 */
enum class PageType(val docId: String) {
    ABOUT_US("about"),
    PRIVACY("privacy"),
    TERMS("terms"),
    REFUND("refund")
}
