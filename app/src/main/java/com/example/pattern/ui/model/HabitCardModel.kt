package com.example.pattern.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pattern.domain.model.HabitType
import kotlinx.collections.immutable.ImmutableList

/**
 * Staff Engineer Refactoring:
 * UI model for habit cards. Optimized for Compose performance using @Immutable.
 */
@Immutable
data class HabitCardModel(
    val id: Int,
    val name: String,
    val type: HabitType,
    val icon: ImageVector,
    val iconEmoji: String? = null,
    val isTaskChecked: Boolean = false,
    val completedCount: Int = 0,
    val accentColorHex: String,
    val durationInMinutes: Int?,
    val taskCount: Int? = null,
    val accumulatedTimeMs: Long = 0L,
    val activeSessionStartMs: Long? = null,
    val isCompleted: Boolean = false,
    val currentStreak: Int = 0
) {
    val isTimerRunning: Boolean get() = activeSessionStartMs != null
    
    fun calculateTotalTimeMs(now: Long = System.currentTimeMillis()): Long {
        val currentSession = if (activeSessionStartMs != null) (now - activeSessionStartMs).coerceAtLeast(0L) else 0L
        return (accumulatedTimeMs + currentSession).coerceAtLeast(0L)
    }
}
