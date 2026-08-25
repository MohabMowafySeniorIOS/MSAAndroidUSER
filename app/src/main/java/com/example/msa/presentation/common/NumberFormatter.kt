package com.msa.android.presentation.common

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Number formatting matching iOS app (always 2 decimals or 3 for some screens). */
object NumberFormatter {
    private val twoDecimalFmt = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    private val threeDecimalFmt = DecimalFormat("#,##0.000", DecimalFormatSymbols(Locale.US))
    private val intFmt = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

    fun two(v: Double): String = if (v.isFinite()) twoDecimalFmt.format(v) else "inf"
    fun three(v: Double): String = if (v.isFinite()) threeDecimalFmt.format(v) else "inf"
    fun int(v: Double): String = if (v.isFinite()) intFmt.format(v) else "inf"

    fun parse(s: String): Double = s.replace(",", "").trim().toDoubleOrNull() ?: 0.0
}
