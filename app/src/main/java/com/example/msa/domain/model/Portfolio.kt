package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Things the user can hold in their portfolio. Mirrors the price grid the app
 * already tracks. [measuredByWeight] disambiguates UX: gold scrap/bullion is
 * entered in grams, but pounds/kilos are entered as a unit count.
 */
enum class AssetType(
    val displayAr: String,
    val displayEn: String,
    val isGold: Boolean,
    val measuredByWeight: Boolean
) {
    GOLD_24K   ("ذهب عيار 24",  "Gold 24K",    isGold = true,  measuredByWeight = true),
    GOLD_21K   ("ذهب عيار 21",  "Gold 21K",    isGold = true,  measuredByWeight = true),
    GOLD_18K   ("ذهب عيار 18",  "Gold 18K",    isGold = true,  measuredByWeight = true),
    GOLD_POUND ("جنيه ذهب",     "Gold Pound",  isGold = true,  measuredByWeight = false),
    GOLD_KILO  ("كيلو ذهب",     "Gold Kilo",   isGold = true,  measuredByWeight = false),
    SILVER_999 ("فضة عيار 999", "Silver 999",  isGold = false, measuredByWeight = true),
    SILVER_925 ("فضة عيار 925", "Silver 925",  isGold = false, measuredByWeight = true),
    SILVER_800 ("فضة عيار 800", "Silver 800",  isGold = false, measuredByWeight = true),
    SILVER_KILO("كيلو فضة",     "Silver Kilo", isGold = false, measuredByWeight = false)
}

/**
 * One holding. `quantity` is grams for weight-based assets and a unit count for
 * pound/kilo assets. We store the TOTAL price paid (not per-unit) because users
 * remember what they paid at the shop, not per-gram math.
 */
@Parcelize
data class PortfolioItem(
    val id: String,
    val type: AssetType,
    val quantity: Double,
    val purchaseTotalPrice: Double,
    val purchaseDate: Long,
    val notes: String? = null
) : Parcelable {
    val purchasePricePerUnit: Double
        get() = if (quantity > 0) purchaseTotalPrice / quantity else 0.0
}

/**
 * [PortfolioItem] decorated with what it's worth right now. `pricesAvailable`
 * is false during the initial price-feed load — the UI shows a dash instead
 * of a misleading "0 EGP".
 */
data class PortfolioItemWithValue(
    val item: PortfolioItem,
    val currentValue: Double,
    val pricesAvailable: Boolean
) {
    val profit: Double        get() = currentValue - item.purchaseTotalPrice
    val profitPercent: Double get() =
        if (item.purchaseTotalPrice > 0) profit / item.purchaseTotalPrice * 100.0 else 0.0
    val isProfit: Boolean     get() = profit >= 0
}

/** Aggregate stats for the summary card. */
data class PortfolioSummary(
    val itemCount: Int,
    val totalInvested: Double,
    val totalCurrentValue: Double
) {
    val totalProfit: Double        get() = totalCurrentValue - totalInvested
    val totalProfitPercent: Double get() =
        if (totalInvested > 0) totalProfit / totalInvested * 100.0 else 0.0
    val isProfit: Boolean          get() = totalProfit >= 0
}
