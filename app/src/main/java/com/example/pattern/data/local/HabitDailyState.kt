package com.example.pattern.data.local

import androidx.room.Entity

@Entity(
    tableName = "habit_daily_state",
    primaryKeys = ["habitId", "date"]
)
data class HabitDailyState(
    val habitId: Int,
    val date: String,              // e.g. "2025-11-19" (LocalDate.toString())
    val timerStartTime: Long? = null,
    val timerPauseTime: Long? = null,
    val isCompleted: Boolean = false
)