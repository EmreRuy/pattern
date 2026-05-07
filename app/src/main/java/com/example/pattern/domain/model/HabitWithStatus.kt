package com.example.pattern.domain.model

/**
 * Domain model representing a habit with its status for a specific date.
 */
data class HabitWithStatus(
    val habit: Habit,
    val dailyState: HabitDailyState?,
    val currentStreak: Int
)
