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

    init {
        // نحمّل اللغة المحفوظة عشان الشاشة تفتح والاختيار الحالي مظلّل.
        // من غير كده كانت بتفتح دايماً على "عربي" حتى لو التطبيق إنجليزي —
        // وده مكانش باين لأن الشاشة مكانتش موصولة بأي حتة.
        viewModelScope.launch {
            val current = prefs.currentLanguage()
            _state.update { it.copy(selected = current) }
        }
    }

    fun select(lang: String) { _state.update { it.copy(selected = lang) } }

    fun apply(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setLanguage(_state.value.selected)
            prefs.setLanguageSelected()
            onDone()
        }
    }
}
