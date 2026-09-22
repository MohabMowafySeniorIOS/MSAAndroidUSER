package com.msa.android.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.msa.android.data.repository.AuthRepositoryImpl
import com.msa.android.data.repository.AuthResult
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val busy: Boolean = false,
    val error: String? = null,
    /** بيتملا بعد التسجيل — شاشة الكود بتستخدمه */
    val pendingPhone: String? = null,
    val done: Boolean = false,
    val resendCooldown: Int = 0
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepositoryImpl,
    private val languagePreferences: LanguagePreferences,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun clearError() = _state.update { it.copy(error = null) }

    fun login(phone: String, password: String) = run {
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            when (val result = repo.login(phone, password, fcmToken())) {
                is AuthResult.Success -> {
                    syncDeviceToken()
                    _state.update { it.copy(busy = false, done = true) }
                }
                is AuthResult.Failure -> {
                    // السيرفر بيرجع 409 لو الرقم لسه مش مفعّل وبيبعت كود جديد،
                    // فبنوديه لشاشة الكود بدل ما نعرض رسالة خطأ ونسيبه مقفول.
                    if (result.code == "phone_not_verified") {
                        _state.update { it.copy(busy = false, pendingPhone = phone, error = result.message) }
                    } else {
                        _state.update { it.copy(busy = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, confirm: String) {
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val locale = runCatching { languagePreferences.currentLanguage() }.getOrDefault("ar")

            when (val result = repo.register(name, email, phone, password, confirm, fcmToken(), locale)) {
                is AuthResult.Success -> _state.update { it.copy(busy = false, pendingPhone = phone) }
                is AuthResult.Failure -> _state.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    fun verify(phone: String, code: String) {
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            when (val result = repo.verifyOtp(phone, code)) {
                /*
                 * التوكن بيرجع من `verify-otp` بس، والطلب ده مابيحملش
                 * `fcm_token`. فأول حاجة بعد ما الجلسة تتحفظ: نبعت
                 * توكن الجهاز واللغة — من غير كده المستخدم الجديد
                 * ما يوصلوش أي إشعار موجّه لحد ما يعمل دخول تاني.
                 */
                is AuthResult.Success -> {
                    syncDeviceToken()
                    _state.update { it.copy(busy = false, done = true) }
                }
                is AuthResult.Failure -> _state.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    /**
     * مزامنة توكن الجهاز — **من غير ما الشاشة تستناها**.
     *
     * لو استنيناها، المستخدم كان هيفضل شايف اللودر بعد ما الدخول نجح
     * فعلاً، لمدة طلب شبكة زيادة. ولو الشاشة اتقفلت، `viewModelScope`
     * بيتلغي والمزامنة تضيع.
     */
    private fun syncDeviceToken() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            deviceRepository.syncToken()
        }
    }

    fun resend(phone: String) {
        viewModelScope.launch {
            when (val result = repo.resendOtp(phone)) {
                is AuthResult.Success -> _state.update { it.copy(resendCooldown = 60, error = null) }
                is AuthResult.Failure ->
                    _state.update { it.copy(error = result.message, resendCooldown = result.cooldown ?: 0) }
            }
        }
    }

    fun tickCooldown() {
        _state.update { if (it.resendCooldown > 0) it.copy(resendCooldown = it.resendCooldown - 1) else it }
    }

    /** التوكن بيتبعت مع التسجيل والدخول عشان السيرفر يقدر يوجّه الإشعارات. */
    private suspend fun fcmToken(): String? =
        runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
}
