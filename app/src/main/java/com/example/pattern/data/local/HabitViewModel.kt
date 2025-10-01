package com.example.pattern.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(
    private val dao: HabitDao
) : ViewModel() {

    // Expose all habits as a StateFlow so Compose can observe changes
    val habits = dao.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Add a new habit
    fun addHabit(
        name: String,
        type: String,
        emoji: String,
        durationHours: Int?,
        durationMinutes: Int?,
        selectedDays: String,
        reminderEnabled: Boolean,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                type = type,
                emoji = emoji,
                durationHours = durationHours,
                durationMinutes = durationMinutes,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime,
                selectedDays = selectedDays
            )
            dao.insertHabit(habit)
        }
    }

    // Update an existing habit
    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            dao.updateHabit(habit)
        }
    }

    // Delete a habit
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            dao.deleteHabit(habit)
        }
    }
}