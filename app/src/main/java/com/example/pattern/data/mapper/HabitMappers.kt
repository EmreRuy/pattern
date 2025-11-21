package com.example.pattern.data.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.mutableStateOf
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.model.HabitCardModel
import com.example.pattern.data.local.entity.Habit as LocalHabit
import com.example.pattern.data.model.HabitCardModel as UiHabit

fun LocalHabit.toCardModel(daily: HabitDailyState?): UiHabit {
    val icon = when (type) {
        HabitType.BUILD -> Icons.Default.Build
        HabitType.QUIT -> Icons.Default.CheckCircle
        HabitType.TASK -> Icons.Default.AddCircle
    }

    return HabitCardModel(
        id = this.id,
        name = this.name,
        type = this.type,
        icon = icon,
        iconEmoji = this.iconCode,
        isChecked = mutableStateOf(daily?.isCompleted ?: this.isCompleted),
        isTimeChecked = mutableStateOf(false),
        accentColorHex = accentColorHex,
        durationInMinutes = this.durationInMinutes,
        timerStartTime = daily?.timerStartTime,
        timerPauseTime = daily?.timerPauseTime,
        isCompleted = daily?.isCompleted ?: false
    )
}
