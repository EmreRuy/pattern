package com.example.pattern.domain.model

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val isAtRisk: Boolean = false,
    val isCompletedToday: Boolean = false
)
