package com.msa.android.data.source.network

import com.msa.android.data.source.local.AuthPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * بيحط `Authorization: Bearer` على الطلبات المحمية بس.
 *
 * مسارات المحتوى والدخول مفتوحة، وإرسال توكن معاها مش غلط بس
 * مالهوش لازمة — وأهم حاجة إن الـ interceptor ميقعش لو مفيش توكن.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val prefs: AuthPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // runBlocking هنا مقبول: الـ interceptor أصلاً بيشتغل على ثريد
        // الشبكة مش الـ main thread.
        val token = runBlocking { prefs.currentToken() }

        val authorized = if (token.isNullOrBlank()) request
        else request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(
            authorized.newBuilder()
                .addHeader("Accept", "application/json")
                .build()
        )
    }
}
