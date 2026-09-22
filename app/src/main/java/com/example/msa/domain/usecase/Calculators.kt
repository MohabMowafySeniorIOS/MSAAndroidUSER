package com.msa.android.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Zakat math — mirrors iOS ZakatVM.swift / ZakatViewModel.swift.
 *
 *  Gold Zakat:
 *   pure gold equivalents (convert all karats to 24k equivalent grams) :
 *     g24 = g24 * 1.0
 *     g22 = g22 * 22/24
 *     g21 = g21 * 21/24
 *     g18 = g18 * 18/24
 *   nisab = 85 grams (24k)
 *   zakat = 2.5% of total pure gold * price per gram 24k (if >= nisab)
 *
 *  Silver Zakat:
 *   نقاء الفضة بمقياس الألف (millesimal fineness) — عيار 925 معناه
 *   925 جزء من 1000، يعني 92.5% فضة خالصة. فالقسمة على 1000 مش على 999:
 *     g999 = g999 * 999/1000
 *     g925 = g925 * 925/1000
 *     g900 = g900 * 900/1000
 *     g800 = g800 * 800/1000
 *     g600 = g600 * 600/1000
 *   nisab = 595 grams من الفضة الخالصة
 *   zakat = 2.5% of total pure silver * price per gram 999 (if >= nisab)
 */
@Singleton
class ZakatCalculator @Inject constructor() {

    companion object {
        /** نصاب الذهب — 85 جرام ذهب خالص (عيار 24) */
        const val GOLD_NISAB = 85.0

        /** نصاب الفضة — 595 جرام فضة خالصة */
        const val SILVER_NISAB = 595.0
    }

    data class GoldZakatInput(
        val g24: Double, val g22: Double, val g21: Double, val g18: Double
    )

    data class GoldZakatResult(
        val totalPureGold: Double,
        val isObligatory: Boolean,
        val zakatGrams: Double,
        val zakatValue: Double
    )

    data class SilverZakatInput(
        val g999: Double, val g925: Double, val g900: Double, val g800: Double, val g600: Double
    )

    data class SilverZakatResult(
        val totalPureSilver: Double,
        val isObligatory: Boolean,
        val zakatGrams: Double,
        val zakatValue: Double
    )

    fun goldZakat(input: GoldZakatInput, pricePerGram24k: Double): GoldZakatResult {
        val pure = input.g24 * 1.0 +
                input.g22 * (22.0 / 24.0) +
                input.g21 * (21.0 / 24.0) +
                input.g18 * (18.0 / 24.0)
        val obligatory = pure >= GOLD_NISAB
        val zg = if (obligatory) pure * 0.025 else 0.0
        return GoldZakatResult(
            totalPureGold = pure,
            isObligatory = obligatory,
            zakatGrams = zg,
            zakatValue = zg * pricePerGram24k
        )
    }

    fun silverZakat(input: SilverZakatInput, pricePerGram999: Double): SilverZakatResult {
        // القسمة على 1000 هي تعريف مقياس النقاء للفضة — القسمة على 999
        // كانت بتحسب عيار 999 كأنه خالص 100% وبتزوّد باقي العيارات ~0.1%
        val pure = input.g999 * (999.0 / 1000.0) +
                input.g925 * (925.0 / 1000.0) +
                input.g900 * (900.0 / 1000.0) +
                input.g800 * (800.0 / 1000.0) +
                input.g600 * (600.0 / 1000.0)
        val obligatory = pure >= SILVER_NISAB
        val zg = if (obligatory) pure * 0.025 else 0.0
        return SilverZakatResult(
            totalPureSilver = pure,
            isObligatory = obligatory,
            zakatGrams = zg,
            zakatValue = zg * pricePerGram999
        )
    }
}

/**
 * GoldValueCalculator — mirrors iOS getGoldPriceViewModel
 *
 *  Uses base 21k price (most active in EG market) and converts
 *  all karats to compute the value of user grams.
 *  total = g24*price24 + g22*price22 + g21*price21 + g18*price18
 */
@Singleton
class GoldValueCalculator @Inject constructor(
    private val priceCalc: PriceCalculator
) {
    data class Input(val g24: Double, val g22: Double, val g21: Double, val g18: Double)
    data class Result(val total: Double, val perKarat: Map<String, Double>)

    fun calculate(input: Input, base21Sell: Double): Result {
        val price24 = base21Sell * (24.0 / 21.0)
        val price22 = base21Sell * (22.0 / 21.0)
        val price21 = base21Sell
        val price18 = base21Sell * (18.0 / 21.0)
        val v24 = input.g24 * price24
        val v22 = input.g22 * price22
        val v21 = input.g21 * price21
        val v18 = input.g18 * price18
        return Result(
            total = v24 + v22 + v21 + v18,
            perKarat = mapOf("24" to v24, "22" to v22, "21" to v21, "18" to v18)
        )
    }
}

/**
 * SilverValueCalculator — mirrors iOS SilverViewModel
 *
 *  The Firestore silver base now represents karat 999 (matching latest iOS).
 *  total = g999*price999 + g925*price925 + g900*price900 + g800*price800 + g600*price600
 */
@Singleton
class SilverValueCalculator @Inject constructor() {
    data class Input(
        val g999: Double, val g925: Double, val g900: Double, val g800: Double, val g600: Double
    )
    data class Result(val total: Double, val perKarat: Map<String, Double>)

    fun calculate(input: Input, base999Sell: Double): Result {
        val p999 = base999Sell                       // base IS karat 999
        val p925 = base999Sell * (925.0 / 999.0)
        val p900 = base999Sell * (900.0 / 999.0)
        val p800 = base999Sell * (800.0 / 999.0)
        val p600 = base999Sell * (600.0 / 999.0)
        val v999 = input.g999 * p999
        val v925 = input.g925 * p925
        val v900 = input.g900 * p900
        val v800 = input.g800 * p800
        val v600 = input.g600 * p600
        return Result(
            total = v999 + v925 + v900 + v800 + v600,
            perKarat = mapOf("999" to v999, "925" to v925, "900" to v900, "800" to v800, "600" to v600)
        )
    }
}
