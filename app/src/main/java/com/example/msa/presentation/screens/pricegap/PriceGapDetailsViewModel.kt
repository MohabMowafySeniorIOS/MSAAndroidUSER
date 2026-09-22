package com.msa.android.presentation.screens.pricegap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.repository.BanksRepository
import com.msa.android.domain.repository.MetalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/** أي معدن الشاشة بتعرض فجوته */
enum class GapMetal(val routeValue: String) {
    GOLD("gold"),
    SILVER("silver");

    companion object {
        fun from(value: String?): GapMetal =
            if (value == SILVER.routeValue) SILVER else GOLD
    }
}

/**
 * أرقام شاشة «تفاصيل الفجوة السعرية» — **للدهب والفضة**.
 *
 * الشاشة بتتفتح من صف الفجوة في الرئيسية، والصف ده بيعرض فجوة الدهب
 * لما المستخدم مختار «ذهب» وفجوة الفضة لما يختار «فضة». قبل كده كانت
 * الشاشة بتعرض الدهب دايماً، فاللي مختار فضة كان بيدوس على رقم فضة
 * ويلاقي تفاصيل دهب.
 *
 * ## ليه بنحسب المعدنين مع بعض؟
 *
 * نفس المصادر (`metals` و`metalsOunce` والدولار) بتغذّي الاتنين،
 * فحسابهم في نفس المكان أرخص من إعادة تشغيل التدفّق لما المعدن
 * يتغيّر — وبيخلّي الانتقال فوري من غير وميض تحميل.
 *
 * ## المعادلات
 *
 * **الدهب** — زي ما كانت بالظبط، ما اتغيّرش فيها حاجة:
 *
 *     gauge  = (worldPerGramUsd × dollarBank) − gold24Local
 *     gap24  = |gold24Local − (worldPerGramUsd × dollarBank)|
 *     gap21  = |gold21Local − (worldPerGramUsd × 21/24 × dollarBank)|
 *
 * **الفضة** — نفس الشكل، بس الأساس عيار ٩٩٩ مش ٢٤:
 *
 *     gauge  = (worldPerGramUsd × dollarBank) − silver999Local
 *     gap999 = |silver999Local − (worldPerGramUsd × dollarBank)|
 *     gap925 = |silver925Local − (worldPerGramUsd × 925/999 × dollarBank)|
 *
 * ## فرق مقصود بين المعدنين
 *
 * «دولار الصاغة» في الدهب بيتحسب من سعر **البيع**، وفي الفضة من سعر
 * **الشراء** — ده اللي `PriceCalculator.dollarSaghaGold/Silver`
 * بيعمله، والرئيسية بتعرض الرقمين دول. لو عكسناه هنا كان الرقم في
 * الشاشة هيخالف الرقم في الرئيسية لنفس اللحظة.
 */
@HiltViewModel
class PriceGapDetailsViewModel @Inject constructor(
    metalsRepo: MetalsRepository,
    banksRepo: BanksRepository
) : ViewModel() {

    /**
     * أرقام معدن واحد.
     *
     * الأسماء عامة (`high`/`low`) مش (`24`/`21`): العيارات بتختلف
     * حسب المعدن — ٢٤ و٢١ للدهب، ٩٩٩ و٩٢٥ للفضة — والشاشة هي اللي
     * بتحط اللافتة المناسبة.
     */
    data class MetalGap(
        val worldPerGramUsd: Double = 0.0,   // سعر الجرام عالمياً بالدولار
        val localHighEgp: Double = 0.0,      // ٢٤ للدهب / ٩٩٩ للفضة
        val localLowEgp: Double = 0.0,       // ٢١ للدهب / ٩٢٥ للفضة
        val dollarSagha: Double = 0.0,       // الدولار المحسوب من السعر المحلي
        val gaugeValue: Double = 0.0,        // بإشارة: العالمي − المحلي
        val gapHighAbs: Double = 0.0,
        val gapLowAbs: Double = 0.0,
        val gapHighIsNegative: Boolean = false,  // المحلي أعلى من العالمي
        val gapLowIsNegative: Boolean = false
    )

    data class State(
        val gold: MetalGap = MetalGap(),
        val silver: MetalGap = MetalGap(),
        val dollarBank: Double = 0.0,        // مشترك بين المعدنين
        val loading: Boolean = true
    ) {
        fun forMetal(metal: GapMetal): MetalGap =
            if (metal == GapMetal.SILVER) silver else gold
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private companion object {
        /** جرامات الأونصة الترويّة */
        const val GRAMS_PER_OUNCE = 31.1035
    }

    init {
        viewModelScope.launch {
            combine(
                metalsRepo.observeMetals(),
                metalsRepo.observeOuncePrice(),
                banksRepo.observeCentralBankDollar()
            ) { metals, ounce, dollarBank ->

                val goldDoc = metals.firstOrNull { it.type == "gold" }
                val silverDoc = metals.firstOrNull { it.type == "silver" }

                val gold21Sell = goldDoc?.salePrice?.toDoubleOrNull() ?: 0.0
                val silver999Sell = silverDoc?.salePrice?.toDoubleOrNull() ?: 0.0
                val silver999Buy = silverDoc?.buyPrice?.toDoubleOrNull() ?: 0.0

                State(
                    gold = computeGap(
                        ouncePriceUsd = ounce.goldPrice,
                        dollarBank = dollarBank,
                        localHigh = gold21Sell * 24.0 / 21.0,   // عيار ٢٤ من أساس ٢١
                        localLow = gold21Sell,                  // عيار ٢١ هو الأساس
                        lowRatio = 21.0 / 24.0,
                        // الدهب: الصاغة من سعر البيع
                        saghaBase = gold21Sell * 24.0 / 21.0
                    ),
                    silver = computeGap(
                        ouncePriceUsd = ounce.silverPrice,
                        dollarBank = dollarBank,
                        localHigh = silver999Sell,                       // ٩٩٩ هو الأساس
                        localLow = silver999Sell * 925.0 / 999.0,        // عيار ٩٢٥
                        lowRatio = 925.0 / 999.0,
                        // الفضة: الصاغة من سعر **الشراء** — زي الرئيسية
                        saghaBase = silver999Buy
                    ),
                    dollarBank = dollarBank,
                    loading = false
                )
            }.collect { s -> _state.value = s }
        }
    }

    /**
     * حساب موحّد للمعدنين.
     *
     * @param localHigh  سعر الجرام المحلي للعيار الأعلى (٢٤ / ٩٩٩)
     * @param localLow   سعر الجرام المحلي للعيار الأدنى (٢١ / ٩٢٥)
     * @param lowRatio   نسبة العيار الأدنى للأعلى — بتتطبّق على السعر العالمي
     * @param saghaBase  السعر المحلي اللي «دولار الصاغة» بيتحسب منه
     */
    private fun computeGap(
        ouncePriceUsd: Double,
        dollarBank: Double,
        localHigh: Double,
        localLow: Double,
        lowRatio: Double,
        saghaBase: Double
    ): MetalGap {
        val worldPerGramUsd =
            if (ouncePriceUsd > 0.0) ouncePriceUsd / GRAMS_PER_OUNCE else 0.0

        val worldHighEgp = worldPerGramUsd * dollarBank
        val worldLowEgp = worldPerGramUsd * lowRatio * dollarBank

        val gapHighSigned = worldHighEgp - localHigh
        val gapLowSigned = worldLowEgp - localLow

        // القسمة على صفر هنا بترجّع Infinity وبتظهر في الواجهة كـ "inf"،
        // فبنحرسها — الأونصة بتبقى صفر لحد ما Firestore يرد
        val sagha = if (worldPerGramUsd > 0.0) saghaBase / worldPerGramUsd else 0.0

        return MetalGap(
            worldPerGramUsd = worldPerGramUsd,
            localHighEgp = localHigh,
            localLowEgp = localLow,
            dollarSagha = sagha,
            gaugeValue = gapHighSigned,
            gapHighAbs = kotlin.math.abs(gapHighSigned),
            gapLowAbs = kotlin.math.abs(gapLowSigned),
            gapHighIsNegative = gapHighSigned < 0.0,
            gapLowIsNegative = gapLowSigned < 0.0
        )
    }
}
