package com.msa.android.presentation.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.Metal
import com.msa.android.domain.model.OuncePrice
import com.msa.android.domain.repository.BanksRepository
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.domain.usecase.GoldPrices
import com.msa.android.domain.usecase.PriceCalculator
import com.msa.android.domain.usecase.SilverPrices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeMode { GOLD, SILVER }

/**
 * Trend flag for the ounce and dollar-sagha cells.
 * Mirrors the iOS OunceDollarCell logic:
 *   if (oldPrice > newPrice) → background = red    (price dropped)
 *   else                     → background = green  (price up / same)
 *   after 800ms              → fade back to white
 */
enum class PriceTrend { NONE, UP, DOWN }

data class HomeState(
    val mode: HomeMode = HomeMode.GOLD,
    val gold: GoldPrices? = null,
    val silver: SilverPrices? = null,
    val ouncePrice: OuncePrice = OuncePrice(),
    val ounceTrend: PriceTrend = PriceTrend.NONE,
    val dollarBank: Double = 0.0,
    val dollarSagha: Double = 0.0,
    val saghaTrend: PriceTrend = PriceTrend.NONE,
    val lastUpdated: Long? = null,
    val loading: Boolean = true
)

/** Snapshot of every input needed to compute the dollarSagha — kept around so
 *  that setMode() can recompute without waiting for the next upstream emission. */
private data class HomeInputs(
    val ounce: OuncePrice = OuncePrice(),
    val gold21Sell: Double = 0.0,
    val silverBaseBuy: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val metalsRepo: MetalsRepository,
    private val banksRepo: BanksRepository,
    private val priceCalculator: PriceCalculator
) : ViewModel() {

    companion object {
        private const val TAG = "HomeVM"
        private const val TREND_FLASH_MS = 800L
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Cached inputs so setMode() can recompute Sagha without re-reading Firestore.
    private var lastInputs = HomeInputs()

    // Track previous values for trend comparison (iOS does this in the cell itself).
    private var prevOunceGold: Double = 0.0
    private var prevOunceSilver: Double = 0.0
    private var prevSagha: Double = 0.0

    init {
        observeAll()
    }

    private fun observeAll() {
        viewModelScope.launch {
            combine(
                metalsRepo.observeMetals(),
                metalsRepo.observeOuncePrice(),
                banksRepo.observeCentralBankDollar(),
                metalsRepo.observeLastUpdated()
            ) { metals, ounce, dollarBank, updated ->
                val (goldBuy, goldSell)         = pickBase(metals, "gold")
                val (silverBuy, silverSell)     = pickBase(metals, "silver")

                Log.d(TAG, "metals=${metals.size} gold=$goldBuy/$goldSell silver=$silverBuy/$silverSell dollar=$dollarBank ounceGold=${ounce.goldPrice}")

                val goldPrices   = priceCalculator.computeGold(goldBuy, goldSell)
                val silverPrices = priceCalculator.computeSilver(silverBuy, silverSell)

                lastInputs = HomeInputs(
                    ounce = ounce,
                    gold21Sell = goldSell,
                    silverBaseBuy = silverBuy
                )

                Tick(metals, ounce, dollarBank, updated, goldPrices, silverPrices)
            }.collect { t ->
                applyTick(t)
            }
        }
    }

    private data class Tick(
        val metals: List<Metal>,
        val ounce: OuncePrice,
        val dollarBank: Double,
        val updated: Long?,
        val gold: GoldPrices,
        val silver: SilverPrices
    )

    private suspend fun applyTick(t: Tick) {
        val mode = _state.value.mode
        val currentOunce = if (mode == HomeMode.GOLD) t.ounce.goldPrice else t.ounce.silverPrice
        val prevOunce    = if (mode == HomeMode.GOLD) prevOunceGold else prevOunceSilver

        // Compute Sagha PER MODE — matches iOS:
        //   Gold mode  → (saleGold24Price * 31.1035) / ouncePriceUSD   (uses SELL)
        //   Silver mode → (buySilverPrice  * 31.1035) / ouncePriceUSD   (uses BUY base 800)
        val sagha = computeSaghaFor(mode, t.ounce)

        // Trend flags (only flag when there was a real prior value)
        val ounceTrend = when {
            prevOunce == 0.0 || currentOunce == 0.0 -> PriceTrend.NONE
            currentOunce > prevOunce -> PriceTrend.UP
            currentOunce < prevOunce -> PriceTrend.DOWN
            else -> PriceTrend.NONE
        }
        val saghaTrend = when {
            !sagha.isFinite() || !prevSagha.isFinite() -> PriceTrend.NONE
            prevSagha == 0.0 || sagha == 0.0 -> PriceTrend.NONE
            sagha > prevSagha -> PriceTrend.UP
            sagha < prevSagha -> PriceTrend.DOWN
            else -> PriceTrend.NONE
        }

        _state.value = HomeState(
            mode = mode,
            gold = t.gold,
            silver = t.silver,
            ouncePrice = t.ounce,
            ounceTrend = ounceTrend,
            dollarBank = t.dollarBank,
            dollarSagha = sagha,
            saghaTrend = saghaTrend,
            lastUpdated = t.updated,
            loading = false
        )

        if (mode == HomeMode.GOLD) prevOunceGold = currentOunce else prevOunceSilver = currentOunce
        if (sagha.isFinite()) prevSagha = sagha

        if (ounceTrend != PriceTrend.NONE || saghaTrend != PriceTrend.NONE) {
            delay(TREND_FLASH_MS)
            _state.update { it.copy(ounceTrend = PriceTrend.NONE, saghaTrend = PriceTrend.NONE) }
        }
    }

    /**
     * Mode-specific Sagha computation that matches iOS exactly.
     * iOS does NOT guard division by zero — when ounce is 0 the result is
     * Double.POSITIVE_INFINITY which the UI shows as "inf" (see screenshot 12).
     */
    private fun computeSaghaFor(mode: HomeMode, ounce: OuncePrice): Double = when (mode) {
        HomeMode.GOLD ->
            priceCalculator.dollarSaghaGold(ounce, lastInputs.gold21Sell)
        HomeMode.SILVER ->
            priceCalculator.dollarSaghaSilver(ounce, lastInputs.silverBaseBuy)
    }

    fun setMode(mode: HomeMode) {
        _state.update {
            // Recompute Sagha for the new mode using the cached inputs so the
            // user sees the right value immediately on toggle (no wait for the
            // next upstream emission).
            val newSagha = computeSaghaFor(mode, lastInputs.ounce)
            it.copy(mode = mode, dollarSagha = newSagha, saghaTrend = PriceTrend.NONE)
        }
    }

    private fun pickBase(metals: List<Metal>, type: String): Pair<Double, Double> {
        val metal = metals.firstOrNull { it.type == type }
        val buy = metal?.buyPrice?.toDoubleOrNull() ?: 0.0
        val sell = metal?.salePrice?.toDoubleOrNull() ?: 0.0
        return buy to sell
    }
}
