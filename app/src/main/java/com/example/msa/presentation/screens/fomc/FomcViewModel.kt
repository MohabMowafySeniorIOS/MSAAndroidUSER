package com.msa.android.presentation.screens.fomc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.FomcEvent
import com.msa.android.domain.model.FomcStatus
import com.msa.android.domain.repository.FomcRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class FomcState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,

    /** الاجتماع القادم — رأس الشاشة والعدّاد */
    val next: FomcEvent? = null,

    val upcoming: List<FomcEvent> = emptyList(),
    val past: List<FomcEvent> = emptyList(),

    /** الثواني الفاضلة على القرار — بتتحدّث كل ثانية */
    val secondsUntilNext: Long? = null,

    /**
     * فشل التحميل. الشاشة بتعرض زرار إعادة محاولة بدل قايمة فاضية —
     * "مفيش اجتماعات" و"الشبكة وقعت" مش نفس الرسالة للمستخدم.
     */
    val failed: Boolean = false
)

/**
 * شاشة اجتماعات الفيدرالي.
 *
 * بتجيب كل الاجتماعات في طلب واحد وتقسّمها محلياً لقادم/سابق — العدد
 * ٨ اجتماعات في السنة، فالتقسيم في الذاكرة أرخص من طلبين للسيرفر.
 *
 * التقسيم بيتعمل بالوقت الحالي على الجهاز مش بحقل `status` من السيرفر:
 * الرد ممكن يكون من الكاش، فاجتماع خلص من ساعة كان هيفضل في قايمة
 * "القادمة" لحد ما الكاش يخلص.
 */
@HiltViewModel
class FomcViewModel @Inject constructor(
    private val repo: FomcRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FomcState())
    val state: StateFlow<FomcState> = _state.asStateFlow()

    private var tickerJob: Job? = null

    init {
        load(force = false)
    }

    fun refresh() = load(force = true)

    private fun load(force: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(loading = !force, refreshing = force, failed = false) }

            repo.events(forceRefresh = force)
                .onSuccess { all -> applyEvents(all) }
                .onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            refreshing = false,
                            // لو عندنا بيانات قديمة معروضة، سيبها وما تعرضش
                            // شاشة خطأ فوقها — الفشل يبان بس لو مفيش حاجة
                            failed = it.upcoming.isEmpty() && it.past.isEmpty()
                        )
                    }
                }
        }
    }

    private fun applyEvents(all: List<FomcEvent>) {
        val now = Instant.now()

        val upcoming = all
            .filter { it.statusAt(now) != FomcStatus.COMPLETED }
            .sortedBy { it.meetingStartAt ?: Instant.MAX }

        val past = all
            .filter { it.statusAt(now) == FomcStatus.COMPLETED }
            .sortedByDescending { it.meetingStartAt ?: Instant.EPOCH }

        // الاجتماع "القادم" هو أقرب واحد لسه القرار بتاعه ما صدرش —
        // ده ممكن يكون اجتماع جاري دلوقتي، وده مقصود: المستخدم عايز
        // يشوف عدّاد على القرار وهو منعقد.
        val next = upcoming.firstOrNull { it.secondsUntil(now) != null } ?: upcoming.firstOrNull()

        _state.update {
            it.copy(
                loading = false,
                refreshing = false,
                failed = false,
                next = next,
                upcoming = upcoming,
                past = past,
                secondsUntilNext = next?.secondsUntil(now)
            )
        }

        startTicker()
    }

    /**
     * عدّاد الثانية. بيشتغل مرة واحدة بس ويتلغى مع الـ ViewModel، فمفيش
     * تسريب لو المستخدم دخل وخرج من الشاشة.
     *
     * أول ما العدّاد يوصل صفر بنعيد التحميل مرة واحدة: القرار صدر
     * والسيرفر غالباً بقى عنده الفائدة الجديدة.
     */
    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)

                val event = _state.value.next ?: continue
                val remaining = event.secondsUntil()

                _state.update { it.copy(secondsUntilNext = remaining) }

                if (remaining == null) {
                    // الحدث عدّى — نحدّث مرة ونسيب الـ ticker يقف
                    load(force = true)

                    return@launch
                }
            }
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
