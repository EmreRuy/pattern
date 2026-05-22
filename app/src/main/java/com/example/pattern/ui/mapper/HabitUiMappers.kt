package com.example.pattern.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.ui.model.HabitCardModel

/**
 * Staff Engineer Refactoring:
 * Updated mapper to correctly propagate the new timer state machine properties.
 */
fun HabitWithStatus.toCardModel(): HabitCardModel {
    val icon = when (habit.type) {
        HabitType.BUILD -> Icons.Default.Build
        HabitType.QUIT -> Icons.Default.CheckCircle
        HabitType.TASK -> Icons.Default.AddCircle
    }

    return HabitCardModel(
        id = habit.id,
        name = habit.name,
        type = habit.type,
        icon = icon,
        iconEmoji = habit.iconCode,
        isTaskChecked = dailyState?.isTaskCompleted ?: false,
        completedCount = dailyState?.completedCount ?: 0,
        accentColorHex = habit.accentColorHex,
        durationInMinutes = habit.durationInMinutes,
        taskCount = habit.taskCount,
        accumulatedTimeMs = dailyState?.accumulatedTimeMs ?: 0L,
        activeSessionStartMs = dailyState?.activeSessionStartMs,
        isCompleted = dailyState?.isCompleted ?: false,
        currentStreak = currentStreak
    )
}
