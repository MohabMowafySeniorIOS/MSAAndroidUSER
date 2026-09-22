package com.msa.android.data.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.msa.android.BuildConfig
import com.msa.android.data.source.local.AuthPreferences
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.DeviceTokenRequest
import com.msa.android.domain.repository.DeviceRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مزامنة توكن الجهاز واللغة مع السيرفر.
 *
 * بينده من تلات أماكن:
 * - `MSAFirebaseMessagingService.onNewToken` — لما FCM يجدّد التوكن
 * - `LanguageViewModel.apply` — لما المستخدم يغيّر لغة التطبيق
 * - `MSAApplication.onCreate` — مرة عند فتح التطبيق، عشان مستخدم
 *   حدّث التطبيق من نسخة قديمة يوصل توكنه من غير ما يستنى تجديد
 */
@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val api: MsaApi,
    private val authPreferences: AuthPreferences,
    private val languagePreferences: LanguagePreferences,
) : DeviceRepository {

    override suspend fun syncToken(): Boolean {
        /*
         * المسار محمي بتوكن دخول — من غير دخول الطلب هيرجع 401 وخلاص.
         * بنوقف بدري بدل ما نضرب السيرفر بطلب معروف إنه هيفشل.
         *
         * الزائر مش بيضيع عليه حاجة: الإعلانات العامة بتوصله على
         * التوبيك، واللي بيحتاج توكن هو الإشعار الموجّه لحسابه.
         */
        val loggedIn = runCatching { !authPreferences.currentToken().isNullOrBlank() }
            .getOrDefault(false)

        if (!loggedIn) return false

        return runCatching {
            val fcmToken = FirebaseMessaging.getInstance().token.await()

            if (fcmToken.isNullOrBlank()) return false

            val response = api.updateDeviceToken(
                DeviceTokenRequest(
                    fcmToken = fcmToken,
                    appVersion = BuildConfig.VERSION_NAME,
                    locale = languagePreferences.currentLanguage(),
                )
            )

            if (!response.isSuccessful) {
                Log.w(TAG, "device-token rejected: ${response.code()}")
            }

            response.isSuccessful
        }.getOrElse { e ->
            // فشل المزامنة مش سبب يوقّف اللي بينده — شوف توثيق الواجهة
            Log.w(TAG, "device-token sync failed", e)
            false
        }
    }

    private companion object {
        const val TAG = "DeviceToken"
    }
}
