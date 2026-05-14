package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain-level representation of a habit's state for a specific day.
 * 
 * Staff Engineer Note: 
 * We use the 'accumulatedTimeMs' + 'activeSessionStartMs' pattern here as well 
 * to ensure that the domain logic remains the single source of truth for time calculations.
 */
@Immutable
data class HabitDailyState(
    val habitId: Int,
    val date: String,
    val accumulatedTimeMs: Long = 0L,
    val activeSessionStartMs: Long? = null,
    val isCompleted: Boolean = false,
    val isTaskCompleted: Boolean = false
) {
    /** 
     * Derived property to check if a timer is currently active.
     */
    val isTimerRunning: Boolean get() = activeSessionStartMs != null

    /**
     * Helper to calculate the current total time. 
     * Doing this in the domain model prevents UI-layer logic leaks.
     */
    fun calculateTotalTimeMs(now: Long = System.currentTimeMillis()): Long {
        val currentSession = if (activeSessionStartMs != null) (now - activeSessionStartMs) else 0L
        return (accumulatedTimeMs + currentSession).coerceAtLeast(0L)
    }
}
