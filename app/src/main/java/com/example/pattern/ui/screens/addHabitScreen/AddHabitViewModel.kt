package com.example.pattern.ui.screens.addHabitScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.notifications.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(habitName = name.take(20)) }
    }

    fun onTypeChange(type: String) {
        _uiState.update { it.copy(habitType = type) }
    }

    fun onEmojiChange(emoji: String) {
        _uiState.update { it.copy(emoji = emoji) }
    }

    fun onMotivationChange(motivation: String) {
        _uiState.update { it.copy(motivation = motivation) }
    }

    fun onDaysChange(days: List<DayOfWeek>) {
        _uiState.update { it.copy(buildHabitDays = days.toImmutableList()) }
    }

    fun onDurationChange(hours: Int, minutes: Int) {
        _uiState.update { it.copy(durationHours = hours, durationMinutes = minutes) }
    }

    fun onTaskCountChange(count: Int) {
        _uiState.update { it.copy(taskCount = count) }
    }

    fun onColorChange(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun onReminderTimeChange(time: String) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    fun onStepChange(step: AddHabitStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun onShowTimePickerChange(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun onShowPermissionDialogChange(show: Boolean) {
        _uiState.update { it.copy(showPermissionDialog = show) }
    }

    fun saveNewHabit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid) return

        val totalDurationInMinutes = if (state.habitType == "Grow") {
            (state.durationHours * 60) + state.durationMinutes
        } else {
            null
        }

        val taskCount = if (state.habitType == "Task") {
            state.taskCount
        } else {
            null
        }

        val newHabit = Habit(
            id = 0,
            name = state.habitName.trim(),
            type = when (state.habitType) {
                "Grow" -> HabitType.BUILD
                "Drop" -> HabitType.QUIT
                else -> HabitType.TASK
            },
            iconCode = state.emoji,
            durationInMinutes = totalDurationInMinutes,
            taskCount = taskCount,
            selectedDays = DayOfWeek.entries.map { state.buildHabitDays.contains(it) }.toImmutableList(),
            accentColorHex = state.selectedColor,
            reminderTime = if (state.reminderEnabled) state.reminderTime else null,
            motivation = if (state.motivation.isBlank()) null else state.motivation.trim(),
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            try {
                val id = repository.upsertHabit(newHabit)
                reminderManager.scheduleReminder(newHabit.copy(id = id.toInt()))
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
