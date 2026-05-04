package com.example.pattern.ui.screens.addHabitScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.notifications.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    fun saveNewHabit(
        name: String,
        type: HabitType,
        durationHours: Int,
        durationMinutes: Int,
        selectedDays: List<Boolean>,
        iconCode: String,
        accentColorHex: String,
        reminderTime: String? = null,
        motivation: String? = null
    ) {
        if (name.isBlank()) return
        
        val totalDurationInMinutes = if (type == HabitType.BUILD) {
            (durationHours * 60) + durationMinutes
        } else {
            null
        }

        val newHabit = Habit(
            id = 0,
            name = name.trim(),
            type = type,
            iconCode = iconCode,
            durationInMinutes = totalDurationInMinutes,
            selectedDays = selectedDays,
            accentColorHex = accentColorHex,
            reminderTime = reminderTime,
            motivation = if (motivation.isNullOrBlank()) null else motivation.trim(),
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            timerStartTime = null,
            timerPauseTime = null
        )
        viewModelScope.launch {
            try {
                val id = repository.upsertHabit(newHabit)
                reminderManager.scheduleReminder(newHabit.copy(id = id.toInt()))
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
