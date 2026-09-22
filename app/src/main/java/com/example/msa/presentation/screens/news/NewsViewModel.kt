package com.msa.android.presentation.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.NewsItem
import com.msa.android.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NewsTab { NORMAL, MSA }

data class NewsState(
    val tab: NewsTab = NewsTab.NORMAL,
    val normalArticles: List<NewsItem> = emptyList(),
    val msaArticles: List<NewsItem> = emptyList(),
    val loadingNormal: Boolean = true,
    val loadingMsa: Boolean = true,
    val error: String? = null
) {
    val articles: List<NewsItem> get() = if (tab == NewsTab.NORMAL) normalArticles else msaArticles
    val isLoading: Boolean       get() = if (tab == NewsTab.NORMAL) loadingNormal else loadingMsa
}

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repo: NewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsState())
    val state: StateFlow<NewsState> = _state.asStateFlow()

    init {
        // Subscribe to both feeds in parallel — tab switches feel instant.
        viewModelScope.launch {
            repo.observeNews()
                .catch { e -> _state.update { it.copy(loadingNormal = false, error = e.message) } }
                .collect { list ->
                    _state.update { it.copy(normalArticles = list, loadingNormal = false, error = null) }
                }
        }
        viewModelScope.launch {
            repo.observeMsaNews()
                .catch { e -> _state.update { it.copy(loadingMsa = false, error = e.message) } }
                .collect { list ->
                    _state.update { it.copy(msaArticles = list, loadingMsa = false, error = null) }
                }
        }
    }

    fun setTab(tab: NewsTab) {
        if (_state.value.tab == tab) return
        _state.update { it.copy(tab = tab) }
    }
}
