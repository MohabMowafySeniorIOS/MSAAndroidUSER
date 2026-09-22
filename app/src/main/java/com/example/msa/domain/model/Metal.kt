package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Mirrors iOS:
 * struct MetalModel: Codable {
 *   var type: String?
 *   var name: String?
 *   var buyPrice: String?
 *   var salePrice: String?
 *   var updatedAt: Timestamp?
 * }
 */
@Parcelize
data class Metal(
    val type: String? = null,        // "gold" / "silver"
    val name: String? = null,
    val buyPrice: String? = null,
    val salePrice: String? = null,
    val updatedAt: Long? = null      // millis epoch
) : Parcelable

/**
 * iOS: struct OuncePrice { var goldPrice: Double; var silverPrice: Double }
 */
@Parcelize
data class OuncePrice(
    val goldPrice: Double = 0.0,
    val silverPrice: Double = 0.0
) : Parcelable

enum class MetalType { GOLD, SILVER }

/**
 * Computed value classes for HomeScreen cells.
 * iOS uses GoldCell logic — we replicate calculation paths in domain.
 */
data class GoldRowData(
    val karatLabel: String,
    val buy: String,
    val sell: String
)
