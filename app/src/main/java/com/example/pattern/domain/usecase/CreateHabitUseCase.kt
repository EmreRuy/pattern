package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.FrequencyType
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.notifications.ReminderManager
import kotlinx.collections.immutable.toImmutableList
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * Staff Engineer Refactoring:
 * Centralized habit creation logic into a Use Case to ensure SSoT
 * and separation of concerns between UI state and domain logic.
 */
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager
) {
    suspend fun execute(
        name: String,
        type: String,
        emoji: String,
        motivation: String,
        selectedDays: List<DayOfWeek>,
        durationHours: Int,
        durationMinutes: Int,
        taskCount: Int,
        color: String,
        reminderEnabled: Boolean,
        reminderTime: String
    ): Long {
        val totalDurationInMinutes = if (type == "Grow") {
            (durationHours * 60) + durationMinutes
        } else {
            null
        }

        val finalTaskCount = if (type == "Task") {
            taskCount
        } else {
            null
        }

        val bitmask = selectedDays.fold(0) { acc, day ->
            acc or (1 shl (day.value - 1))
        }

        val habit = Habit(
            id = 0,
            name = name.trim(),
            type = when (type) {
                "Grow" -> HabitType.BUILD
                "Drop" -> HabitType.QUIT
                else -> HabitType.TASK
            },
            iconCode = emoji,
            durationInMinutes = totalDurationInMinutes,
            taskCount = finalTaskCount,
            selectedDays = DayOfWeek.entries.map { selectedDays.contains(it) }.toImmutableList(),
            accentColorHex = color,
            reminderTime = if (reminderEnabled) reminderTime else null,
            motivation = if (motivation.isBlank()) null else motivation.trim(),
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            startDate = LocalDate.now(),
            frequencyType = if (selectedDays.size == 7) FrequencyType.DAILY else FrequencyType.WEEKLY,
            daysOfWeekBitmask = bitmask
        )

        val id = repository.upsertHabit(habit)
        
        // Logic for side effects (scheduling reminders)
        reminderManager.scheduleReminder(habit.copy(id = id.toInt()))
        
        return id
    }
}
