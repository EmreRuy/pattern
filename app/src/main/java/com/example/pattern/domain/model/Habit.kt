package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Frequency types for habit scheduling.
 */
enum class FrequencyType {
    DAILY,     // Every day
    WEEKLY,    // Specific days of the week (using bitmask)
    INTERVAL   // Every X days
}

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
    val selectedDays: ImmutableList<Boolean>, // Legacy support or specific weekly days
    val iconCode: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val accentColorHex: String,
    val reminderTime: String?,
    val motivation: String?,
    val taskCount: Int? = null,
    
    // Recurrence Fields
    val startDate: LocalDate = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate(),
    val endDate: LocalDate? = null,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyInterval: Int = 1, // X in "Every X days"
    val daysOfWeekBitmask: Int = 127 // All 7 days by default (1111111)
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

    /**
     * Core Mathematical Recurrence Engine.
     * Evaluates if a habit is scheduled on a given date without DB access.
     */
    fun isScheduledOn(date: LocalDate): Boolean {
        // 1. Basic Date Range Guards
        if (date.isBefore(startDate)) return false
        if (endDate != null && date.isAfter(endDate)) return false

        return when (frequencyType) {
            FrequencyType.DAILY -> true
            
            FrequencyType.WEEKLY -> {
                // ISO day of week (1-7, Mon-Sun)
                val dayValue = date.dayOfWeek.value
                // Shift to 0-6 and check bitmask
                (daysOfWeekBitmask and (1 shl (dayValue - 1))) != 0
            }
            
            FrequencyType.INTERVAL -> {
                val daysBetween = ChronoUnit.DAYS.between(startDate, date)
                daysBetween % frequencyInterval == 0L
            }
        }
    }
}
