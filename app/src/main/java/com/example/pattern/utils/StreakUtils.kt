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
 * Optimized version that only calculates the current streak.
 * Stops scanning as soon as the streak is broken, making it much faster for Home Screen.
 */
fun calculateCurrentStreak(
    habit: Habit,
    completedDateStrings: Set<String>,
    today: LocalDate = LocalDate.now()
): Int {
    val creationDate = Instant.ofEpochMilli(habit.createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    if (completedDateStrings.isEmpty()) return 0

    var currentStreak = 0
    var checkDate = today
    val todayStr = today.toString()

    // If not completed today, the streak might still be alive if it was completed yesterday
    // or if it's not scheduled for today.
    if (!completedDateStrings.contains(todayStr)) {
        val dayOfWeekIndex = today.dayOfWeek.value - 1
        val isScheduledToday = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
        if (isScheduledToday) {
            // Streak broken today if it was scheduled and not done
            checkDate = today.minusDays(1)
        } else {
            // Not scheduled today, streak continues from yesterday
            checkDate = today.minusDays(1)
        }
    }

    while (!checkDate.isBefore(creationDate)) {
        val checkDateStr = checkDate.toString()
        val isCompleted = completedDateStrings.contains(checkDateStr)
        
        if (isCompleted) {
            currentStreak++
        } else {
            val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
            val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
            if (isScheduled) break // Streak broken
        }
        checkDate = checkDate.minusDays(1)
    }

    return currentStreak
}

/**
 * Optimized version that accepts pre-calculated completed date strings.
 * Uses a single-pass algorithm for longest streak and a reverse-scan for current streak.
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

    // 1. Calculate Current Streak (Reverse scan for O(current_streak) performance)
    var currentStreak = 0
    var checkDate = today
    val todayStr = today.toString()

    // If not completed today, check if it was even scheduled
    if (!completedDateStrings.contains(todayStr)) {
        val dayOfWeekIndex = today.dayOfWeek.value - 1
        val isScheduledToday = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
        if (isScheduledToday) {
            // Scheduled but not done today: current streak is broken
            checkDate = today.minusDays(1) // Start checking from yesterday for historical streaks, but current is 0
        } else {
            // Not scheduled today: streak remains alive from yesterday
            checkDate = today.minusDays(1)
        }
    }

    // Only count current streak if it's actually alive today or was alive yesterday (and not broken today)
    val isBrokenToday = !completedDateStrings.contains(todayStr) && 
                       habit.selectedDays.getOrNull(today.dayOfWeek.value - 1) == true
    
    if (!isBrokenToday) {
        var tempCheckDate = checkDate
        while (!tempCheckDate.isBefore(creationDate)) {
            val checkDateStr = tempCheckDate.toString()
            if (completedDateStrings.contains(checkDateStr)) {
                currentStreak++
            } else {
                val dayOfWeekIndex = tempCheckDate.dayOfWeek.value - 1
                if (habit.selectedDays.getOrNull(dayOfWeekIndex) == true) break // Streak broken
            }
            tempCheckDate = tempCheckDate.minusDays(1)
        }
    }

    // 2. Calculate Longest Streak (Forward scan O(D))
    var longestStreak = 0
    var runningStreak = 0
    var scanDate = creationDate
    
    while (!scanDate.isAfter(today)) {
        val scanDateStr = scanDate.toString()
        val isCompleted = completedDateStrings.contains(scanDateStr)
        
        if (isCompleted) {
            runningStreak++
            if (runningStreak > longestStreak) longestStreak = runningStreak
        } else {
            val dayOfWeekIndex = scanDate.dayOfWeek.value - 1
            if (habit.selectedDays.getOrNull(dayOfWeekIndex) == true) {
                runningStreak = 0
            }
        }
        scanDate = scanDate.plusDays(1)
    }

    return StreakInfo(currentStreak, longestStreak, totalCompletions)
}

