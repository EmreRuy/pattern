package com.example.pattern.data.mapper

import com.example.pattern.data.local.entity.Habit as LocalHabit
import com.example.pattern.data.local.entity.HabitDailyState as LocalDailyState
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.local.entity.HabitType as LocalHabitType
import com.example.pattern.data.local.entity.HabitWithHistory as LocalHabitWithHistory
import com.example.pattern.domain.model.FrequencyType
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.HabitWithHistory
import com.example.pattern.domain.model.Settings
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate

/**
 * Staff Engineer Note:
 * This mapping layer is crucial for maintaining separation of concerns. 
 * Updated to support the new (Accumulated + ActiveStart) timer state machine.
 */

fun LocalHabitType.toDomain(): HabitType = when (this) {
    LocalHabitType.BUILD -> HabitType.BUILD
    LocalHabitType.QUIT -> HabitType.QUIT
    LocalHabitType.TASK -> HabitType.TASK
}

fun HabitType.toLocal(): LocalHabitType = when (this) {
    HabitType.BUILD -> LocalHabitType.BUILD
    HabitType.QUIT -> LocalHabitType.QUIT
    HabitType.TASK -> LocalHabitType.TASK
}

fun LocalHabit.toDomain(): Habit = Habit(
    id = id,
    name = name,
    type = type.toDomain(),
    durationInMinutes = durationInMinutes,
    selectedDays = selectedDays.toImmutableList(),
    iconCode = iconCode,
    isCompleted = isCompleted,
    createdAt = createdAt,
    accentColorHex = accentColorHex,
    reminderTime = reminderTime,
    motivation = motivation,
    taskCount = taskCount,
    startDate = LocalDate.parse(startDate),
    endDate = endDate?.let { LocalDate.parse(it) },
    frequencyType = FrequencyType.valueOf(frequencyType),
    frequencyInterval = frequencyInterval,
    daysOfWeekBitmask = daysOfWeekBitmask
)

fun Habit.toLocal(): LocalHabit = LocalHabit(
    id = id,
    name = name,
    type = type.toLocal(),
    durationInMinutes = durationInMinutes,
    selectedDays = selectedDays,
    iconCode = iconCode,
    isCompleted = isCompleted,
    createdAt = createdAt,
    accentColorHex = accentColorHex,
    reminderTime = reminderTime,
    motivation = motivation,
    taskCount = taskCount,
    startDate = startDate.toString(),
    endDate = endDate?.toString(),
    frequencyType = frequencyType.name,
    frequencyInterval = frequencyInterval,
    daysOfWeekBitmask = daysOfWeekBitmask
)

fun LocalDailyState.toDomain(): HabitDailyState = HabitDailyState(
    habitId = habitId,
    date = date,
    accumulatedTimeMs = accumulatedTimeMs,
    activeSessionStartMs = activeSessionStartMs,
    isCompleted = isCompleted,
    completedCount = completedCount
)

fun HabitDailyState.toLocal(): LocalDailyState = LocalDailyState(
    habitId = habitId,
    date = date,
    accumulatedTimeMs = accumulatedTimeMs,
    activeSessionStartMs = activeSessionStartMs,
    isCompleted = isCompleted,
    completedCount = completedCount
)

fun LocalHabitWithHistory.toDomain(): HabitWithHistory = HabitWithHistory(
    habit = habit.toDomain(),
    history = history.map { it.toDomain() }
)

fun SettingsEntity.toDomain(): Settings = Settings(
    quietHoursEnabled = quietHoursEnabled,
    startTime = startTime,
    endTime = endTime,
    totalXP = totalXP
)

fun Settings.toLocal(): SettingsEntity = SettingsEntity(
    quietHoursEnabled = quietHoursEnabled,
    startTime = startTime,
    endTime = endTime,
    totalXP = totalXP
)
