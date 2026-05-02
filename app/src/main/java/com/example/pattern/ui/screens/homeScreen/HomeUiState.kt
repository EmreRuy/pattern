package com.example.pattern.ui.screens.homeScreen

import com.example.pattern.data.model.HabitCardModel
import com.example.pattern.utils.LevelInfo
import java.time.LocalDate

sealed interface HomeUiState {
    data object Loading : HomeUiState
    
    data class Success(
        val selectedDate: LocalDate = LocalDate.now(),
        val isSelectedDateToday: Boolean = true,
        val habits: List<HabitCardModel> = emptyList(),
        val habitsByDate: Map<LocalDate, List<HabitCardModel>> = emptyMap(),
        val hasAnyHabits: Boolean = false,
        val levelInfo: LevelInfo,
        val explodeConfetti: Boolean = false
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}

sealed interface HomeUiEvent {
    data class OnDateSelected(val date: LocalDate) : HomeUiEvent
    data class OnTimerStart(val habitId: Int, val date: LocalDate) : HomeUiEvent
    data class OnTimerPause(val habitId: Int, val date: LocalDate) : HomeUiEvent
    data class OnTimerResume(val habitId: Int, val date: LocalDate) : HomeUiEvent
    data class OnTimerFinish(val habitId: Int, val date: LocalDate) : HomeUiEvent
    data class OnTimerUnfinish(val habitId: Int, val date: LocalDate) : HomeUiEvent
    data class OnTaskToggle(val habitId: Int, val date: LocalDate, val completed: Boolean) : HomeUiEvent
    data object OnConfettiAnimationShown : HomeUiEvent
}
