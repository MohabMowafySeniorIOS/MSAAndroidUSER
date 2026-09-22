package com.msa.android.domain.usecase

import com.msa.android.domain.model.OuncePrice
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors iOS GoldenScreen.swift price math.
 *
 *  Karats (purity factors):
 *   24 = 1.000     21 = 0.875     18 = 0.750
 *   جنيه دهب = 8 g  (gold pound = 8 g of 21k)
 *   كيلو دهب = 1000 g
 *
 *  iOS gold formulas:
 *   karat21 = baseGoldPrice
 *   karat24 = baseGoldPrice * (24.0 / 21.0)
 *   karat18 = baseGoldPrice * (18.0 / 21.0)
 *   goldPound = karat21 * 8
 *   goldKilo = karat21 * 1000
 *
 *  Silver karats from base 800:
 *   karat999 = base * (999/800)
 *   karat925 = base * (925/800)
 *   karat800 = base
 *   silverKilo = base * 1000
 *
 *  دولار الصاغة (gold mode) — from iOS OunceDollarCell:
 *   let saleGold24Price = saleGold21Price / 0.875
 *   DollarLabel = (saleGold24Price * 31.1035) / ouncePriceUSD
 *
 *  دولار الصاغة (silver mode) — from iOS OunceDollarCell.configrationSilverCell:
 *   DollarLabel = (buySilverPrice * 31.1035) / ouncePriceUSD
 */
@Singleton
class PriceCalculator @Inject constructor() {

    fun computeGold(base21Buy: Double, base21Sell: Double): GoldPrices {
        return GoldPrices(
            karat24Buy = base21Buy * (24.0 / 21.0),
            karat24Sell = base21Sell * (24.0 / 21.0),
            karat21Buy = base21Buy,
            karat21Sell = base21Sell,
            karat18Buy = base21Buy * (18.0 / 21.0),
            karat18Sell = base21Sell * (18.0 / 21.0),
            goldPoundBuy = base21Buy * 8.0,
            goldPoundSell = base21Sell * 8.0,
            goldKiloBuy = base21Buy * 1000.0,
            goldKiloSell = base21Sell * 1000.0
        )
    }

    /**
     * The price coming from Firestore (`metals/silver`) now represents silver
     * karat 999 (matching the latest iOS behaviour).  Compute the lower karats
     * by scaling down from 999 instead of scaling up from 800.
     *
     *   karat999 = base                  ← Firestore value as-is
     *   karat925 = base × (925 / 999)
     *   karat800 = base × (800 / 999)
     *   silverKilo = base × 1000         ← kilo of 999-purity silver
     */
    fun computeSilver(base999Buy: Double, base999Sell: Double): SilverPrices {
        return SilverPrices(
            karat999Buy   = base999Buy,
            karat999Sell  = base999Sell,
            karat925Buy   = base999Buy  * (925.0 / 999.0),
            karat925Sell  = base999Sell * (925.0 / 999.0),
            karat800Buy   = base999Buy  * (800.0 / 999.0),
            karat800Sell  = base999Sell * (800.0 / 999.0),
            silverKiloBuy  = base999Buy  * 1000.0,
            silverKiloSell = base999Sell * 1000.0
        )
    }

    /**
     * "دولار الصاغة" (gold mode) — 1:1 port of iOS:
     *     calculateGoldDollarRate(
     *         ouncePriceUSD: ounce.goldPrice,
     *         localGramPrice24: saleGold21Price / 0.875)
     *
     * Note: iOS DOES NOT guard against division by zero — if the ounce is 0,
     * the result is Double.POSITIVE_INFINITY and the UI shows "inf" (see iOS
     * screenshot 12 for confirmation). We replicate that behavior exactly.
     *
     * @param base21Sell  the sell price for gold karat 21 (from Firestore)
     */
    fun dollarSaghaGold(ouncePrice: OuncePrice, base21Sell: Double): Double {
        val saleGold24 = base21Sell / 0.875   // identical to base21Sell * (24.0/21.0)
        return (saleGold24 * 31.1035) / ouncePrice.goldPrice
    }

    /**
     * "دولار الصاغة" (silver mode) — 1:1 port of iOS configrationSilverCell:
     *     calculateGoldDollarRate(
     *         ouncePriceUSD: ounce,
     *         localGramPrice24: buySilverPrice)
     *
     * Same no-guard behaviour as above.
     *
     * @param silver999Buy  the buy price for silver karat 999 (raw value from Firestore)
     */
    fun dollarSaghaSilver(ouncePrice: OuncePrice, silver999Buy: Double): Double {
        // CRITICAL: silver mode divides by the silver ounce price (XAG/USD),
        // NOT the gold one. Previously this used goldPrice and gave wildly
        // wrong values when the user toggled to silver.
        return (silver999Buy * 31.1035) / ouncePrice.silverPrice
    }
}

data class GoldPrices(
    val karat24Buy: Double, val karat24Sell: Double,
    val karat21Buy: Double, val karat21Sell: Double,
    val karat18Buy: Double, val karat18Sell: Double,
    val goldPoundBuy: Double, val goldPoundSell: Double,
    val goldKiloBuy: Double, val goldKiloSell: Double
)

data class SilverPrices(
    val karat999Buy: Double, val karat999Sell: Double,
    val karat925Buy: Double, val karat925Sell: Double,
    val karat800Buy: Double, val karat800Sell: Double,
    val silverKiloBuy: Double, val silverKiloSell: Double
)
