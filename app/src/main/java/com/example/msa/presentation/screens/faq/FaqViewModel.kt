package com.msa.android.presentation.screens.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.domain.model.FaqItem
import com.msa.android.domain.repository.FaqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FaqState(
    val items: List<FaqItem> = emptyList(),
    val expandedId: String? = null,
    val loading: Boolean = true
)

@HiltViewModel
class FaqViewModel @Inject constructor(
    private val repo: FaqRepository,
    private val prefs: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(FaqState())
    val state: StateFlow<FaqState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val lang = prefs.currentLanguage()
            repo.observeFaqs(lang).collect { list ->
                _state.update { it.copy(items = list, loading = false) }
            }
        }
    }

    fun toggle(id: String) {
        _state.update { it.copy(expandedId = if (it.expandedId == id) null else id) }
    }
}
