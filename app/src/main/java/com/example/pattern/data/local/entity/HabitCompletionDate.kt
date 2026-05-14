package com.example.pattern.data.local.entity

/**
 * Lightweight POJO for Room to fetch only habitId and date.
 * Used for high-performance streak calculations.
 */
data class HabitCompletionDate(
    val habitId: Int,
    val date: String
)
