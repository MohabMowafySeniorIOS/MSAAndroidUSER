package com.msa.android.presentation.screens.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageState(val selected: String = "ar")

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val prefs: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(LanguageState())
    val state: StateFlow<LanguageState> = _state.asStateFlow()

    fun select(lang: String) { _state.update { it.copy(selected = lang) } }

    fun apply(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setLanguage(_state.value.selected)
            prefs.setLanguageSelected()
            onDone()
        }
    }
}
