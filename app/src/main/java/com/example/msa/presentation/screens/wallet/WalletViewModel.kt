package com.msa.android.presentation.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.repository.AuthRepositoryImpl
import com.msa.android.data.repository.PortfolioApiRepository
import com.msa.android.data.repository.PortfolioDraft
import com.msa.android.data.repository.PortfolioResult
import com.msa.android.domain.model.PortfolioEntry
import com.msa.android.domain.model.PortfolioMetal
import com.msa.android.domain.model.PortfolioTotals
import com.msa.android.domain.usecase.ZakatCalculator
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.data.source.firebase.FirestoreConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletState(
    val loading: Boolean = true,
    val error: String? = null,
    val totals: PortfolioTotals = PortfolioTotals(),
    val items: List<PortfolioEntry> = emptyList(),
    /** الأسعار الحالية — بتتبعت للسيرفر مع كل طلب */
    val gold21: Double = 0.0,
    val silver999: Double = 0.0,
    /** المحفظة بيانات شخصية على السيرفر، فلازم تسجيل دخول */
    val isLoggedIn: Boolean = true
) {
    val isEmpty: Boolean get() = !loading && isLoggedIn && items.isEmpty()
}

/**
 * نتيجة زكاة المحفظة — بتتحسب من القطع اللي المستخدم مسجّلها،
 * فمش محتاج يدخل الأوزان تاني في شاشة الحاسبة.
 */
data class WalletZakat(
    val metal: PortfolioMetal,
    val pureGrams: Double,
    val nisab: Double,
    val isDue: Boolean,
    val zakatGrams: Double,
    val zakatValue: Double
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repo: PortfolioApiRepository,
    private val metals: MetalsRepository,
    private val auth: AuthRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            auth.isLoggedIn.collect { logged ->
                _state.update { it.copy(isLoggedIn = logged, loading = logged) }
                if (logged) refresh() else _state.update {
                    it.copy(items = emptyList(), totals = PortfolioTotals())
                }
            }
        }
        viewModelScope.launch {
            metals.observeMetals().collect { list ->
                val gold = list.firstOrNull { it.type == "gold" }?.salePrice?.toDoubleOrNull() ?: 0.0
                val silver = list.firstOrNull { it.type == "silver" }?.salePrice?.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(gold21 = gold, silver999 = silver) }
                // أول ما الأسعار توصل بنحمّل المحفظة — قبلها القيم الحالية
                // كانت هتطلع أصفار.
                if ((gold > 0 || silver > 0) && _state.value.isLoggedIn) refresh()
            }
        }
    }

    fun refresh() {
        val s = _state.value
        if (!s.isLoggedIn) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = repo.load(s.gold21, s.silver999)) {
                is PortfolioResult.Success -> _state.update {
                    it.copy(loading = false, totals = result.totals, items = result.items)
                }
                is PortfolioResult.Failure -> _state.update {
                    it.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun delete(id: Long, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val error = repo.delete(id)
            if (error == null) refresh()
            onDone(error)
        }
    }

    /**
     * زكاة المحفظة لمعدن واحد.
     *
     * بنعمّم معادلة النقاء بدل ما نعدّد العيارات: الذهب على مقياس 24
     * والفضة على مقياس الألف. كده عيار 14 — الموجود في المحفظة ومش
     * موجود في `ZakatCalculator.GoldInput` — بيتحسب صح من غير حالة خاصة.
     */
    fun zakatFor(metal: PortfolioMetal): WalletZakat {
        val s = _state.value
        val items = s.items.filter { it.metal == metal }

        val scale = if (metal == PortfolioMetal.GOLD) 24.0 else 1000.0
        val pure = items.sumOf { item ->
            val karat = item.karat.toDoubleOrNull() ?: 0.0
            item.weight * (karat / scale)
        }

        val nisab = if (metal == PortfolioMetal.GOLD)
            ZakatCalculator.GOLD_NISAB else ZakatCalculator.SILVER_NISAB
        val due = pure >= nisab
        val grams = if (due) pure * 0.025 else 0.0

        // سعر الجرام الخالص: عيار 24 للذهب، وعيار 999 للفضة
        val purePrice = if (metal == PortfolioMetal.GOLD) s.gold21 * (24.0 / 21.0) else s.silver999

        return WalletZakat(
            metal = metal,
            pureGrams = pure,
            nisab = nisab,
            isDue = due,
            zakatGrams = grams,
            zakatValue = grams * purePrice
        )
    }

    /** سعر الجرام الحالي للعيار المختار — بيملا الحقل تلقائياً في شاشة الإضافة */
    fun currentGramPrice(metal: PortfolioMetal, karat: String): Double {
        val k = karat.toDoubleOrNull() ?: return 0.0
        val s = _state.value
        return if (metal == PortfolioMetal.GOLD) s.gold21 * (k / 21.0)
        else s.silver999 * (k / 999.0)
    }
}

@HiltViewModel
class WalletEditorViewModel @Inject constructor(
    private val repo: PortfolioApiRepository
) : ViewModel() {

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    fun save(existingId: Long?, draft: PortfolioDraft, onSaved: () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            _error.value = null
            val message = if (existingId == null) repo.add(draft) else repo.update(existingId, draft)
            _busy.value = false
            if (message == null) onSaved() else _error.value = message
        }
    }
}
