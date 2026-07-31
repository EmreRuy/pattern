package com.example.pattern.ui.screens.addHabitScreen

import androidx.compose.runtime.Immutable
import com.example.pattern.domain.model.HabitEmoji
import com.example.pattern.domain.model.HabitType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.DayOfWeek

@Immutable
data class EditHabitUiState(
    val isLoading: Boolean = true,
    val currentStep: AddHabitStep = AddHabitStep.Main,
    val habitName: String = "",
    val habitType: String = "Grow",
    val emoji: String = "🔥",
    val emojiSearchQuery: String = "",
    val selectedEmojiCategory: String = "All",
    val availableEmojiCategories: ImmutableList<String> = persistentListOf("All"),
    val filteredEmojis: ImmutableList<HabitEmoji> = persistentListOf(),
    val motivation: String = "",
    val buildHabitDays: ImmutableList<DayOfWeek> = DayOfWeek.entries.toImmutableList(),
    val durationHours: Int = 0,
    val durationMinutes: Int = 30,
    val taskCount: Int = 1,
    val selectedColor: String = "#6366F1",
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val showTimePicker: Boolean = false,
    val showCustomEmojiDialog: Boolean = false,
    val error: String? = null
) {
    val isNameValid: Boolean get() = habitName.isNotBlank()
    val isColorValid: Boolean get() = selectedColor.isNotBlank()
    val isDaysValid: Boolean get() = buildHabitDays.isNotEmpty()
    val isEmojiValid: Boolean get() = emoji.isNotBlank()
    
    val isValid: Boolean get() = isNameValid && isColorValid && isDaysValid && isEmojiValid
}
