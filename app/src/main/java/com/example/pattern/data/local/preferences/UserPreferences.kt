package com.example.pattern.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pattern.domain.model.ThemeConfig
import androidx.datastore.preferences.core.emptyPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
    }

    // Optimistic theme override to prevent DataStore I/O lag
    private val _themeConfigOverride = MutableStateFlow<ThemeConfig?>(null)
    
    // Optimistic language override to prevent DataStore I/O lag
    private val _languageOverride = MutableStateFlow<String?>(null)

    val isFirstRun: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_FIRST_RUN] ?: true
        }

    val themeConfig: Flow<ThemeConfig> = combine(
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val themeName = preferences[PreferencesKeys.THEME_CONFIG] ?: ThemeConfig.FOLLOW_SYSTEM.name
                ThemeConfig.valueOf(themeName)
            },
        _themeConfigOverride
    ) { persistentTheme, overrideTheme ->
        overrideTheme ?: persistentTheme
    }

    val preferredLanguage: Flow<String> = combine(
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                preferences[PreferencesKeys.PREFERRED_LANGUAGE] ?: "en"
            },
        _languageOverride
    ) { persistentLang, overrideLang ->
        overrideLang ?: persistentLang
    }

    /**
     * Staff Engineer Refactoring:
     * 1. Optimistic Update: Set the override immediately on the caller thread
     *    to ensure zero-lag UI response.
     * 2. Persistence: The actual DataStore write happens in the suspend block.
     */
    fun updateThemeConfigOptimistically(themeConfig: ThemeConfig) {
        _themeConfigOverride.value = themeConfig
    }

    suspend fun persistThemeConfig(themeConfig: ThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = themeConfig.name
        }
    }

    fun updateLanguageOptimistically(languageCode: String) {
        _languageOverride.value = languageCode
    }

    suspend fun persistLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_LANGUAGE] = languageCode
        }
    }

    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_RUN] = false
        }
    }
}
