package com.msa.android.data.source.local

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** موقع المستخدم — إحداثيات بس، من غير أي تفاصيل جهاز */
data class UserLocation(val latitude: Double, val longitude: Double)

/**
 * جلب موقع المستخدم لترتيب الفروع بالأقرب.
 *
 * بنستخدم `LocationManager` من نظام أندرويد نفسه مش
 * Play Services Location: الحاجة هنا هي نقطة تقريبية مرة واحدة عشان
 * نرتّب قايمة فروع — مش تتبّع مستمر ولا دقة أمتار. إضافة مكتبة
 * جديدة (وتبعية على خدمات جوجل) للغرض ده مش مستاهلة، والتطبيق
 * كده بيفضل شغّال على أجهزة من غير GMS.
 *
 * الموقع بيتخزّن في الذاكرة بس — مش بيتكتب على القرص ولا بيتبعت
 * لأي حتة غير الـ API بتاعنا مع طلب الفروع.
 */
@Singleton
class UserLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        const val TAG = "UserLocation"

        /**
         * مهلة جلب موقع جديد. جهاز جوّه مبنى ممكن ياخد وقت طويل
         * للقفل على GPS، والمستخدم مش هيستنى — بعد المهلة بنكمّل
         * بالقايمة غير المرتّبة بدل ما نسيبه على شاشة تحميل.
         */
        const val FRESH_FIX_TIMEOUT_MS = 8_000L

        /** آخر موقع معروف أقدم من كده بنتجاهله */
        const val LAST_KNOWN_MAX_AGE_MS = 10 * 60 * 1000L
    }

    private val manager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** هل خدمة الموقع نفسها مفتوحة في الجهاز؟ */
    fun isLocationEnabled(): Boolean {
        val lm = manager ?: return false

        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * بيرجّع موقع المستخدم، أو `null` لو مفيش إذن / الخدمة مقفولة /
     * المهلة خلصت.
     *
     * بيجرّب آخر موقع معروف الأول — لو فيه واحد حديث، ده أسرع بكتير
     * من انتظار قفل جديد والدقة كافية لترتيب فروع.
     */
    @SuppressLint("MissingPermission")   // بنفحص الإذن في `hasPermission()` فوق
    suspend fun current(): UserLocation? {
        if (!hasPermission()) {
            Log.d(TAG, "no location permission")

            return null
        }

        val lm = manager ?: return null

        lastKnownFresh(lm)?.let { return it.toUserLocation() }

        return withTimeoutOrNull(FRESH_FIX_TIMEOUT_MS) { requestFresh(lm) }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownFresh(lm: LocationManager): Location? {
        val now = System.currentTimeMillis()

        return lm.allProviders
            .asSequence()
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .filter { now - it.time < LAST_KNOWN_MAX_AGE_MS }
            // الأدق هو اللي فيه أصغر دائرة خطأ
            .minByOrNull { it.accuracy }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFresh(lm: LocationManager): UserLocation? =
        suspendCancellableCoroutine { continuation ->
            val provider = when {
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                else -> null
            }

            if (provider == null) {
                continuation.resume(null)

                return@suspendCancellableCoroutine
            }

            // getCurrentLocation موجودة من API 30 بس، والتطبيق minSdk 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val signal = CancellationSignal()

                continuation.invokeOnCancellation { signal.cancel() }

                runCatching {
                    lm.getCurrentLocation(
                        provider,
                        signal,
                        Executors.newSingleThreadExecutor()
                    ) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location?.toUserLocation())
                        }
                    }
                }.onFailure {
                    Log.w(TAG, "getCurrentLocation failed", it)
                    if (continuation.isActive) continuation.resume(null)
                }
            } else {
                // على أندرويد 8–10 بنطلب تحديث واحد وبنفك الاستماع فوراً
                // بعده — من غير كده الـ listener بيفضل شغال وبيستهلك بطارية
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lm.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location.toUserLocation())
                    }

                    @Deprecated("مطلوبة على الإصدارات القديمة")
                    override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}

                    override fun onProviderDisabled(p: String) {
                        lm.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(null)
                    }

                    override fun onProviderEnabled(p: String) {}
                }

                continuation.invokeOnCancellation { lm.removeUpdates(listener) }

                runCatching {
                    lm.requestSingleUpdate(provider, listener, android.os.Looper.getMainLooper())
                }.onFailure {
                    Log.w(TAG, "requestSingleUpdate failed", it)
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }

    private fun Location.toUserLocation() = UserLocation(latitude, longitude)
}
