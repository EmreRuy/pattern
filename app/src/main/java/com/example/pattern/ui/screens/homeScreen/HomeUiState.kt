package com.example.pattern.ui.screens.homeScreen

import androidx.compose.runtime.Immutable
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.utils.CalendarDayModel
import com.example.pattern.domain.model.LevelInfo
import java.time.LocalDate

@Immutable
sealed interface HomeUiState {
    data object Loading : HomeUiState
    
    @Immutable
    data class Success(
        val selectedDate: LocalDate = LocalDate.now(),
        val isSelectedDateToday: Boolean = true,
        val habits: List<HabitCardModel> = emptyList(),
        val habitsByDate: Map<LocalDate, List<HabitCardModel>> = emptyMap(),
        val hasAnyHabits: Boolean = false,
        val levelInfo: LevelInfo,
        val explodeConfetti: Boolean = false
    ) : HomeUiState

    @Immutable
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
