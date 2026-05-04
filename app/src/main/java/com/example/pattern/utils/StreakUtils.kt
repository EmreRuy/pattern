package com.example.pattern.utils

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StreakInfo(
    val currentStreak: Int,
    val totalCompletions: Int
)

/**
 * Calculates streak information with senior-level precision.
 * 
 * Performance: O(N) where N is the number of days since habit creation.
 * Optimized with Set lookups for completion dates.
 */
fun calculateStreak(
    habit: Habit,
    dailyStates: List<HabitDailyState>,
    today: LocalDate = LocalDate.now()
): StreakInfo {
    val completedDateStrings = dailyStates
        .filter { it.isCompleted || it.isTaskCompleted }
        .map { it.date }
        .toSet()
    
    return calculateStreakFromDates(habit, completedDateStrings, today)
}

/**
 * Optimized version that accepts pre-calculated completed date strings.
 */
fun calculateStreakFromDates(
    habit: Habit,
    completedDateStrings: Set<String>,
    today: LocalDate = LocalDate.now(),
    totalCompletions: Int = completedDateStrings.size
): StreakInfo {
    val creationDate = Instant.ofEpochMilli(habit.createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    if (completedDateStrings.isEmpty()) {
        return StreakInfo(0, 0)
    }

    var currentStreak = 0
    var checkDate = today
    val todayStr = today.toString()

    // If today is not completed, we start checking from yesterday.
    if (!completedDateStrings.contains(todayStr)) {
        checkDate = today.minusDays(1)
    }

    // Backtrack until we reach the creation date or a break in the streak
    while (!checkDate.isBefore(creationDate)) {
        val checkDateStr = checkDate.toString()
        val isCompleted = completedDateStrings.contains(checkDateStr)
        
        if (isCompleted) {
            currentStreak++
        } else {
            val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
            val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
            if (isScheduled) {
                // Gap in a scheduled day after creation - streak broken
                break
            }
        }
        
        checkDate = checkDate.minusDays(1)
        
        // Safety bounds
        if (currentStreak > 10000) break
    }

    return StreakInfo(currentStreak, totalCompletions)
}
