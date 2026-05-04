package com.example.pattern.domain.model

data class HabitDailyState(
    val habitId: Int,
    val date: String,
    val timerStartTime: Long? = null,
    val timerPauseTime: Long? = null,
    val isCompleted: Boolean = false,
    val isTaskCompleted: Boolean = false
)
