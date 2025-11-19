package com.example.pattern.data.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pattern.data.local.HabitType

data class HabitCardModel(
    val id: Int,
    val name: String,
    val type: HabitType,
    val icon: ImageVector,
    val iconEmoji: String? = null,
    var isChecked: MutableState<Boolean> = mutableStateOf(false),
    var isTimeChecked: MutableState<Boolean> = mutableStateOf(false),
    val accentColorHex: String,
    val durationInMinutes: Int?,
    val timerStartTime: Long? = null,
    val timerPauseTime: Long? = null,
    val isCompleted: Boolean = false
)