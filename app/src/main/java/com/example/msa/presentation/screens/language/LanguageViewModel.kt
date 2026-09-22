package com.msa.android.presentation.screens.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.local.NotificationPreferences
import com.msa.android.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageState(val selected: String = "ar")

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val prefs: LanguagePreferences,
    private val notificationPreferences: NotificationPreferences,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LanguageState())
    val state: StateFlow<LanguageState> = _state.asStateFlow()

    init {
        // نحمّل اللغة المحفوظة عشان الشاشة تفتح والاختيار الحالي مظلّل.
        // من غير كده كانت بتفتح دايماً على "عربي" حتى لو التطبيق إنجليزي —
        // وده مكانش باين لأن الشاشة مكانتش موصولة بأي حتة.
        viewModelScope.launch {
            val current = prefs.currentLanguage()
            _state.update { it.copy(selected = current) }
        }
    }

    fun select(lang: String) { _state.update { it.copy(selected = lang) } }

    fun apply(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setLanguage(_state.value.selected)
            prefs.setLanguageSelected()

            /*
             * اللغة بتحدّد إشعاراته كمان، مش الواجهة بس: الاشتراك في
             * التوبيكس بلغة المستخدم، والسيرفر بيبعت الإشعار الموجّه
             * باللغة المسجّلة عنده.
             *
             * بس ده بيتعمل في **سكوب مستقل** مش جوّه `viewModelScope`،
             * ومن غير ما `onDone()` تستناه:
             *
             * - `syncToken` بيقرا توكن FCM وبيعمل طلب شبكة. لو الشاشة
             *   استنته، المستخدم كان هيضغط «تطبيق» ويقعد مستني مهلة
             *   الشبكة كاملة من غير أي مؤشر تحميل.
             * - ولو خرج من الشاشة في النص، `viewModelScope` بيتلغي —
             *   `onDone()` ما تتنفّذش، واللغة تبقى اتحفظت والواجهة
             *   فاضلة بالقديمة لحد ما يقفل التطبيق ويفتحه.
             *
             * الاتنين بيبلعوا أي فشل جوّه — شوف توثيقهم.
             */
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                notificationPreferences.applyAll()
                deviceRepository.syncToken()
            }

            onDone()
        }
    }
}
