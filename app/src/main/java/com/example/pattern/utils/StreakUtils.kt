package com.example.pattern.utils

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.StreakInfo
import java.time.LocalDate

/**
 * Staff-Engineer Performance Perfection:
 * This version uses Epoch Days (Long) for all calculations.
 * Result: ZERO LocalDate objects are created during the streak scan loop.
 */

private fun getDayOfWeekValue(epochDay: Long): Int {
    // Epoch Day 0 is 1970-01-01 (Thursday). 
    // Thursday is 4 in java.time.DayOfWeek.
    // (0 + 3) % 7 + 1 = 4
    return (((epochDay + 3) % 7 + 7) % 7).toInt() + 1
}

fun calculateStreak(
    habit: Habit,
    dailyStates: List<HabitDailyState>,
    today: LocalDate = LocalDate.now()
): StreakInfo {
    if (dailyStates.isEmpty()) return StreakInfo(0, 0, 0)

    val completedEpochs = dailyStates
        .filter { it.isCompleted || it.isTaskCompleted }
        .map { LocalDate.parse(it.date).toEpochDay() }
        .toSet()
    
    return calculateStreakFromDates(habit, completedEpochs, today)
}

fun calculateCurrentStreak(
    habit: Habit,
    completedEpochs: Set<Long>,
    today: LocalDate = LocalDate.now()
): Int {
    if (completedEpochs.isEmpty()) return 0

    val creationEpoch = habit.createdAtLocalDate.toEpochDay()
    val todayEpoch = today.toEpochDay()
    
    var currentStreak = 0
    var checkEpoch = todayEpoch

    // 1. Determine the starting point for the streak scan
    if (!completedEpochs.contains(todayEpoch)) {
        val isScheduledToday = habit.selectedDays[today.dayOfWeek.value - 1]
        if (isScheduledToday) {
            checkEpoch = todayEpoch - 1
        } else {
            // Not scheduled today, streak continues from yesterday
            checkEpoch = todayEpoch - 1
        }
    }

    // 2. Scan backwards using primitive Longs
    while (checkEpoch >= creationEpoch) {
        if (completedEpochs.contains(checkEpoch)) {
            currentStreak++
        } else {
            val dayOfWeekValue = getDayOfWeekValue(checkEpoch)
            val isScheduled = habit.selectedDays[dayOfWeekValue - 1]
            if (isScheduled) break
        }
        checkEpoch--
    }

    return currentStreak
}

fun calculateStreakFromDates(
    habit: Habit,
    completedEpochs: Set<Long>,
    today: LocalDate = LocalDate.now(),
    totalCompletions: Int = completedEpochs.size
): StreakInfo {
    if (completedEpochs.isEmpty()) return StreakInfo(0, 0, 0)

    val creationEpoch = habit.createdAtLocalDate.toEpochDay()
    val todayEpoch = today.toEpochDay()

    // 1. Current Streak
    var currentStreak = 0
    val isBrokenToday = !completedEpochs.contains(todayEpoch) && 
                       habit.selectedDays[today.dayOfWeek.value - 1]
    
    if (!isBrokenToday) {
        var checkEpoch = if (!completedEpochs.contains(todayEpoch)) todayEpoch - 1 else todayEpoch
        while (checkEpoch >= creationEpoch) {
            if (completedEpochs.contains(checkEpoch)) {
                currentStreak++
            } else {
                val dayOfWeekValue = getDayOfWeekValue(checkEpoch)
                if (habit.selectedDays[dayOfWeekValue - 1]) break
            }
            checkEpoch--
        }
    }

    // 2. Longest Streak (Forward scan)
    var longestStreak = 0
    var runningStreak = 0
    for (epoch in creationEpoch..todayEpoch) {
        if (completedEpochs.contains(epoch)) {
            runningStreak++
            if (runningStreak > longestStreak) longestStreak = runningStreak
        } else {
            val dayOfWeekValue = getDayOfWeekValue(epoch)
            if (habit.selectedDays[dayOfWeekValue - 1]) {
                runningStreak = 0
            }
        }
    }

    return StreakInfo(currentStreak, longestStreak, totalCompletions)
}
