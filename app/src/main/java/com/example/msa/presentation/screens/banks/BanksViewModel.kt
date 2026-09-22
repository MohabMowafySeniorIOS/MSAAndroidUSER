package com.msa.android.presentation.screens.banks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.BankNotificationPreferences
import com.msa.android.domain.model.BankRate
import com.msa.android.domain.repository.BanksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BanksState(
    val selectedCurrency: String = "USD",
    val items: List<BankRate> = emptyList(),
    val loading: Boolean = true,
    /** اسم البنك -> حالة إشعار تغيّر السعر المحفوظة له. */
    val notifEnabled: Map<String, Boolean> = emptyMap()
)

@HiltViewModel
class BanksViewModel @Inject constructor(
    private val repo: BanksRepository,
    private val notifPrefs: BankNotificationPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(BanksState())
    val state: StateFlow<BanksState> = _state.asStateFlow()

    // Track the current subscription so we can cancel it when the currency changes,
    // otherwise we'd keep streaming docs from the old currency.
    private var subscription: Job? = null

    init {
        subscribe("USD")

        // بيحدّث الـUI أول ما أي سويتش يتغيّر (سواء من نفس الشاشة أو أول
        // مرة يتحدد فيها قيمة افتراضية لبنك جديد).
        viewModelScope.launch {
            notifPrefs.enabledMapFlow().collect { map ->
                _state.update { it.copy(notifEnabled = map) }
            }
        }
    }

    fun setCurrency(currency: String) {
        if (_state.value.selectedCurrency == currency) return
        _state.update { it.copy(selectedCurrency = currency, loading = true, items = emptyList()) }
        subscribe(currency)
    }

    /** بينادى لما المستخدم يغيّر سويتش الإشعار بتاع بنك معيّن يدويًا. */
    fun setBankNotificationEnabled(bankName: String, enabled: Boolean) {
        viewModelScope.launch {
            notifPrefs.setEnabled(bankName, enabled)
        }
    }

    private fun subscribe(currency: String) {
        subscription?.cancel()
        subscription = viewModelScope.launch {
            repo.observeBanks(currency).collect { list ->
                _state.update { it.copy(items = list, loading = false) }

                // إشعارات تغيّر السعر خاصة بالدولار بس (زي السيرفر - index.js)،
                // فبنحدد القيم الافتراضية للبنوك الجديدة لما نكون في تبويب USD بس.
                if (currency == "USD") {
                    notifPrefs.ensureDefaults(list.map { it.name })
                }
            }
        }
    }
}
