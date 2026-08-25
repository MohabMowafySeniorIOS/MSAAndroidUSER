package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 1:1 port of iOS `MetalType` struct from bullionsScreenView.swift.
 * Document IDs in the `Billions` collection map to:
 *   • "Karat21" → uses gold base 21 price as-is
 *   • "Karat24" → gold price × 24/21
 *   • "Silver"  → uses silver base 800 price
 */
@Parcelize
data class BullionMetalType(
    val id: String,
    val name: String,
    val type: String
) : Parcelable

/**
 * 1:1 port of iOS `grameModel`. One product/gram entry from the `gram` array
 * on a manufacturer document. Field names preserve iOS spelling:
 *   name_ar, name_en, count, cashBack, Manufacturing
 */
@Parcelize
data class BullionGramItem(
    val name: String,
    val count: Double,
    val cashBack: Double,
    val manufacturing: Double,
    val manufacturingPrice: Double      // = Manufacturing × count
) : Parcelable
