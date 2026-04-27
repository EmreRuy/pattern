package com.example.pattern.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pattern.data.local.entity.HabitType

data class HabitCardModel(
    val id: Int,
    val name: String,
    val type: HabitType,
    val icon: ImageVector,
    val iconEmoji: String? = null,
    val isTaskChecked: Boolean = false,
    val accentColorHex: String,
    val durationInMinutes: Int?,
    val timerStartTime: Long? = null,
    val timerPauseTime: Long? = null,
    val isCompleted: Boolean = false,
    val currentStreak: Int = 0
)
