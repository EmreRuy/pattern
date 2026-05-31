package com.example.pattern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.pattern.data.repository.PremiumRepository
import com.example.pattern.domain.usecase.CheckHabitLimitUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val premiumRepository: PremiumRepository,
    private val checkHabitLimitUseCase: CheckHabitLimitUseCase
) : ViewModel() {

    val habitLimitStatus = checkHabitLimitUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        userPreferences.isFirstRun
            .onEach { isFirstRun ->
                _startDestination.value = if (isFirstRun) {
                    com.example.pattern.ui.navigation.Screens.Onboarding.route
                } else {
                    com.example.pattern.ui.navigation.Screens.Home.route
                }
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setFirstRunCompleted()
        }
    }
}
