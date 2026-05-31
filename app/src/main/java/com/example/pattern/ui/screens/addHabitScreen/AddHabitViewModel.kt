package com.example.pattern.ui.screens.addHabitScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.usecase.CreateHabitUseCase
import com.example.pattern.domain.usecase.IsPremiumUserUseCase
import com.example.pattern.domain.usecase.CheckHabitLimitUseCase
import com.example.pattern.domain.usecase.HabitLimitStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val isPremiumUserUseCase: IsPremiumUserUseCase,
    private val checkHabitLimitUseCase: CheckHabitLimitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            isPremiumUserUseCase().collectLatest { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { 
            it.copy(habitName = name) 
        }
    }

    fun onTypeChange(type: String) {
        _uiState.update { 
            it.copy(habitType = type) 
        }
    }

    fun onEmojiChange(emoji: String) {
        _uiState.update { 
            it.copy(emoji = emoji) 
        }
    }

    fun onMotivationChange(motivation: String) {
        _uiState.update { 
            it.copy(motivation = motivation) 
        }
    }

    fun onDaysChange(days: List<DayOfWeek>) {
        _uiState.update { 
            it.copy(buildHabitDays = days.toImmutableList()) 
        }
    }

    fun onDurationChange(hours: Int, minutes: Int) {
        _uiState.update { 
            it.copy(durationHours = hours, durationMinutes = minutes) 
        }
    }

    fun onTaskCountChange(count: Int) {
        _uiState.update { 
            it.copy(taskCount = count) 
        }
    }

    fun onColorChange(color: String) {
        _uiState.update { 
            it.copy(selectedColor = color) 
        }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update { 
            it.copy(reminderEnabled = enabled) 
        }
    }

    fun onReminderTimeChange(time: String) {
        _uiState.update { 
            it.copy(reminderTime = time) 
        }
    }

    fun onStepChange(step: AddHabitStep) {
        _uiState.update { 
            it.copy(currentStep = step) 
        }
    }

    fun onShowTimePickerChange(show: Boolean) {
        _uiState.update { 
            it.copy(showTimePicker = show) 
        }
    }

    fun onShowPermissionDialogChange(show: Boolean) {
        _uiState.update { 
            it.copy(showPermissionDialog = show) 
        }
    }

    fun saveNewHabit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val limitStatus = checkHabitLimitUseCase().first()
            if (limitStatus is HabitLimitStatus.Reached) {
                // Secondary safety check: This shouldn't be reached if UI logic is correct
                return@launch
            }

            val habitId = createHabitUseCase.execute(
                name = uiState.value.habitName,
                type = uiState.value.habitType,
                emoji = uiState.value.emoji,
                color = uiState.value.selectedColor,
                selectedDays = uiState.value.buildHabitDays,
                motivation = uiState.value.motivation,
                reminderEnabled = uiState.value.reminderEnabled,
                reminderTime = uiState.value.reminderTime,
                durationHours = uiState.value.durationHours,
                durationMinutes = uiState.value.durationMinutes,
                taskCount = uiState.value.taskCount
            )
            if (habitId > 0) onSuccess()
        }
    }
}
