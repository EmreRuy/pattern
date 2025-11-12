package com.example.pattern.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.mutableStateOf
import com.example.pattern.data.local.Habit as LocalHabit
import com.example.pattern.data.model.HabitCard as UiHabit

fun LocalHabit.toUiModel(): UiHabit {
    val icon = when (type) {
        HabitType.BUILD -> Icons.Default.Build
        HabitType.QUIT -> Icons.Default.CheckCircle
        HabitType.TASK -> Icons.Default.AddCircle
    }
    return UiHabit(
        id = this.id,
        name = this.name,
        type = this.type,
        iconEmoji = this.iconCode,
        icon = icon,
        isChecked = mutableStateOf(this.isCompleted),
        isTimeChecked = mutableStateOf(false),
        accentColorHex = accentColorHex
    )
}
