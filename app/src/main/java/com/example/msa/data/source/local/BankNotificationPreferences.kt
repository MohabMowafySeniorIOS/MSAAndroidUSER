package com.msa.android.data.source.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * إدارة تفعيل/تعطيل إشعار تغيّر سعر الدولار لكل بنك على حدة (السويتش اللي
 * بيظهر جنب كل بنك في شاشة "أسعار الدولار على البنوك"). نفس المنطق بالظبط
 * الموجود في iOS (BankNotificationSettings.swift):
 *
 *   - أول مرة نشوف فيها بنك جديد (يعني أول مرة نوصله من Firestore):
 *       البنك المركزي المصري -> السويتش مفعّل (ON) افتراضيًا.
 *       أي بنك تاني -> السويتش معطّل (OFF) افتراضيًا.
 *     وبعد كده القيمة بتفضل زي ما المستخدم سابها لحد ما يغيّرها بنفسه.
 *
 *   - التوبيك بتاع كل بنك بيتحسب بنفس خوارزمية السيرفر (index.js):
 *       "bank_" + md5(اسم البنك بالعربي زي ما هو من egrates.com)
 *     حسابه هنا بدل ما نقراه من Firestore بيخلي السويتش يشتغل صح حتى لو
 *     الشاشة بتجيب بياناتها من "currencies/USD/banks" اللي مالهاش حقل topic.
 */
@Singleton
class BankNotificationPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private const val TAG = "BankNotifPrefs"
        private const val ENABLED_PREFIX = "bank_notif_enabled_v1_"
        private val SEEN_BANKS_KEY = stringSetPreferencesKey("bank_notif_seen_banks_v1")

        private fun enabledKey(bankName: String) =
            booleanPreferencesKey(ENABLED_PREFIX + bankName)

        /** نفس الطريقة المستخدمة في باقي الكود (BanksScreen) للتعرف على
         * البنك المركزي. */
        private fun isCentralBank(bankName: String): Boolean =
            bankName.contains("المركز") || bankName.contains("Central")

        /** "bank_" + md5(bankName) - مطابق تمامًا لخوارزمية السيرفر فى index.js. */
        fun topicFor(bankName: String): String {
            val digest = MessageDigest.getInstance("MD5")
                .digest(bankName.toByteArray(Charsets.UTF_8))
            val hex = digest.joinToString("") { "%02x".format(it) }
            return "bank_$hex"
        }
    }

    /** خريطة (اسم البنك -> حالة الإشعار المحفوظة) لكل البنوك المعروفة. */
    fun enabledMapFlow(): Flow<Map<String, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.asMap().entries
                .filter { it.key.name.startsWith(ENABLED_PREFIX) }
                .associate { (key, value) ->
                    key.name.removePrefix(ENABLED_PREFIX) to (value as? Boolean ?: false)
                }
        }

    /**
     * بيتأكد إن كل بنك في اللستة موجودة عنده قيمة افتراضية محفوظة (أول ظهور
     * بس)، وبيطبّق الاشتراك/إلغاء الاشتراك في الـFCM topic على طول لأي بنك
     * جديد. بيتنادى كل ما توصل بيانات بنوك جديدة من Firestore (تبويب
     * الدولار بس، زي ما السيرفر بيبعت إشعارات تغيّر السعر للدولار بس).
     */
    suspend fun ensureDefaults(bankNames: List<String>) {
        val candidates = bankNames.filter { it.isNotBlank() }
        if (candidates.isEmpty()) return

        val seen = dataStore.data.first()[SEEN_BANKS_KEY] ?: emptySet()
        val newBanks = candidates.filter { it !in seen }
        if (newBanks.isEmpty()) return

        dataStore.edit { mutablePrefs ->
            val updatedSeen = (mutablePrefs[SEEN_BANKS_KEY] ?: emptySet()).toMutableSet()
            for (bank in newBanks) {
                mutablePrefs[enabledKey(bank)] = isCentralBank(bank)
                updatedSeen.add(bank)
            }
            mutablePrefs[SEEN_BANKS_KEY] = updatedSeen
        }

        for (bank in newBanks) {
            applySubscription(bank, isCentralBank(bank))
        }
    }

    suspend fun isEnabled(bankName: String): Boolean =
        dataStore.data.first()[enabledKey(bankName)] ?: isCentralBank(bankName)

    /** بينادى لما المستخدم يغيّر السويتش بنفسه من الشاشة. */
    suspend fun setEnabled(bankName: String, enabled: Boolean) {
        if (bankName.isBlank()) return

        dataStore.edit { mutablePrefs ->
            mutablePrefs[enabledKey(bankName)] = enabled
            val updatedSeen = (mutablePrefs[SEEN_BANKS_KEY] ?: emptySet()).toMutableSet()
            updatedSeen.add(bankName)
            mutablePrefs[SEEN_BANKS_KEY] = updatedSeen
        }

        applySubscription(bankName, enabled)
    }

    private fun applySubscription(bankName: String, enabled: Boolean) {
        val topic = topicFor(bankName)
        val msg = FirebaseMessaging.getInstance()
        if (enabled) {
            msg.subscribeToTopic(topic)
                .addOnFailureListener { e -> Log.w(TAG, "subscribe failed ($topic)", e) }
        } else {
            msg.unsubscribeFromTopic(topic)
                .addOnFailureListener { e -> Log.w(TAG, "unsubscribe failed ($topic)", e) }
        }
    }
}
