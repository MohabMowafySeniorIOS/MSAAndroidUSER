package com.msa.android.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.CalendarCurrency
import com.msa.android.domain.model.CalendarDay
import com.msa.android.domain.model.EconomicEvent
import com.msa.android.domain.model.EventImpact
import com.msa.android.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/** مدى التبويبات فوق — نفس ترتيب الشاشة */
enum class CalendarRange { YESTERDAY, TODAY, TOMORROW, WEEK }

/**
 * شاشة التقويم الاقتصادي.
 *
 * ## طلب واحد لكل التبويبات
 *
 * بنجيب من **امبارح لحد ٦ أيام قدّام** في طلب واحد، والتبويبات
 * بتفلتر من نفس النتيجة. يعني تبديل التبويب فوري من غير لودينج ولا
 * طلب شبكة — وده مهم لأن المستخدم بيقلّب بينهم بسرعة.
 *
 * الفلتر (الأهمية والعملة) بيتطبّق في **السيرفر** مش هنا: لو فلترنا
 * محلياً، كنا هنسحب مئات الأحداث عشان نعرض عشرة.
 *
 * ## التجميع بتوقيت الجهاز
 *
 * السيرفر بيجمّع بتوقيت UTC. حدث الساعة ١١ م بتوقيت نيويورك بيبقى
 * اليوم اللي بعده في UTC — فلو عرضنا تجميع السيرفر زي ما هو، مستخدم
 * في القاهرة كان هيشوفه تحت عنوان يوم غلط. عشان كده بنعيد التجميع
 * هنا بتوقيت الجهاز.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: CalendarRepository
) : ViewModel() {

    data class State(
        val loading: Boolean = true,
        val failed: Boolean = false,

        /** كل اللي اتحمّل — مجمّع بتوقيت الجهاز ومرتّب */
        val days: List<CalendarDay> = emptyList(),

        val range: CalendarRange = CalendarRange.TODAY,

        // الفلاتر — فاضية يعني من غير فلتر
        val impacts: Set<EventImpact> = emptySet(),
        val currencies: Set<String> = emptySet(),

        /** قايمة الفلتر — جاية من السيرفر مش ثابتة في التطبيق */
        val availableCurrencies: List<CalendarCurrency> = emptyList()
    ) {
        val hasFilter: Boolean get() = impacts.isNotEmpty() || currencies.isNotEmpty()

        /** أيام التبويب المختار بس */
        val visibleDays: List<CalendarDay>
            get() {
                val today = LocalDate.now(ZoneId.systemDefault())

                return when (range) {
                    CalendarRange.YESTERDAY -> days.filter { it.date == today.minusDays(1) }
                    CalendarRange.TODAY     -> days.filter { it.date == today }
                    CalendarRange.TOMORROW  -> days.filter { it.date == today.plusDays(1) }
                    // «الأسبوع ده» = النهاردة و٦ أيام قدّام. امبارح
                    // مش جواه عن قصد — التبويب ده عن اللي جاي.
                    CalendarRange.WEEK      -> days.filter {
                        !it.date.isBefore(today) && !it.date.isAfter(today.plusDays(6))
                    }
                }
            }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        load()
        loadCurrencies()
    }

    fun setRange(range: CalendarRange) {
        // تبديل التبويب فلترة محلية بس — مفيش طلب شبكة
        _state.update { it.copy(range = range) }
    }

    fun toggleImpact(impact: EventImpact) {
        _state.update { s ->
            s.copy(impacts = if (impact in s.impacts) s.impacts - impact else s.impacts + impact)
        }
        load()
    }

    fun toggleCurrency(code: String) {
        _state.update { s ->
            s.copy(currencies = if (code in s.currencies) s.currencies - code else s.currencies + code)
        }
        load()
    }

    fun clearFilters() {
        _state.update { it.copy(impacts = emptySet(), currencies = emptySet()) }
        load()
    }

    fun refresh() = load(force = true)

    private fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failed = false) }

            val today = LocalDate.now(ZoneId.systemDefault())
            val s = _state.value

            /*
             * بنطلب يوم زيادة على الطرفين.
             *
             * السيرفر بيفلتر بتوقيت UTC وإحنا بنعرض بتوقيت الجهاز،
             * والفرق بينهم لحد ١٤ ساعة. من غير الهامش ده، أحداث أول
             * يوم أو آخر يوم كانت هتقع بره المدى وتختفي.
             */
            repo.days(
                from = today.minusDays(2),
                to = today.plusDays(7),
                impacts = s.impacts,
                currencies = s.currencies,
                forceRefresh = force
            ).onSuccess { days ->
                _state.update { it.copy(loading = false, days = regroupByDeviceZone(days)) }
            }.onFailure {
                _state.update { it.copy(loading = false, failed = true) }
            }
        }
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            repo.currencies().onSuccess { list ->
                _state.update { it.copy(availableCurrencies = list) }
            }
            // الفشل هنا مش بيتعرض: الفلتر بيفضل بالأهمية بس، والشاشة
            // الأساسية شغّالة. رسالة خطأ على حاجة ثانوية بتقلق من غير
            // ما تفيد.
        }
    }

    /**
     * إعادة التجميع بتوقيت الجهاز.
     *
     * السيرفر بيجمّع بـ UTC. الترتيب جوه اليوم بيتحافظ عليه زي ما جه
     * (الأهم الأول عند تساوي الوقت) لأن `sortedBy` في كوتلن مستقر.
     */
    private fun regroupByDeviceZone(days: List<CalendarDay>): List<CalendarDay> {
        val all: List<EconomicEvent> = days.flatMap { it.events }

        return all
            .groupBy { it.occursAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, events) -> CalendarDay(date, events.sortedBy { it.occursAt }) }
            .sortedBy { it.date }
    }
}
