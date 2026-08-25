package com.msa.android.presentation.screens.calculators.silvervalue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.domain.usecase.SilverValueCalculator
import com.msa.android.presentation.common.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SilverValueState(
    val g999: String = "", val g925: String = "", val g900: String = "", val g800: String = "", val g600: String = "",
    val base999Sell: Double = 0.0,
    val total: Double = 0.0
)

@HiltViewModel
class SilverValueViewModel @Inject constructor(
    private val metalsRepo: MetalsRepository,
    private val calc: SilverValueCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(SilverValueState())
    val state: StateFlow<SilverValueState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            metalsRepo.observeMetals().collect { metals ->
                // The Firestore `metals/silver` value now represents karat 999.
                val silver = metals.firstOrNull { it.type == "silver" }
                _state.update { it.copy(base999Sell = silver?.salePrice?.toDoubleOrNull() ?: 0.0) }
            }
        }
    }

    fun update(field: String, value: String) {
        _state.update { s ->
            when (field) {
                "999" -> s.copy(g999 = value); "925" -> s.copy(g925 = value)
                "900" -> s.copy(g900 = value); "800" -> s.copy(g800 = value)
                "600" -> s.copy(g600 = value); else -> s
            }
        }
    }

    fun calculate() {
        val s = _state.value
        val r = calc.calculate(
            SilverValueCalculator.Input(
                g999 = NumberFormatter.parse(s.g999),
                g925 = NumberFormatter.parse(s.g925),
                g900 = NumberFormatter.parse(s.g900),
                g800 = NumberFormatter.parse(s.g800),
                g600 = NumberFormatter.parse(s.g600)
            ),
            base999Sell = s.base999Sell
        )
        _state.update { it.copy(total = r.total) }
    }
}
