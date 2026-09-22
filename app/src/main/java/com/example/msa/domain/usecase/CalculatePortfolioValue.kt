package com.msa.android.domain.usecase

import com.msa.android.domain.model.AssetType
import com.msa.android.domain.model.PortfolioItem
import com.msa.android.domain.model.PortfolioItemWithValue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Computes today's value of a holding by multiplying the user's quantity by
 * the current market BUY price for that asset.
 *
 * Why buy not sell? In the Egyptian gold market the dealer's *buy* price
 * (سعر الشراء) is what the customer receives if they liquidate now — the
 * honest "if I sold today" number. Using sell would overstate gains since the
 * spread is the dealer's margin, not yours.
 */
@Singleton
class CalculatePortfolioValue @Inject constructor() {

    fun calculate(
        item: PortfolioItem,
        gold: GoldPrices?,
        silver: SilverPrices?
    ): PortfolioItemWithValue {
        val pricesReady = (item.type.isGold && gold != null) ||
                          (!item.type.isGold && silver != null)
        val unitPrice = unitBuyPrice(item.type, gold, silver)
        return PortfolioItemWithValue(
            item = item,
            currentValue = unitPrice * item.quantity,
            pricesAvailable = pricesReady
        )
    }

    private fun unitBuyPrice(type: AssetType, gold: GoldPrices?, silver: SilverPrices?): Double =
        when (type) {
            AssetType.GOLD_24K    -> gold?.karat24Buy    ?: 0.0
            AssetType.GOLD_21K    -> gold?.karat21Buy    ?: 0.0
            AssetType.GOLD_18K    -> gold?.karat18Buy    ?: 0.0
            AssetType.GOLD_POUND  -> gold?.goldPoundBuy  ?: 0.0
            AssetType.GOLD_KILO   -> gold?.goldKiloBuy   ?: 0.0
            AssetType.SILVER_999  -> silver?.karat999Buy ?: 0.0
            AssetType.SILVER_925  -> silver?.karat925Buy ?: 0.0
            AssetType.SILVER_800  -> silver?.karat800Buy ?: 0.0
            AssetType.SILVER_KILO -> silver?.silverKiloBuy ?: 0.0
        }
}
