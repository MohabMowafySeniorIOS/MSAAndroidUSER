package com.msa.android.data.repository

import com.msa.android.data.source.local.AuthPreferences
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.ApiError
import com.msa.android.data.source.network.dto.AuthResponse
import com.msa.android.data.source.network.dto.LoginRequest
import com.msa.android.data.source.network.dto.RegisterRequest
import com.msa.android.data.source.network.dto.ResendOtpRequest
import com.msa.android.data.source.network.dto.VerifyOtpRequest
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/** نتيجة أي عملية حساب — إما نجاح أو رسالة خطأ جاهزة للعرض. */
sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(val message: String, val code: String? = null, val cooldown: Int? = null) : AuthResult
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: MsaApi,
    private val prefs: AuthPreferences,
    private val moshi: Moshi
) {
    val isLoggedIn: Flow<Boolean> = prefs.isLoggedIn
    val userName: Flow<String?> = prefs.userName
    val userPhone: Flow<String?> = prefs.userPhone

    /** بيعمل الحساب ويبعت الكود. التوكن مبيرجعش هنا. */
    suspend fun register(
        name: String, email: String, phone: String,
        password: String, confirm: String, fcmToken: String?,
        locale: String,
    ): AuthResult = safely {
        api.register(
            RegisterRequest(
                name = name.trim(),
                email = email.trim(),
                phone = normalizePhone(phone),
                password = password,
                passwordConfirmation = confirm,
                fcmToken = fcmToken,
                /*
                 * اللغة كانت `"ar"` ثابتة في الـDTO ومحدش بيمرّرها.
                 * يعني السيرفر كان فاكر إن كل مستخدمين أندرويد عرب،
                 * وأي إشعار موجّه كان بيروح عربي للي شغّال إنجليزي.
                 */
                locale = locale,
            )
        ).unitOrThrow()
    }

    /** تأكيد الكود — هنا بس بيتحفظ التوكن. */
    suspend fun verifyOtp(phone: String, code: String): AuthResult = safely {
        api.verifyOtp(VerifyOtpRequest(normalizePhone(phone), code.trim())).saveSession()
    }

    suspend fun resendOtp(phone: String): AuthResult = safely {
        api.resendOtp(ResendOtpRequest(normalizePhone(phone))).unitOrThrow()
    }

    suspend fun login(phone: String, password: String, fcmToken: String?): AuthResult = safely {
        api.login(
            LoginRequest(phone = normalizePhone(phone), password = password, fcmToken = fcmToken)
        ).saveSession()
    }

    /**
     * الخروج بيمسح الجلسة محلياً حتى لو الطلب فشل — المستخدم ضغط خروج
     * ولازم يخرج فعلاً، مش يفضل داخل لأن النت فصل.
     */
    suspend fun logout() {
        runCatching { api.logout() }
        prefs.clear()
    }

    suspend fun deleteAccount(): AuthResult = safely {
        api.deleteAccount().unitOrThrow()
        prefs.clear()
    }

    // ── مساعدات ────────────────────────────────────────────────────

    /** توحيد صيغة الموبايل — نفس منطق الباك اند. */
    private fun normalizePhone(raw: String): String {
        var d = raw.filter(Char::isDigit)
        if (d.startsWith("00")) d = d.drop(2)
        if (d.startsWith("20")) d = "0" + d.drop(2)
        if (d.length == 10 && d.startsWith("1")) d = "0$d"
        return d
    }

    private suspend fun Response<AuthResponse>.saveSession() {
        val body = bodyOrThrow()
        prefs.save(body.token, body.user.id, body.user.name, body.user.phone)
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        if (!isSuccessful) throw ApiException(parseError(this))
        return body() ?: throw ApiException(ApiError(message = "رد فاضي من السيرفر"))
    }

    private fun <T> Response<T>.unitOrThrow() {
        if (!isSuccessful) throw ApiException(parseError(this))
    }

    private fun <T> parseError(response: Response<T>): ApiError {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        val parsed = raw?.let {
            runCatching { moshi.adapter(ApiError::class.java).fromJson(it) }.getOrNull()
        }
        return parsed ?: ApiError(message = defaultMessage(response.code()))
    }

    private fun defaultMessage(code: Int) = when (code) {
        401 -> "لازم تسجّل الدخول"
        403 -> "الحساب موقوف. تواصل مع الدعم."
        404 -> "الرقم ده مش مسجّل"
        429 -> "حاولت كتير، استنى شوية وجرّب تاني"
        in 500..599 -> "في مشكلة في السيرفر، جرّب كمان شوية"
        else -> "حصل خطأ غير متوقع"
    }

    private inline fun safely(block: () -> Unit): AuthResult = try {
        block()
        AuthResult.Success
    } catch (e: ApiException) {
        AuthResult.Failure(
            message = e.error.firstMessage() ?: "حصل خطأ",
            code = e.error.error,
            cooldown = e.error.cooldown
        )
    } catch (e: Exception) {
        // مفيش نت أو timeout
        AuthResult.Failure("تأكد من اتصالك بالإنترنت وجرّب تاني")
    }

    private class ApiException(val error: ApiError) : Exception(error.message)
}
