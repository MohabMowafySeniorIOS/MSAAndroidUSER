package com.msa.android.presentation.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.repository.VersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoreState(
    val versionName: String = "1.0"
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val versionRepo: VersionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MoreState())
    val state: StateFlow<MoreState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            versionRepo.observeVersion().collect { info ->
                _state.update { it.copy(versionName = info.version) }
            }
        }
    }
}
