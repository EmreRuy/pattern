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

    // If today is not a selected day and not completed, start checking from yesterday
    val todayDayOfWeek = today.dayOfWeek.value - 1 // 0-6
    val isScheduledToday = habit.selectedDays.getOrNull(todayDayOfWeek) == true
    
    if (!completedDates.contains(today) && !isScheduledToday) {
        checkDate = today.minusDays(1)
    } else if (!completedDates.contains(today) && isScheduledToday) {
        // Today is scheduled but not completed. 
        // We check if the streak is still alive from yesterday.
        checkDate = today.minusDays(1)
    }

    // Now go backwards
    while (true) {
        val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
        val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
        
        if (isScheduled) {
            if (completedDates.contains(checkDate)) {
                currentStreak++
            } else {
                // Gap in a scheduled day - streak broken
                // Exception: if we just started checking and today is scheduled but not yet completed,
                // we don't break the streak yet, but we also don't increment.
                // But the loop logic above already handled starting from yesterday if today is not completed.
                break
            }
        } else {
            // Not scheduled - just skip this day (don't break streak)
        }
        
        checkDate = checkDate.minusDays(1)
        
        // Safety break for very old habits or infinite loops (though minusDays is safe)
        if (checkDate.isBefore(LocalDate.ofEpochDay(0))) break 
        if (currentStreak > 10000) break // reasonable limit
    }

    return StreakInfo(currentStreak, totalCompletions)
}
