package com.msa.android.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.NotificationPreferences
import com.msa.android.data.source.local.NotificationPreferences.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val enabled: Map<Category, Boolean> = Category.entries.associateWith { true }
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: NotificationPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.enabledMapFlow().collect { map ->
                _state.update { it.copy(enabled = map) }
            }
        }
    }

    fun toggle(category: Category, enabled: Boolean) {
        viewModelScope.launch { prefs.setEnabled(category, enabled) }
    }
}
