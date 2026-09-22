package com.msa.android.data.source.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * توكن الدخول وبيانات المستخدم.
 *
 * ملحوظة أمنية: DataStore مش مشفّر. على جهاز غير مروت ده كافي لأن
 * ملفات التطبيق معزولة، لكن لو حبيت تشدّد الأمان استخدم EncryptedFile
 * أو خزّن التوكن في الـ Keystore.
 */
@Singleton
class AuthPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val TOKEN = stringPreferencesKey("auth_token_v1")
        val USER_ID = longPreferencesKey("auth_user_id_v1")
        val USER_NAME = stringPreferencesKey("auth_user_name_v1")
        val USER_PHONE = stringPreferencesKey("auth_user_phone_v1")
    }

    val token: Flow<String?> = dataStore.data.map { it[TOKEN] }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { !it[TOKEN].isNullOrBlank() }

    val userName: Flow<String?> = dataStore.data.map { it[USER_NAME] }

    val userPhone: Flow<String?> = dataStore.data.map { it[USER_PHONE] }

    suspend fun currentToken(): String? = dataStore.data.first()[TOKEN]

    suspend fun save(token: String, id: Long, name: String, phone: String) {
        dataStore.edit {
            it[TOKEN] = token
            it[USER_ID] = id
            it[USER_NAME] = name
            it[USER_PHONE] = phone
        }
    }

    /** بعد تعديل البروفايل — الاسم المعروض في شاشة المزيد لازم يتحدّث */
    suspend fun updateName(name: String) {
        dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(TOKEN); it.remove(USER_ID)
            it.remove(USER_NAME); it.remove(USER_PHONE)
        }
    }
}
