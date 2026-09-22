package com.msa.android.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import com.msa.android.data.repository.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val languageFlow: Flow<String> = dataStore.data.map { it[KEY_LANG] ?: "ar" }
    val onboardingDoneFlow: Flow<Boolean> = dataStore.data.map { it[KEY_ONBOARDING] ?: false }
    val languageSelectedFlow: Flow<Boolean> = dataStore.data.map { it[KEY_LANG_SELECTED] ?: false }

    suspend fun setLanguage(lang: String) { dataStore.edit { it[KEY_LANG] = lang } }
    suspend fun setOnboardingDone() { dataStore.edit { it[KEY_ONBOARDING] = true } }
    suspend fun setLanguageSelected() { dataStore.edit { it[KEY_LANG_SELECTED] = true } }

    suspend fun currentLanguage(): String = languageFlow.first()

    companion object {
        private val KEY_LANG = stringPreferencesKey("app_lang")
        private val KEY_ONBOARDING = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_done")
        private val KEY_LANG_SELECTED = androidx.datastore.preferences.core.booleanPreferencesKey("lang_selected")



        /** Read language synchronously inside attachBaseContext. */
        fun staticGetLanguage(ctx: Context): String = runBlocking {
            ctx.dataStore.data.first()[KEY_LANG] ?: "ar"
        }
    }
}
