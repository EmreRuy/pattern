package com.example.pattern.data.mapper

import com.example.pattern.data.local.entity.Habit as LocalHabit
import com.example.pattern.data.local.entity.HabitDailyState as LocalDailyState
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.local.entity.HabitType as LocalHabitType
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.Settings

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
    selectedDays = selectedDays,
    iconCode = iconCode,
    isCompleted = isCompleted,
    createdAt = createdAt,
    accentColorHex = accentColorHex,
    timerStartTime = timerStartTime,
    timerPauseTime = timerPauseTime,
    reminderTime = reminderTime,
    motivation = motivation
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
    timerStartTime = timerStartTime,
    timerPauseTime = timerPauseTime,
    reminderTime = reminderTime,
    motivation = motivation
)

fun LocalDailyState.toDomain(): HabitDailyState = HabitDailyState(
    habitId = habitId,
    date = date,
    timerStartTime = timerStartTime,
    timerPauseTime = timerPauseTime,
    isCompleted = isCompleted,
    isTaskCompleted = isTaskCompleted
)

fun HabitDailyState.toLocal(): LocalDailyState = LocalDailyState(
    habitId = habitId,
    date = date,
    timerStartTime = timerStartTime,
    timerPauseTime = timerPauseTime,
    isCompleted = isCompleted,
    isTaskCompleted = isTaskCompleted
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
