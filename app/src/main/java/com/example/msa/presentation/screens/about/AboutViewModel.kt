package com.msa.android.presentation.screens.about

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

data class AboutState(
    val page: PageContent? = null,
    val language: String = "ar",
    val loading: Boolean = true
)

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val pagesRepo: PagesRepository,
    private val prefs: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(AboutState())
    val state: StateFlow<AboutState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val lang = prefs.currentLanguage()
            _state.update { it.copy(language = lang) }
            pagesRepo.observePage(PageType.ABOUT_US).collect { page ->
                _state.update { it.copy(page = page, loading = false) }
            }
        }
    }
}
