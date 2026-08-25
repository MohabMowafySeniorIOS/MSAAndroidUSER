package com.msa.android.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.domain.model.OnBoardingPage
import com.msa.android.domain.repository.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnBoardingState(
    val pages: List<OnBoardingPage> = defaultPages(),
    val current: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val repo: OnBoardingRepository,
    private val prefs: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(OnBoardingState())
    val state: StateFlow<OnBoardingState> = _state.asStateFlow()

    val language = prefs.languageFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "ar")

    init {
        viewModelScope.launch {
            repo.observePages().collect { pages ->
                val list = if (pages.isEmpty()) defaultPages() else pages
                _state.update { it.copy(pages = list, isLoading = false) }
            }
        }
    }

    fun next() {
        val s = _state.value
        if (s.current < s.pages.lastIndex) _state.update { it.copy(current = it.current + 1) }
    }

    fun back() {
        val s = _state.value
        if (s.current > 0) _state.update { it.copy(current = it.current - 1) }
    }

    fun goTo(i: Int) { _state.update { it.copy(current = i) } }

    fun finishOnBoarding(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingDone()
            onDone()
        }
    }
}

/** Defaults that mirror the iOS hard-coded onboarding fallback. */
private fun defaultPages(): List<OnBoardingPage> = listOf(
    OnBoardingPage(
        id = "1",
        imageUrl = "",
        titleAr = "مرحبًا بك في MSA",
        titleEn = "Welcome to MSA",
        descAr  = "اشتري وبع الذهب بسهولة وأمان في مكان واحد",
        descEn  = "Buy and sell gold safely in one place",
        order = 0
    ),
    OnBoardingPage(
        id = "2",
        imageUrl = "",
        titleAr = "تابع أسعار الذهب",
        titleEn = "Track gold prices",
        descAr  = "كن على اطلاع دائم بأسعار الذهب وتحركات السوق",
        descEn  = "Stay up to date with live gold prices",
        order = 1
    ),
    OnBoardingPage(
        id = "3",
        imageUrl = "",
        titleAr = "اشتري الذهب في أي وقت",
        titleEn = "Buy gold anytime",
        descAr  = "احصل على أفضل الأسعار المحدثة لحظيًا بضغطة واحدة",
        descEn  = "Get the best live prices in one tap",
        order = 2
    ),
    OnBoardingPage(
        id = "4",
        imageUrl = "",
        titleAr = "ابدأ رحلتك مع الذهب",
        titleEn = "Start your gold journey",
        descAr  = "انضم لآلاف المستخدمين واستثمر بثقة",
        descEn  = "Join thousands and invest with confidence",
        order = 3
    ),
    OnBoardingPage(
        id = "5",
        imageUrl = "",
        titleAr = "أمان وثقة",
        titleEn = "Safety & trust",
        descAr  = "استثماراتك محمية بأعلى معايير الأمان",
        descEn  = "Your investments are protected with top security",
        order = 4
    ),
)
