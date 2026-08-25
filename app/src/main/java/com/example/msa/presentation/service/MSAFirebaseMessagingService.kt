package com.msa.android.presentation.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.msa.android.MSAApplication
import com.msa.android.MSAApplication.Companion.CHANNEL_DEFAULT
import com.msa.android.MSAApplication.Companion.CHANNEL_DOLLAR
import com.msa.android.MSAApplication.Companion.CHANNEL_MSA_SOUND
import com.msa.android.MainActivity
import com.msa.android.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming FCM messages and picks the right notification channel +
 * custom sound based on the message payload. Mirrors iOS behaviour where the
 * APNs payload's `sound` key chooses between the default system sound and a
 * bundled audio file.
 *
 * Expected FCM payload shapes:
 *
 *   1) Default system sound — no sound field, or sound: "default":
 *      {
 *        "notification": { "title": "...", "body": "..." }
 *      }
 *
 *   2) MSA custom sound — matches iOS aSUoV7eCGcs:
 *      {
 *        "notification": { "title": "...", "body": "...", "sound": "msa_notification" }
 *      }
 *      or via data-only message with "sound": "msa_notification".
 *
 *   3) Dollar-change sound — matches iOS dollarChanged.caf:
 *      {
 *        "notification": { "title": "...", "body": "...", "sound": "dollar_changed" }
 *      }
 *      or "sound": "dollarChanged" (legacy iOS naming) — both accepted.
 *
 * NOTE: On Android 8+ the channel determines the sound; we route to the
 * appropriate pre-created channel. On Android < 8 we set the sound URI directly
 * on the builder.
 */
class MSAFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MSAFcm"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM received: from=${message.from} data=${message.data} " +
                "notif=${message.notification?.title}/${message.notification?.body}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        // The `sound` field can arrive either as part of the notification block
        // (read from RemoteMessage.Notification.sound) or inside the data block.
        // Both iOS naming ("dollarChanged") and Android resource naming
        // ("dollar_changed") are accepted.
        val rawSound: String? = message.notification?.sound ?: message.data["sound"]

        postNotification(title, body, rawSound)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Resubscribe to topics in case the token rotation requires it.
        MSAApplication.let { /* topics are subscribed app-wide in MSAApplication.onCreate */ }
    }

    private fun postNotification(title: String, body: String, rawSound: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Normalise the sound key sent from the server.
        val soundKey = rawSound?.lowercase()
            ?.removeSuffix(".caf")
            ?.removeSuffix(".mp3")
            ?.removeSuffix(".wav")
            ?.replace("-", "_")

        val channelId: String
        val soundResId: Int?
        when (soundKey) {
            "dollar_changed", "dollarchanged" -> {
                channelId = CHANNEL_DOLLAR
                soundResId = R.raw.dollar_changed
            }
            "msa", "msa_notification", "asuov7ecgcs" -> {
                channelId = CHANNEL_MSA_SOUND
                soundResId = R.raw.msa_notification
            }
            else -> {
                channelId = CHANNEL_DEFAULT
                soundResId = null   // system default
            }
        }
        Log.d(TAG, "soundKey=$soundKey → channelId=$channelId")

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(getColor(R.color.notification_gold))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // On Android < 8 there are no channels — set the sound URI directly.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && soundResId != null) {
            val uri = Uri.parse("android.resource://$packageName/$soundResId")
            builder.setSound(uri, AudioAttributes.USAGE_NOTIFICATION)
        }

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
