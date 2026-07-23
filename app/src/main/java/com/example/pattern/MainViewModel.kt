package com.example.pattern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.preferences.UserPreferences
import com.example.pattern.domain.model.ThemeConfig
import com.example.pattern.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Any?>(null)
    val startDestination: StateFlow<Any?> = _startDestination.asStateFlow()

    val themeConfig: StateFlow<ThemeConfig> = userPreferences.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeConfig.FOLLOW_SYSTEM)

    private val _isUiReady = MutableStateFlow(false)
    
    val isSplashReady: StateFlow<Boolean> = combine(_startDestination, _isUiReady) { dest, uiReady ->
        when (dest) {
            is Destination.Onboarding -> true
            is Destination.Home -> uiReady
            else -> false
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Destination Logic
        userPreferences.isFirstRun
            .onEach { isFirstRun ->
                _startDestination.value = if (isFirstRun) {
                    Destination.Onboarding
                } else {
                    Destination.Home
                }
            }
            .launchIn(viewModelScope)
    }

    fun onUiReady() {
        _isUiReady.value = true
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setFirstRunCompleted()
        }
    }
}
