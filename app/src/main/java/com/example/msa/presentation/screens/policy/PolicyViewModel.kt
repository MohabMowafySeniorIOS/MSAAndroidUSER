package com.msa.android.presentation.screens.policy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.domain.model.PageContent
import com.msa.android.domain.model.PageType
import com.msa.android.domain.repository.PagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PolicyState(
    val page: PageContent? = null,
    val type: PageType = PageType.PRIVACY,
    val language: String = "ar",
    val loading: Boolean = true
)

@HiltViewModel
class PolicyViewModel @Inject constructor(
    private val pagesRepo: PagesRepository,
    private val prefs: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(PolicyState())
    val state: StateFlow<PolicyState> = _state.asStateFlow()

    fun load(type: PageType) {
        _state.update { it.copy(type = type) }
        viewModelScope.launch {
            val lang = prefs.currentLanguage()
            _state.update { it.copy(language = lang) }
            pagesRepo.observePage(type).collect { page ->
                _state.update { it.copy(page = page, loading = false) }
            }
        }
    }
}
