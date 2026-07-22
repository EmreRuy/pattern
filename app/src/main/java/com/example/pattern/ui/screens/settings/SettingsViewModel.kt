package com.example.pattern.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Immutable
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Language
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(getCurrentLanguage())

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getSettingsStream(),
        _currentLanguage
    ) { settings, language ->
        SettingsUiState(
            settings = settings ?: Settings(),
            currentLanguage = language,
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
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        _currentLanguage.value = Language.fromCode(languageCode)
    }

    private fun getCurrentLanguage(): Language {
        val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"
        return Language.fromCode(currentLocale)
    }
}
