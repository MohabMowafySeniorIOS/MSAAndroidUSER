package com.msa.android.presentation.screens.calculators.silverzakat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.domain.usecase.ZakatCalculator
import com.msa.android.presentation.common.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SilverZakatState(
    val g999: String = "", val g925: String = "", val g900: String = "", val g800: String = "", val g600: String = "",
    val pricePerGram999: Double = 0.0,
    val result: ZakatCalculator.SilverZakatResult? = null
)

@HiltViewModel
class SilverZakatViewModel @Inject constructor(
    private val metalsRepo: MetalsRepository,
    private val calc: ZakatCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(SilverZakatState())
    val state: StateFlow<SilverZakatState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            metalsRepo.observeMetals().collect { metals ->
                // The Firestore `metals/silver` value now represents karat 999
                // directly (matches latest iOS) — no purity-ratio conversion.
                val p999 = metals.firstOrNull { it.type == "silver" }
                    ?.salePrice?.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(pricePerGram999 = p999) }
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
        val res = calc.silverZakat(
            ZakatCalculator.SilverZakatInput(
                g999 = NumberFormatter.parse(s.g999),
                g925 = NumberFormatter.parse(s.g925),
                g900 = NumberFormatter.parse(s.g900),
                g800 = NumberFormatter.parse(s.g800),
                g600 = NumberFormatter.parse(s.g600)
            ),
            pricePerGram999 = s.pricePerGram999
        )
        _state.update { it.copy(result = res) }
    }
}
