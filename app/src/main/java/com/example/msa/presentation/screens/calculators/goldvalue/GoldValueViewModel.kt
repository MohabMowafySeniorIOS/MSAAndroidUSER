package com.msa.android.presentation.screens.calculators.goldvalue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.Metal
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.domain.usecase.GoldValueCalculator
import com.msa.android.presentation.common.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoldValueState(
    val g24: String = "",
    val g22: String = "",
    val g21: String = "",
    val g18: String = "",
    val base21Sell: Double = 0.0,
    val total: Double = 0.0
)

@HiltViewModel
class GoldValueViewModel @Inject constructor(
    private val metalsRepo: MetalsRepository,
    private val calculator: GoldValueCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(GoldValueState())
    val state: StateFlow<GoldValueState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            metalsRepo.observeMetals().collect { metals: List<Metal> ->
                // مستند Firestore اسمه "gold" (الـ type = doc.id)، وسعره هو سعر عيار 21
                // — نفس اللي بتستخدمه الشاشة الرئيسية والسبائك والمحفظة.
                // كان مكتوب "gold_21" وده مستند مش موجود، فالسعر كان بيرجع صفر دايماً.
                val base = metals.firstOrNull { it.type == "gold" }
                val baseSell = base?.salePrice?.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(base21Sell = baseSell) }
            }
        }
    }

    fun update(field: String, value: String) {
        _state.update { state ->
            when (field) {
                "24" -> state.copy(g24 = value)
                "22" -> state.copy(g22 = value)
                "21" -> state.copy(g21 = value)
                "18" -> state.copy(g18 = value)
                else -> state
            }
        }
    }

    fun calculate() {
        val s = _state.value
        val result = calculator.calculate(
            GoldValueCalculator.Input(
                g24 = NumberFormatter.parse(s.g24),
                g22 = NumberFormatter.parse(s.g22),
                g21 = NumberFormatter.parse(s.g21),
                g18 = NumberFormatter.parse(s.g18)
            ),
            base21Sell = s.base21Sell
        )
        _state.update { it.copy(total = result.total) }
    }
}
