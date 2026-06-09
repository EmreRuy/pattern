package com.example.pattern

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.preferences.UserPreferences
import com.example.pattern.domain.usecase.CheckHabitLimitUseCase
import com.example.pattern.domain.usecase.HabitLimitStatus
import com.example.pattern.ui.navigation.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(
        val startDestination: String
    ) : MainUiState
}

sealed interface NavigationEvent {
    data class NavigateToDetail(val habitId: Int) : NavigationEvent
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val checkHabitLimitUseCase: CheckHabitLimitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    val habitLimitStatus: StateFlow<HabitLimitStatus?> = checkHabitLimitUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        userPreferences.isFirstRun
            .onEach { isFirstRun ->
                val startDestination = if (isFirstRun) {
                    Screens.Onboarding.route
                } else {
                    Screens.Home.route
                }
                _uiState.value = MainUiState.Success(startDestination)
            }
            .launchIn(viewModelScope)
    }

    fun handleIntent(intent: Intent?) {
        val habitId = intent?.getIntExtra("HABIT_ID", -1) ?: -1
        if (habitId != -1) {
            intent?.removeExtra("HABIT_ID")
            viewModelScope.launch {
                _navigationEvents.send(NavigationEvent.NavigateToDetail(habitId))
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setFirstRunCompleted()
        }
    }
}
