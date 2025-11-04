package com.example.pattern.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    /**
     * The StateFlow that the HomeView will observe. It contains the list of all habits
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
        reminderEnabled: Boolean,
        iconCode: String
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
            reminderEnabled = reminderEnabled
        )
        // Launch a coroutine to insert the data off the main thread
        viewModelScope.launch {
            try {
                repository.insertHabit(newHabit)
                println("Habit saved successfully: ${newHabit.name}")
            } catch (e: Exception) {
                println("Failed to save habit: ${e.message}")
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habit)
                println("Habit deleted: ${habit.name}")
            } catch (e: Exception) {
                println("Failed to delete habit: ${e.message}")
            }
        }
    }
}