package com.example.pattern.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.usecase.HabitWithStatus
import com.example.pattern.ui.model.HabitCardModel

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
        accentColorHex = habit.accentColorHex,
        durationInMinutes = habit.durationInMinutes,
        timerStartTime = dailyState?.timerStartTime,
        timerPauseTime = dailyState?.timerPauseTime,
        isCompleted = dailyState?.isCompleted ?: false,
        currentStreak = currentStreak
    )
}
