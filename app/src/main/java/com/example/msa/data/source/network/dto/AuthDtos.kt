package com.msa.android.data.source.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/* ── الطلبات ───────────────────────────────────────────────────────── */

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String,
    @Json(name = "device_platform") val devicePlatform: String = "android",
    @Json(name = "app_version") val appVersion: String? = null,
    @Json(name = "fcm_token") val fcmToken: String? = null,
    val locale: String = "ar"
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val phone: String,
    val password: String,
    @Json(name = "device_platform") val devicePlatform: String = "android",
    @Json(name = "app_version") val appVersion: String? = null,
    @Json(name = "fcm_token") val fcmToken: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(val phone: String, val code: String)

@JsonClass(generateAdapter = true)
data class ResendOtpRequest(val phone: String)

/* ── الردود ────────────────────────────────────────────────────────── */

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long,
    val name: String,
    val email: String? = null,
    val phone: String,
    @Json(name = "phone_verified") val phoneVerified: Boolean = false,
    val locale: String? = null
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val message: String?,
    val phone: String?,
    @Json(name = "otp_expires_in") val otpExpiresIn: Int? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(val token: String, val user: UserDto)

@JsonClass(generateAdapter = true)
data class MessageResponse(val message: String?)

@JsonClass(generateAdapter = true)
data class MeResponse(val user: UserDto)

/**
 * شكل خطأ لارافيل الموحّد.
 * 422 بيرجع `errors` فيها رسالة لكل حقل، والباقي `message` بس.
 */
@JsonClass(generateAdapter = true)
data class ApiError(
    val message: String? = null,
    val error: String? = null,
    val errors: Map<String, List<String>>? = null,
    val cooldown: Int? = null
) {
    /** أول رسالة مفيدة نعرضها للمستخدم */
    fun firstMessage(): String? =
        errors?.values?.firstOrNull()?.firstOrNull() ?: message
}

/**
 * تحديث توكن الجهاز واللغة بعد تسجيل الدخول.
 *
 * منفصل عن [LoginRequest] لأن السبب مختلف: الدخول بيحصل مرة، لكن FCM
 * بيجدّد التوكن لوحده والمستخدم بيغيّر اللغة من غير ما يعمل دخول.
 */
@JsonClass(generateAdapter = true)
data class DeviceTokenRequest(
    @Json(name = "fcm_token") val fcmToken: String,
    @Json(name = "device_platform") val devicePlatform: String = "android",
    @Json(name = "app_version") val appVersion: String? = null,
    val locale: String? = null,
)
