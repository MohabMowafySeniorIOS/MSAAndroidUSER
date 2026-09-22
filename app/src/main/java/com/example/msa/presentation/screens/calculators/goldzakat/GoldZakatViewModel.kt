package com.msa.android.presentation.screens.calculators.goldzakat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.Metal
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.domain.usecase.ZakatCalculator
import com.msa.android.presentation.common.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoldZakatState(
    val g24: String = "", val g22: String = "", val g21: String = "", val g18: String = "",
    val pricePerGram24: Double = 0.0,
    val result: ZakatCalculator.GoldZakatResult? = null
)

@HiltViewModel
class GoldZakatViewModel @Inject constructor(
    private val metalsRepo: MetalsRepository,
    private val calc: ZakatCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(GoldZakatState())
    val state: StateFlow<GoldZakatState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            metalsRepo.observeMetals().collect { metals: List<Metal> ->
                // نفس تصحيح شاشة حساب قيمة الذهب: المستند اسمه "gold" مش "gold_21"
                val base21 = metals.firstOrNull { it.type == "gold" }
                    ?.salePrice?.toDoubleOrNull() ?: 0.0
                val price24 = base21 * (24.0 / 21.0)
                _state.update { it.copy(pricePerGram24 = price24) }
            }
        }
    }

    fun update(field: String, value: String) {
        _state.update { s ->
            when (field) {
                "24" -> s.copy(g24 = value); "22" -> s.copy(g22 = value)
                "21" -> s.copy(g21 = value); "18" -> s.copy(g18 = value)
                else -> s
            }
        }
    }

    fun calculate() {
        val s = _state.value
        val res = calc.goldZakat(
            ZakatCalculator.GoldZakatInput(
                g24 = NumberFormatter.parse(s.g24),
                g22 = NumberFormatter.parse(s.g22),
                g21 = NumberFormatter.parse(s.g21),
                g18 = NumberFormatter.parse(s.g18)
            ),
            pricePerGram24k = s.pricePerGram24
        )
        _state.update { it.copy(result = res) }
    }
}
