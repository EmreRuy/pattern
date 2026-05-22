package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Staff Engineer Refactoring:
 * Clean, immutable domain model for a Habit.
 * Optimized with pre-calculated LocalDate to avoid repeated parsing in performance-critical loops.
 * 
 * 1. Moved timer state to HabitDailyState (Single Source of Truth).
 * 2. Switched to ImmutableList for enhanced Compose stability.
 */
@Immutable
data class Habit(
    val id: Int,
    val name: String,
    val type: HabitType,
    val durationInMinutes: Int?,
    val selectedDays: ImmutableList<Boolean>,
    val iconCode: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val accentColorHex: String,
    val reminderTime: String?,
    val motivation: String?,
    val taskCount: Int? = null
) {
    /** 
     * Pre-calculated LocalDate for streak calculations.
     * Prevents expensive Instant -> LocalDate conversions in lists.
     */
    val createdAtLocalDate: LocalDate by lazy {
        Instant.ofEpochMilli(createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
