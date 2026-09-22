package com.msa.android.domain.model

import android.R
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** iOS Trend enum mirror */
enum class Trend { UP, DOWN, SAME;
    companion object {
        fun fromString(s: String?): Trend = when (s?.lowercase()) {
            "up" -> UP
            "down" -> DOWN
            else -> SAME
        }
    }
}

/**
 * Mirrors iOS BankRate model.
 * Used in أسعار العملات (Dollar prices on banks).
 */
@Parcelize
data class BankRate(
    val id: String,
    val name: String,
    val buy: String,
    val sell: String,
    val logo: String,
    val date: String,
    val trend: Trend,
    val bankUrl: String,
    val currency: String = "USD",   // iOS doc field; defaults to USD for backwards compat
    /** millis from the Firestore `bankUpdatedAt` Timestamp, if present — used to
     *  order/select banks by recency (see BanksRepositoryImpl). */
    val updatedAtMillis: Long? = null
) : Parcelable
