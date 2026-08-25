package com.msa.android.presentation.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.Metal
import com.msa.android.domain.model.PortfolioItem
import com.msa.android.domain.model.PortfolioItemWithValue
import com.msa.android.domain.model.PortfolioSummary
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.domain.repository.PortfolioRepository
import com.msa.android.domain.usecase.CalculatePortfolioValue
import com.msa.android.domain.usecase.GoldPrices
import com.msa.android.domain.usecase.PriceCalculator
import com.msa.android.domain.usecase.SilverPrices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioState(
    val items: List<PortfolioItemWithValue> = emptyList(),
    val summary: PortfolioSummary = PortfolioSummary(0, 0.0, 0.0),
    val loading: Boolean = true,
    val pricesReady: Boolean = false
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val portfolioRepo: PortfolioRepository,
    private val metalsRepo: MetalsRepository,
    private val priceCalculator: PriceCalculator,
    private val calculateValue: CalculatePortfolioValue
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState())
    val state: StateFlow<PortfolioState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                portfolioRepo.observeItems(),
                metalsRepo.observeMetals()
            ) { items, metals ->
                val (gold, silver) = computePrices(metals)
                buildState(items, gold, silver)
            }.collect { newState ->
                _state.update { newState }
            }
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch { portfolioRepo.removeItem(id) }
    }

    private fun computePrices(metals: List<Metal>): Pair<GoldPrices?, SilverPrices?> {
        val gold = metals.firstOrNull { it.type == "gold" }
        val silver = metals.firstOrNull { it.type == "silver" }
        val goldBuy = gold?.buyPrice?.toDoubleOrNull() ?: 0.0
        val goldSell = gold?.salePrice?.toDoubleOrNull() ?: 0.0
        val silverBuy = silver?.buyPrice?.toDoubleOrNull() ?: 0.0
        val silverSell = silver?.salePrice?.toDoubleOrNull() ?: 0.0

        val goldPrices = if (goldBuy > 0 || goldSell > 0)
            priceCalculator.computeGold(goldBuy, goldSell) else null
        val silverPrices = if (silverBuy > 0 || silverSell > 0)
            priceCalculator.computeSilver(silverBuy, silverSell) else null
        return goldPrices to silverPrices
    }

    private fun buildState(
        rawItems: List<PortfolioItem>,
        gold: GoldPrices?,
        silver: SilverPrices?
    ): PortfolioState {
        val valued = rawItems.map { calculateValue.calculate(it, gold, silver) }
        val sorted = valued.sortedByDescending { it.item.purchaseDate }
        val summary = PortfolioSummary(
            itemCount = sorted.size,
            totalInvested = sorted.sumOf { it.item.purchaseTotalPrice },
            totalCurrentValue = sorted.sumOf { it.currentValue }
        )
        return PortfolioState(
            items = sorted,
            summary = summary,
            loading = false,
            pricesReady = (gold != null || silver != null)
        )
    }
}
