package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Staff Engineer Refactoring:
 * Clean, immutable domain model for a Habit.
 * Optimized with pre-calculated LocalDate to avoid repeated parsing in performance-critical loops.
 */
@Immutable
data class Habit(
    val id: Int,
    val name: String,
    val type: HabitType,
    val durationInMinutes: Int?,
    val selectedDays: List<Boolean>,
    val iconCode: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val accentColorHex: String,
    val accumulatedTimeMs: Long = 0L,
    val activeSessionStartMs: Long? = null,
    val reminderTime: String?,
    val motivation: String?
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

    val isTimerRunning: Boolean get() = activeSessionStartMs != null

    fun calculateTotalTimeMs(now: Long = System.currentTimeMillis()): Long {
        val currentSession = if (activeSessionStartMs != null) (now - activeSessionStartMs) else 0L
        return (accumulatedTimeMs + currentSession).coerceAtLeast(0L)
    }
}
