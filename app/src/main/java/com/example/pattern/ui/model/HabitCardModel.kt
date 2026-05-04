package com.example.pattern.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pattern.domain.model.HabitType

@Immutable
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
