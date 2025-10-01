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

    // --- State Management for HomeView ---

    /**
     * The StateFlow that the HomeView will observe. It contains the list of all habits
     * and UI status (like loading).
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

        // 1. Convert hours/minutes into a single Int in minutes
        val totalDurationInMinutes = if (type == HabitType.BUILD) {
            (durationHours * 60) + durationMinutes
        } else {
            null
        }

        // 2. Create the Habit Entity object
        val newHabit = Habit(
            name = name.trim(),
            type = type,
            iconCode = iconCode,
            durationInMinutes = totalDurationInMinutes,
            selectedDays = selectedDays,
            reminderEnabled = reminderEnabled
        )

        // 3. Launch a coroutine to insert the data off the main thread
        viewModelScope.launch {
            try {
                repository.insertHabit(newHabit)
                println("Habit saved successfully: ${newHabit.name}")
            } catch (e: Exception) {
                // Handle the error (e.g., logging or showing a toast to the user)
                println("Failed to save habit: ${e.message}")
            }
        }
    }
}