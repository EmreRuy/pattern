package com.example.pattern.utils

import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import java.time.LocalDate

data class StreakInfo(
    val currentStreak: Int,
    val totalCompletions: Int
)

fun calculateStreak(habit: Habit, dailyStates: List<HabitDailyState>): StreakInfo {
    val completedDates = dailyStates
        .filter { it.isCompleted || it.isTaskCompleted }
        .map { LocalDate.parse(it.date) }
        .toSet()

    if (completedDates.isEmpty()) {
        return StreakInfo(0, 0)
    }

    val totalCompletions = completedDates.size
    
    val today = LocalDate.now()
    var currentStreak = 0
    var checkDate = today

    // If today is not completed, we start checking if the streak is still alive from yesterday.
    // This handles both scheduled and non-scheduled days.
    if (!completedDates.contains(today)) {
        checkDate = today.minusDays(1)
    }

    // Now go backwards
    while (true) {
        val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
        val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
        val isCompleted = completedDates.contains(checkDate)
        
        if (isCompleted) {
            currentStreak++
        } else if (isScheduled) {
            // Gap in a scheduled day - streak broken
            break
        } else {
            // Not completed and not scheduled - keep looking back (streak is preserved)
        }
        
        checkDate = checkDate.minusDays(1)
        
        // Safety break for very old habits
        if (checkDate.isBefore(LocalDate.ofEpochDay(0))) break 
        if (currentStreak > 10000) break
    }

    return StreakInfo(currentStreak, totalCompletions)
}
