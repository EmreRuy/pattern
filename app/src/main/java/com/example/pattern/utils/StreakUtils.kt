package com.example.pattern.utils

import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
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
    // 1. Determine habit creation date to bound the search
    val creationDate = Instant.ofEpochMilli(habit.createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    // 2. Extract completed dates for O(1) lookups
    val completedDates = dailyStates
        .filter { it.isCompleted || it.isTaskCompleted }
        .map { LocalDate.parse(it.date) }
        .toSet()

    if (completedDates.isEmpty()) {
        return StreakInfo(0, 0)
    }

    val totalCompletions = completedDates.size
    
    var currentStreak = 0
    var checkDate = today

    // 3. If today is not completed, we start checking from yesterday.
    // This correctly handles the case where the user hasn't completed the habit yet today.
    if (!completedDates.contains(today)) {
        checkDate = today.minusDays(1)
    }

    // 4. Backtrack until we reach the creation date or a break in the streak
    // A streak is broken if a scheduled day is missed.
    // A streak is PRESERVED if a non-scheduled day is missed.
    while (!checkDate.isBefore(creationDate)) {
        val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
        val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
        val isCompleted = completedDates.contains(checkDate)
        
        if (isCompleted) {
            currentStreak++
        } else if (isScheduled) {
            // Gap in a scheduled day after creation - streak broken
            break
        }
        // Not completed and not scheduled - streak is preserved (skipping day)
        
        checkDate = checkDate.minusDays(1)
        
        // Safety bounds
        if (checkDate.isBefore(LocalDate.ofEpochDay(0))) break 
        if (currentStreak > 10000) break
    }

    return StreakInfo(currentStreak, totalCompletions)
}
