package com.example.pattern.ui.screens.addHabitScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.IsPremiumUserUseCase
import com.example.pattern.notifications.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager,
    private val isPremiumUserUseCase: IsPremiumUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])
    
    private val _habit = MutableStateFlow<Habit?>(null)
    val habit: StateFlow<Habit?> = _habit.asStateFlow()

    val isPremium = isPremiumUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            _habit.value = repository.getHabitOnce(habitId)
        }
    }

    fun updateHabit(
        name: String,
        type: String,
        durationHours: Int,
        durationMinutes: Int,
        selectedDays: List<DayOfWeek>,
        emoji: String,
        colorHex: String,
        reminderEnabled: Boolean,
        reminderTime: String?,
        motivation: String?,
        taskCount: Int?
    ) {
        val currentHabit = _habit.value ?: return
        
        val daysList = DayOfWeek.entries.map { selectedDays.contains(it) }
        
        val updatedHabit = currentHabit.copy(
            name = name.trim(),
            type = when(type) {
                "Grow" -> HabitType.BUILD
                "Drop" -> HabitType.QUIT
                else -> HabitType.TASK
            },
            durationInMinutes = if (type == "Grow") (durationHours * 60) + durationMinutes else null,
            taskCount = if (type == "Task") taskCount else null,
            selectedDays = daysList.toImmutableList(),
            iconCode = emoji,
            accentColorHex = colorHex,
            reminderTime = if (reminderEnabled) reminderTime else null,
            motivation = if (motivation.isNullOrBlank()) null else motivation.trim()
        )
        viewModelScope.launch {
            repository.updateHabit(updatedHabit)
            if (updatedHabit.reminderTime != null) {
                reminderManager.scheduleReminder(updatedHabit)
            } else {
                reminderManager.cancelReminder(updatedHabit.id)
            }
        }
    }
}
