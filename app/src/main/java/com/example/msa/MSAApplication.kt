package com.msa.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.msa.android.R
import com.google.firebase.FirebaseApp
import com.google.android.gms.ads.MobileAds
import com.msa.android.data.source.local.NotificationPreferences
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MSAApplication : Application() {

    @Inject lateinit var notificationPreferences: NotificationPreferences

    companion object {
        // Channel IDs — referenced both here (channel creation) and inside
        // MSAFirebaseMessagingService when posting notifications.
        const val CHANNEL_DEFAULT = "msa_channel_default"   // system default sound
        const val CHANNEL_MSA_SOUND = "msa_channel_sound"   // res/raw/msa_notification.mp3
        const val CHANNEL_DOLLAR = "msa_channel_dollar"     // res/raw/dollar_changed.mp3
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannels()
        applyNotificationPreferences()
        initializeAdMob()
    }

    /**
     * Initialize the Google Mobile Ads SDK off the main thread.
     * `MobileAds.initialize` does I/O and can block UI thread hundreds of ms
     * on cold start. The SDK queues ad requests internally until init completes,
     * so loading an AdView before this finishes is safe (shows nothing until ready).
     */
    private fun initializeAdMob() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            MobileAds.initialize(this@MSAApplication) { /* no-op */ }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1) Default channel — uses the device's regular notification sound.
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DEFAULT,
                "إشعارات MSA",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات عامة (سعر الذهب والفضة، الأخبار)"
                enableLights(true)
                enableVibration(true)
            }
        )

        // 2) MSA custom sound channel — plays res/raw/msa_notification.mp3
        val msaSoundUri =
            Uri.parse("android.resource://$packageName/${R.raw.msa_notification}")
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MSA_SOUND,
                "إشعارات MSA بصوت مخصص",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات MSA بصوت التطبيق المخصص"
                enableLights(true)
                enableVibration(true)
                setSound(
                    msaSoundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
        )

        // 3) Dollar-changed channel — plays res/raw/dollar_changed.mp3
        val dollarSoundUri =
            Uri.parse("android.resource://$packageName/${R.raw.dollar_changed}")
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DOLLAR,
                "تنبيه تغير سعر الدولار",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعار خاص عند تغيّر سعر الدولار"
                enableLights(true)
                enableVibration(true)
                setSound(
                    dollarSoundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
        )
    }

    /**
     * بيطبّق اختيارات المستخدم من شاشة الإعدادات على اشتراكات FCM.
     *
     * قبل كده كان الكود بيشترك في كل التوبيكس عند كل فتحة للتطبيق، وده كان
     * هيلغي أي قفل عمله المستخدم من الإعدادات. دلوقتي بنقرا المحفوظ ونطبّقه —
     * والافتراضي لو مفيش حاجة محفوظة إن كل الأنواع مفعّلة، فسلوك المستخدم
     * القديم زي ما هو.
     */
    private fun applyNotificationPreferences() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            notificationPreferences.applyAll()
        }
    }
}
