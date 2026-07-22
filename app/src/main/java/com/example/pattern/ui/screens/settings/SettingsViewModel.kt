package com.example.pattern.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Immutable
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.preferences.UserPreferences
import com.example.pattern.domain.model.Language
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.model.ThemeConfig
import com.example.pattern.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SettingsUiState(
    val settings: Settings = Settings(),
    val currentLanguage: Language = Language.fromCode("en"),
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getSettingsStream(),
        userPreferences.preferredLanguage,
        userPreferences.themeConfig
    ) { settings, languageCode, theme ->
        SettingsUiState(
            settings = settings ?: Settings(),
            currentLanguage = Language.fromCode(languageCode),
            themeConfig = theme,
            isLoading = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState(isLoading = true)
        )

    fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        viewModelScope.launch {
            repository.updateQuietHours(enabled, start, end)
        }
    }

    fun changeLanguage(languageCode: String) {
        // 1. Instant optimistic update
        userPreferences.updateLanguageOptimistically(languageCode)
        
        // 2. Apply to app system
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        
        // 3. Background persistence
        viewModelScope.launch {
            userPreferences.persistLanguage(languageCode)
        }
    }

    fun setThemeConfig(themeConfig: ThemeConfig) {
        // 1. Instant optimistic update on the Main thread
        userPreferences.updateThemeConfigOptimistically(themeConfig)
        
        // 2. Background persistence
        viewModelScope.launch {
            userPreferences.persistThemeConfig(themeConfig)
        }
    }
}
