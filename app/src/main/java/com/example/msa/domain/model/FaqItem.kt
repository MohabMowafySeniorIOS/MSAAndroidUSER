package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Mirrors iOS FAQModel(question, answer) */
@Parcelize
data class FaqItem(
    val id: String = "",
    val question: String = "",
    val answer: String = ""
) : Parcelable
