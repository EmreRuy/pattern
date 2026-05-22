package com.example.pattern.ui.screens.addHabitScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.usecase.CreateHabitUseCase
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
    private val createHabitUseCase: CreateHabitUseCase
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

        viewModelScope.launch {
            try {
                createHabitUseCase.execute(
                    name = state.habitName,
                    type = state.habitType,
                    emoji = state.emoji,
                    motivation = state.motivation,
                    selectedDays = state.buildHabitDays,
                    durationHours = state.durationHours,
                    durationMinutes = state.durationMinutes,
                    taskCount = state.taskCount,
                    color = state.selectedColor,
                    reminderEnabled = state.reminderEnabled,
                    reminderTime = state.reminderTime
                )
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
