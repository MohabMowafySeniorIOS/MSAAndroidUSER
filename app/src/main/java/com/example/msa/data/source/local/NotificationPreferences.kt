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
 * ## الاشتراك بلغة المستخدم بس
 *
 * كل نوع ليه توبيكين — `x_ar` و`x_en`. النسخة القديمة كانت بتشترك في
 * الاتنين مع بعض، فلما السيرفر يبعت نفس الإعلان بالعربي على `x_ar`
 * وبالإنجليزي على `x_en` الجهاز كان **بياخد إشعارين**. ده كان بيحصل
 * فعلاً مع إشعارات الفيدرالي.
 *
 * دلوقتي بنشترك في توبيك لغة المستخدم وبنلغي التاني صراحةً. الإلغاء
 * الصريح مهم: من غيره المستخدم اللي محدّث من نسخة قديمة بيفضل مشترك
 * في اللغتين للأبد، لأن مفيش حد بيقوله يسيب التوبيك القديم.
 *
 * ولما المستخدم يغيّر لغة التطبيق لازم ينادي [applyAll] تاني.
 */
@Singleton
class NotificationPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val languagePreferences: LanguagePreferences,
) {

    /**
     * أنواع الإشعارات والتوبيكس المقابلة ليها على السيرفر.
     *
     * [topics] هي **كل** التوبيكس بتاعة النوع بغض النظر عن اللغة —
     * بنستخدمها في الإلغاء. الاشتراك بيمشي على [topicsFor].
     */
    enum class Category(val key: String, val topics: List<String>) {
        GOLD    ("gold",    listOf("gold_ar", "gold_en")),
        SILVER  ("silver",  listOf("silver_ar", "silver_en")),
        DOLLAR  ("dollar",  listOf("dollar_prices")),
        NEWS    ("news",    listOf("news_ar", "news_en")),
        GENERAL ("general", listOf("general_ar", "general_en")),

        /**
         * اجتماعات الفيدرالي — تنبيه قبل الاجتماع، وعند صدور القرار،
         * والبيان، والمحضر. السيرفر بيبعت على التوبيكس دي من
         * `fomc:notify`.
         */
        FOMC    ("fomc",    listOf("fomc_ar", "fomc_en"));

        /**
         * التوبيكس اللي المفروض المستخدم يكون مشترك فيها بلغته.
         *
         * `dollar_prices` مالهوش لاحقة لغة — بيرجع زي ما هو للغتين.
         */
        fun topicsFor(language: String): List<String> {
            val suffix = if (language == "en") "_en" else "_ar"
            val localized = topics.filter { it.endsWith("_ar") || it.endsWith("_en") }

            return if (localized.isEmpty()) {
                topics
            } else {
                topics.filter { !localized.contains(it) } + localized.filter { it.endsWith(suffix) }
            }
        }
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
        applySubscription(category, enabled, currentLanguage())
    }

    /**
     * بيطبّق الاشتراكات المحفوظة على FCM. بيتنادى عند فتح التطبيق بدل
     * الاشتراك الأعمى في كل التوبيكس، عشان اختيار المستخدم ما يتلغيش
     * كل مرة يفتح التطبيق.
     *
     * وبيتنادى كمان بعد تغيير لغة التطبيق، عشان التوبيكس تتبدّل.
     */
    suspend fun applyAll() {
        val saved = dataStore.data.first()
        val language = currentLanguage()

        for (category in Category.entries) {
            applySubscription(category, saved[keyFor(category)] ?: true, language)
        }
    }

    private suspend fun currentLanguage(): String =
        runCatching { languagePreferences.currentLanguage() }.getOrDefault("ar")

    private fun applySubscription(category: Category, enabled: Boolean, language: String) {
        val msg = FirebaseMessaging.getInstance()

        /*
         * التوبيكس اللي المفروض نكون مشتركين فيها، وكل الباقي بيتلغي.
         *
         * بنلغي بشكل صريح مش بنسيب — المستخدم اللي حدّث من نسخة قديمة
         * مشترك في اللغتين، ومفيش حاجة تانية هتخرّجه من القديمة.
         */
        val wanted = if (enabled) category.topicsFor(language) else emptyList()

        for (topic in category.topics) {
            if (wanted.contains(topic)) {
                msg.subscribeToTopic(topic)
                    .addOnFailureListener { e -> Log.w(TAG, "subscribe failed ($topic)", e) }
            } else {
                msg.unsubscribeFromTopic(topic)
                    .addOnFailureListener { e -> Log.w(TAG, "unsubscribe failed ($topic)", e) }
            }
        }
    }
}
