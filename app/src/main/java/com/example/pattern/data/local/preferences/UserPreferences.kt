package com.example.pattern.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
    }

    val isFirstRun: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_FIRST_RUN] ?: true
        }

    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_RUN] = false
        }
    }
}
