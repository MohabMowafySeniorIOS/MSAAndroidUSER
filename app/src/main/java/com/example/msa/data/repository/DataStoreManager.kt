package com.msa.android.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "msa_prefs"
)

object DataStoreManager {

    @Volatile
    private var INSTANCE: DataStore<Preferences>? = null

    fun getDataStore(context: Context): DataStore<Preferences> {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile("msa_prefs")
                }
            ).also {
                INSTANCE = it
            }
        }
    }
}
