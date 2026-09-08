package com.msa.android.data.source.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تحكّم منفصل لكل نوع إشعار — الذهب، الفضة، أسعار الدولار، الأخبار،
 * والإشعارات العامة. نفس أسلوب [BankNotificationPreferences] بالظبط،
 * ونفس المنطق الموجود في iOS (`NotificationSettings.swift`).
 *
 * كل نوع مربوط بـ FCM topic (أو أكتر). لما المستخدم يقفل النوع بنعمل
 * `unsubscribe` من كل التوبيكس بتاعته، ولما يفتحه بنرجّع نشترك.
 *
 * الافتراضي: كل الأنواع **مفعّلة** — عشان سلوك التطبيق ما يتغيرش لمستخدم
 * قديم بيحدّث، وده نفس اللي كان بيحصل قبل الشاشة دي (الاشتراك في كل
 * التوبيكس عند فتح التطبيق).
 *
 * ملحوظة مهمة: الاشتراك في التوبيك بيمنع رسايل التوبيك بس. لو السيرفر
 * بيبعت إشعار على التوكن مباشرة أو لكل الأجهزة، السويتش مش هيوقّفه —
 * لازم السيرفر يبعت على التوبيكس دي.
 */
@Singleton
class NotificationPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    /** أنواع الإشعارات والتوبيكس المقابلة ليها على السيرفر. */
    enum class Category(val key: String, val topics: List<String>) {
        GOLD    ("gold",    listOf("gold_ar", "gold_en")),
        SILVER  ("silver",  listOf("silver_ar", "silver_en")),
        DOLLAR  ("dollar",  listOf("dollar_prices")),
        NEWS    ("news",    listOf("news_ar", "news_en")),
        GENERAL ("general", listOf("general_ar", "general_en"));
    }

    companion object {
        private const val TAG = "NotifPrefs"
        private const val PREFIX = "notif_enabled_v1_"

        private fun keyFor(category: Category) =
            booleanPreferencesKey(PREFIX + category.key)
    }

    /** حالة كل الأنواع — أي نوع مش متخزّن بيرجع مفعّل. */
    fun enabledMapFlow(): Flow<Map<Category, Boolean>> =
        dataStore.data.map { prefs ->
            Category.entries.associateWith { prefs[keyFor(it)] ?: true }
        }

    suspend fun isEnabled(category: Category): Boolean =
        dataStore.data.first()[keyFor(category)] ?: true

    /** بينادى لما المستخدم يغيّر السويتش من شاشة الإعدادات. */
    suspend fun setEnabled(category: Category, enabled: Boolean) {
        dataStore.edit { it[keyFor(category)] = enabled }
        applySubscription(category, enabled)
    }

    /**
     * بيطبّق الاشتراكات المحفوظة على FCM. بيتنادى عند فتح التطبيق بدل
     * الاشتراك الأعمى في كل التوبيكس، عشان اختيار المستخدم ما يتلغيش
     * كل مرة يفتح التطبيق.
     */
    suspend fun applyAll() {
        val saved = dataStore.data.first()
        for (category in Category.entries) {
            applySubscription(category, saved[keyFor(category)] ?: true)
        }
    }

    private fun applySubscription(category: Category, enabled: Boolean) {
        val msg = FirebaseMessaging.getInstance()
        for (topic in category.topics) {
            if (enabled) {
                msg.subscribeToTopic(topic)
                    .addOnFailureListener { e -> Log.w(TAG, "subscribe failed ($topic)", e) }
            } else {
                msg.unsubscribeFromTopic(topic)
                    .addOnFailureListener { e -> Log.w(TAG, "unsubscribe failed ($topic)", e) }
            }
        }
    }
}
