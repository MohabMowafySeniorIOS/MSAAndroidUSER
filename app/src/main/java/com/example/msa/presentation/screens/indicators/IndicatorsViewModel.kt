package com.msa.android.presentation.screens.indicators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.PriceDataGenerator
import com.msa.android.domain.model.ChartDataSet
import com.msa.android.domain.model.MetalType2
import com.msa.android.domain.model.TimePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class IndicatorsState(
    val metal: MetalType2 = MetalType2.GOLD,
    val period: TimePeriod = TimePeriod.H24,
    val charts: List<ChartDataSet> = emptyList(),
    val loading: Boolean = true
)

@HiltViewModel
class IndicatorsViewModel @Inject constructor(
    private val generator: PriceDataGenerator
) : ViewModel() {

    private val _state = MutableStateFlow(IndicatorsState())
    val state: StateFlow<IndicatorsState> = _state.asStateFlow()

    init {
        reload()
    }

    fun setMetal(metal: MetalType2) {
        if (_state.value.metal == metal) return
        _state.update { it.copy(metal = metal) }
        reload()
    }

    fun setPeriod(period: TimePeriod) {
        if (_state.value.period == period) return
        _state.update { it.copy(period = period) }
        reload()
    }

    private fun reload() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            // Tiny artificial delay so the loading spinner doesn't flicker on first paint
            delay(120)
            val charts = withContext(Dispatchers.Default) {
                generator.chartsFor(_state.value.metal, _state.value.period)
            }
            _state.update { it.copy(charts = charts, loading = false) }
        }
    }
}
