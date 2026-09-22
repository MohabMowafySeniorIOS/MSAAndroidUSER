package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Mirrors iOS OnBoarding model (image, title_ar/en, description_ar/en, order)
 */
@Parcelize
data class OnBoardingPage(
    val id: String = "",
    val imageUrl: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val descAr: String = "",
    val descEn: String = "",
    val order: Int = 0
) : Parcelable
