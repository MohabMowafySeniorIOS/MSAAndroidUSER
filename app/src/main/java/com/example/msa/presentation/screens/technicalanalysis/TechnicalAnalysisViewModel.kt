package com.msa.android.presentation.screens.technicalanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.TechnicalAnalysisDataSource
import com.msa.android.domain.model.TAPeriod
import com.msa.android.domain.model.TechnicalAnalysisData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TAState(
    val period: TAPeriod = TAPeriod.YESTERDAY,
    val data: TechnicalAnalysisData? = null,
    val loading: Boolean = true
)

@HiltViewModel
class TechnicalAnalysisViewModel @Inject constructor(
    private val source: TechnicalAnalysisDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(TAState())
    val state: StateFlow<TAState> = _state.asStateFlow()

    init { load(TAPeriod.YESTERDAY) }

    fun setPeriod(period: TAPeriod) {
        if (_state.value.period == period && _state.value.data != null) return
        _state.update { it.copy(period = period) }
        load(period)
    }

    private fun load(period: TAPeriod) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            // Mirror iOS 0.25 s artificial wait so the spinner feels consistent
            delay(250)
            _state.update { it.copy(data = source.load(period), loading = false) }
        }
    }
}
