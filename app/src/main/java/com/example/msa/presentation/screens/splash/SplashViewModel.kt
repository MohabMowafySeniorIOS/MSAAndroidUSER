package com.msa.android.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashState(
    val ready: Boolean = false
)

/**
 * App always goes Splash → Home now (language picker + onboarding removed
 * from the startup flow). Kept as a ViewModel/state pair — rather than
 * hardcoding a fixed delay in the screen — in case something else needs to
 * gate readiness here later (e.g. warming up a cache) without touching the
 * navigation graph again.
 */
@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = SplashState(ready = true)
        }
    }
}
