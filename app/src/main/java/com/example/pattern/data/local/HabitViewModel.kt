package com.example.pattern.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val habitList: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {
    /*
      The StateFlow that the HomeView will observe. It contains the list of all habits
     */
    val homeUiState: StateFlow<HomeUiState> = repository.getAllHabitsStream()
        .map { habits ->
            HomeUiState(habitList = habits)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun saveNewHabit(
        name: String,
        type: HabitType,
        durationHours: Int,
        durationMinutes: Int,
        selectedDays: List<Boolean>,
        iconCode: String,
        accentColorHex: String
    ) {
        if (name.isBlank()) {
            println("Error: Habit name cannot be empty.")
            return
        }
        // Converts hours/minutes into a single Int in minutes
        val totalDurationInMinutes = if (type == HabitType.BUILD) {
            (durationHours * 60) + durationMinutes
        } else {
            null
        }

        // Creates the Habit Entity object
        val newHabit = Habit(
            name = name.trim(),
            type = type,
            iconCode = iconCode,
            durationInMinutes = totalDurationInMinutes,
            selectedDays = selectedDays,
            accentColorHex = accentColorHex
        )
        viewModelScope.launch {
            try {
                repository.insertHabit(newHabit)
                println("Habit saved successfully: ${newHabit.name}")
            } catch (e: Exception) {
                println("Failed to save habit: ${e.message}")
            }
        }
    }
    fun startTimer(habitId: Int) {
        viewModelScope.launch {
            val habit = homeUiState.value.habitList.firstOrNull { it.id == habitId } ?: return@launch

            // If already completed, don't allow restart
            if (habit.isCompleted) return@launch

            val updated = habit.copy(
                timerStartTime = System.currentTimeMillis(),
                timerPauseTime = null // clear pause
            )

            repository.updateHabit(updated)
        }
    }


    fun pauseTimer(habitId: Int) {
        viewModelScope.launch {
            val habit = homeUiState.value.habitList.firstOrNull { it.id == habitId } ?: return@launch

            // Can't pause if already completed
            if (habit.isCompleted) return@launch

            // Don't pause if timer wasn't running
            if (habit.timerStartTime == null) return@launch

            val updated = habit.copy(
                timerPauseTime = System.currentTimeMillis()
            )

            repository.updateHabit(updated)
        }
    }

    fun resumeTimer(habitId: Int) {
        viewModelScope.launch {
            val habit = homeUiState.value.habitList.firstOrNull { it.id == habitId } ?: return@launch

            if (habit.isCompleted) return@launch
            if (habit.timerStartTime == null) return@launch
            if (habit.timerPauseTime == null) return@launch // can't resume unless paused

            val now = System.currentTimeMillis()
            val pausedDuration = now - habit.timerPauseTime

            val newStartTime = habit.timerStartTime + pausedDuration

            val updated = habit.copy(
                timerStartTime = newStartTime,
                timerPauseTime = null // cleared because running now
            )

            repository.updateHabit(updated)
        }
    }
    fun finishTimer(habitId: Int) {
        viewModelScope.launch {
            val habit = homeUiState.value.habitList.firstOrNull { it.id == habitId } ?: return@launch

            val updated = habit.copy(
                isCompleted = true,
                timerStartTime = null,
                timerPauseTime = null
            )

            repository.updateHabit(updated)
        }
    }


}