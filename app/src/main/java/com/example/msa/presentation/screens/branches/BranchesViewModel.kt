package com.msa.android.presentation.screens.branches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.local.UserLocationProvider
import com.msa.android.domain.model.Branch
import com.msa.android.domain.repository.BranchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BranchesState(
    val loading: Boolean = true,
    val branches: List<Branch> = emptyList(),
    val arabic: Boolean = true,

    /** الترتيب دلوقتي بالأقرب؟ الواجهة بتعرض شرح مختلف لكل حالة */
    val sortedByDistance: Boolean = false,

    /** المستخدم رفض الإذن — بنعرض زرار «فعّل الموقع» جوّه الشاشة */
    val locationDenied: Boolean = false,

    /** الإذن متاح بس خدمة الموقع نفسها مقفولة في الجهاز */
    val locationOff: Boolean = false,

    val failed: Boolean = false
)

/**
 * شاشة الفروع.
 *
 * إذن الموقع بيتطلب هنا — في الشاشة دي بس، مش عند فتح التطبيق.
 * الطلب في سياقه الطبيعي («عايز أشوف أقرب فرع») نسبة قبوله أعلى
 * بكتير، وبيوفّر على التطبيق سؤال في مراجعة جوجل بلاي عن سبب طلب
 * الموقع من غير مناسبة.
 *
 * **الشاشة بتشتغل من غير الإذن**: لو المستخدم رفض، بنجيب الفروع من
 * غير إحداثيات والسيرفر بيرجّعها بترتيب اللوحة. الرفض بيقلّل فايدة
 * الشاشة، مش بيفضّيها.
 */
@HiltViewModel
class BranchesViewModel @Inject constructor(
    private val repo: BranchRepository,
    private val location: UserLocationProvider,
    private val language: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(BranchesState())
    val state: StateFlow<BranchesState> = _state.asStateFlow()

    init {
        load()
    }

    /**
     * @param forceRefresh السحب للتحديث بيمسح الكاش الأول
     */
    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failed = false) }

            val arabic = language.languageFlow.first() == "ar"

            if (forceRefresh) repo.invalidate()

            val hasPermission = location.hasPermission()
            val serviceOn = location.isLocationEnabled()

            // بنجيب الموقع بس لو الإذن موجود والخدمة شغالة — غير كده
            // `current()` هتستنى المهلة كاملة على الفاضي
            val point = if (hasPermission && serviceOn) location.current() else null

            repo.branches(lat = point?.latitude, lng = point?.longitude)
                .onSuccess { list ->
                    _state.update {
                        it.copy(
                            loading = false,
                            branches = list,
                            arabic = arabic,
                            sortedByDistance = point != null,
                            locationDenied = !hasPermission,
                            locationOff = hasPermission && !serviceOn,
                            failed = false
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            arabic = arabic,
                            locationDenied = !hasPermission,
                            locationOff = hasPermission && !serviceOn,
                            // لو عندنا قايمة قديمة معروضة سيبها — الفشل
                            // يبان بس لو مفيش حاجة نعرضها
                            failed = it.branches.isEmpty()
                        )
                    }
                }
        }
    }

    /** بيتنادى بعد ما المستخدم يرد على نافذة الإذن */
    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(locationDenied = !granted) }

        // الإذن اتقبل — نعيد التحميل عشان الترتيب يبقى بالأقرب.
        // لازم نمسح الكاش لأن مفتاحه كان "no-location".
        if (granted) load(forceRefresh = true)
    }
}
