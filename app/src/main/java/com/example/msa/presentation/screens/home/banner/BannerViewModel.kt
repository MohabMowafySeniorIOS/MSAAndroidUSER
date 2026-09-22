package com.msa.android.presentation.screens.home.banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BannerViewModel @Inject constructor(
    private val repo: BannerRepository
) : ViewModel() {

    // مفيش كاش — بنبدأ فاضي وبنملا من الشبكة.
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    private val _popupVisible = MutableStateFlow(false)
    val popupVisible: StateFlow<Boolean> = _popupVisible.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {

            // null = الشبكة وقعت. مفيش كاش نرجع له، فمفيش حاجة نعرضها.
            val items = repo.fetch() ?: return@launch

            _banners.value = items

            if (repo.shouldShowPopup(items)) {
                _popupVisible.value = true
                // بنسجّلها وهي بتتعرض مش وهي بتتقفل، عشان لو المستخدم
                // قفلها ورجع للرئيسية ما تظهرش تاني في نفس الجلسة.
                repo.markPopupShown()
            }
        }
    }

    /** إغلاق من زرار الـ X أو الضغط على الخلفية. */
    fun dismissPopup() {
        _popupVisible.value = false
    }
}
