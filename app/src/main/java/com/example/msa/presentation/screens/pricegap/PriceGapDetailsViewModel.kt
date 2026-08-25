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

/**
 * Derived numbers shown on the "تفاصيل الفجوة السعرية" screen.
 *
 * The same observables that drive HomeScreen feed this VM, so values
 * stay in sync (ounce polls every 10 s, banks listen live).
 *
 *   gauge & home value:
 *       gaugeValue = (worldPerGramUsd * dollarBank) − gold24LocalEgp
 *       (negative when local is more expensive than world)
 *
 *   detail card "تفاصيل الفجوة السعرية":
 *       gap24 = |gold24LocalEgp − (worldPerGramUsd * dollarBank)|
 *       gap21 = |gold21LocalEgp − (worldPerGramUsd * 21/24 * dollarBank)|
 *       (always positive, shown in red on the card)
 */
@HiltViewModel
class PriceGapDetailsViewModel @Inject constructor(
    metalsRepo: MetalsRepository,
    banksRepo: BanksRepository
) : ViewModel() {

    data class State(
        val worldPerGramUsd: Double = 0.0,       // e.g. 142.57 USD per gram (24K)
        val gold24LocalEgp: Double = 0.0,        // e.g. 7543 EGP per gram (24K, sell price)
        val gold21LocalEgp: Double = 0.0,        // e.g. 6600 EGP per gram (21K)
        val dollarSagha: Double = 0.0,           // gold-implied dollar (local / world)
        val dollarBank: Double = 0.0,            // official bank dollar
        val gaugeValue: Double = 0.0,            // signed: worldInEgp − local
        val gap24Abs: Double = 0.0,              // absolute gap, 24K
        val gap21Abs: Double = 0.0,              // absolute gap, 21K
        val gap24IsNegative: Boolean = false,    // true if local > world (red zone)
        val gap21IsNegative: Boolean = false,
        val loading: Boolean = true
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                metalsRepo.observeMetals(),
                metalsRepo.observeOuncePrice(),
                banksRepo.observeCentralBankDollar()
            ) { metals, ounce, dollarBank ->
                val goldSell21Egp = metals
                    .firstOrNull { it.type == "gold" }
                    ?.salePrice?.toDoubleOrNull() ?: 0.0
                val gold24Local = goldSell21Egp * 24.0 / 21.0
                val gold21Local = goldSell21Egp
                val worldPerGramUsd = if (ounce.goldPrice > 0.0)
                    ounce.goldPrice / 31.1035 else 0.0
                val world24InEgp = worldPerGramUsd * dollarBank
                val world21InEgp = worldPerGramUsd * (21.0 / 24.0) * dollarBank
                val gauge = world24InEgp - gold24Local
                val gap24Signed = world24InEgp - gold24Local
                val gap21Signed = world21InEgp - gold21Local
                val sagha = if (worldPerGramUsd > 0.0)
                    gold24Local / worldPerGramUsd else 0.0
                State(
                    worldPerGramUsd = worldPerGramUsd,
                    gold24LocalEgp = gold24Local,
                    gold21LocalEgp = gold21Local,
                    dollarSagha = sagha,
                    dollarBank = dollarBank,
                    gaugeValue = gauge,
                    gap24Abs = kotlin.math.abs(gap24Signed),
                    gap21Abs = kotlin.math.abs(gap21Signed),
                    gap24IsNegative = gap24Signed < 0.0,
                    gap21IsNegative = gap21Signed < 0.0,
                    loading = false
                )
            }.collect { s -> _state.value = s }
        }
    }
}
