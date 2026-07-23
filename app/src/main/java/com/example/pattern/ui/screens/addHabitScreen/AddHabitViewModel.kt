package com.example.pattern.ui.screens.addHabitScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.repository.EmojiRepository
import com.example.pattern.domain.usecase.CreateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    emojiRepository: EmojiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = combine(
        _uiState,
        emojiRepository.getAllEmojis(),
        emojiRepository.getCategories()
    ) { state, allEmojis, categories ->
        val filtered = allEmojis.filter { emoji ->
            val matchesCategory = (state.selectedEmojiCategory == "All") || (emoji.category == state.selectedEmojiCategory)
            val matchesSearch = state.emojiSearchQuery.isBlank() || 
                    emoji.value.contains(state.emojiSearchQuery) || 
                    emoji.keywords.any { it.contains(state.emojiSearchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }.toImmutableList()

        state.copy(
            filteredEmojis = filtered,
            availableEmojiCategories = categories.toImmutableList()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddHabitUiState()
    )

    fun onNameChange(name: String) {
        _uiState.update { 
            if (it.habitName == name.take(20)) it else it.copy(habitName = name.take(20))
        }
    }

    fun onTypeChange(type: String) {
        _uiState.update { 
            if (it.habitType == type) it else it.copy(habitType = type)
        }
    }

    fun onEmojiChange(emoji: String) {
        _uiState.update { 
            if (it.emoji == emoji) it else it.copy(emoji = emoji)
        }
    }

    fun onEmojiSearchQueryChange(query: String) {
        _uiState.update { 
            if (it.emojiSearchQuery == query) it else it.copy(emojiSearchQuery = query)
        }
    }

    fun onEmojiCategoryChange(category: String) {
        _uiState.update { 
            if (it.selectedEmojiCategory == category) it else it.copy(selectedEmojiCategory = category)
        }
    }

    fun onMotivationChange(motivation: String) {
        _uiState.update { 
            if (it.motivation == motivation) it else it.copy(motivation = motivation)
        }
    }

    fun onDaysChange(days: List<DayOfWeek>) {
        val immutableDays = days.toImmutableList()
        _uiState.update { 
            if (it.buildHabitDays == immutableDays) it else it.copy(buildHabitDays = immutableDays)
        }
    }

    fun onDurationChange(hours: Int, minutes: Int) {
        _uiState.update { 
            if (it.durationHours == hours && it.durationMinutes == minutes) it 
            else it.copy(durationHours = hours, durationMinutes = minutes)
        }
    }

    fun onTaskCountChange(count: Int) {
        _uiState.update { 
            if (it.taskCount == count) it else it.copy(taskCount = count)
        }
    }

    fun onColorChange(color: String) {
        _uiState.update { 
            if (it.selectedColor == color) it else it.copy(selectedColor = color)
        }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update { 
            if (it.reminderEnabled == enabled) it else it.copy(reminderEnabled = enabled)
        }
    }

    fun onReminderTimeChange(time: String) {
        _uiState.update { 
            if (it.reminderTime == time) it else it.copy(reminderTime = time)
        }
    }

    fun onStepChange(step: AddHabitStep) {
        _uiState.update { 
            if (it.currentStep == step) it else it.copy(currentStep = step)
        }
    }

    fun onShowTimePickerChange(show: Boolean) {
        _uiState.update { 
            if (it.showTimePicker == show) it else it.copy(showTimePicker = show)
        }
    }

    fun onShowPermissionDialogChange(show: Boolean) {
        _uiState.update { 
            if (it.showPermissionDialog == show) it else it.copy(showPermissionDialog = show)
        }
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
            } catch (_: Exception) {

            }
        }
    }
}
