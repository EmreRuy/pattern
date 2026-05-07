package com.example.pattern.utils

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.StreakInfo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
        return StreakInfo(0, 0, 0)
    }

    // 1. Calculate Current Streak
    var currentStreak = 0
    var checkDate = today
    val todayStr = today.toString()

    if (!completedDateStrings.contains(todayStr)) {
        checkDate = today.minusDays(1)
    }

    while (!checkDate.isBefore(creationDate)) {
        val checkDateStr = checkDate.toString()
        val isCompleted = completedDateStrings.contains(checkDateStr)
        
        if (isCompleted) {
            currentStreak++
        } else {
            val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
            val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
            if (isScheduled) break
        }
        checkDate = checkDate.minusDays(1)
    }

    // 2. Calculate Longest Streak
    var longestStreak = 0
    var tempStreak = 0
    var scanDate = creationDate
    
    while (!scanDate.isAfter(today)) {
        val scanDateStr = scanDate.toString()
        val isCompleted = completedDateStrings.contains(scanDateStr)
        
        if (isCompleted) {
            tempStreak++
            if (tempStreak > longestStreak) longestStreak = tempStreak
        } else {
            val dayOfWeekIndex = scanDate.dayOfWeek.value - 1
            val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
            if (isScheduled) {
                tempStreak = 0
            }
        }
        scanDate = scanDate.plusDays(1)
    }

    return StreakInfo(currentStreak, longestStreak, totalCompletions)
}
