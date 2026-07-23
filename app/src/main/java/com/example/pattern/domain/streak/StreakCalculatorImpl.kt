package com.example.pattern.domain.streak

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.StreakInfo
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakCalculatorImpl @Inject constructor() : StreakCalculator {

    override fun calculate(
        habit: Habit,
        history: List<HabitDailyState>,
        today: LocalDate
    ): StreakInfo {
        if (history.isEmpty()) return StreakInfo(0, 0, 0)

        val completedEpochs = history
            .filter { it.isCompleted || it.isTaskCompleted }
            .map { LocalDate.parse(it.date).toEpochDay() }
            .toSet()

        val creationEpoch = habit.createdAtLocalDate.toEpochDay()
        val todayEpoch = today.toEpochDay()

        // 1. Core State
        val isCompletedToday = completedEpochs.contains(todayEpoch)
        val isScheduledToday = habit.selectedDays[today.dayOfWeek.value - 1]
        
        // 2. Calculate Current Streak
        // We start scanning from "effectiveToday"
        // If it's completed today, we start from today.
        // If not completed today but scheduled, today is a "grace period" (streak hasn't broken yet).
        // If not completed today and NOT scheduled, we start from yesterday.
        val currentStreak = calculateCurrentStreak(
            completedEpochs, 
            creationEpoch, 
            todayEpoch, 
            isCompletedToday, 
            habit.selectedDays
        )

        // 3. Calculate Longest Streak (Forward scan)
        val longestStreak = calculateLongestStreak(
            habit,
            completedEpochs,
            creationEpoch,
            todayEpoch
        )

        // 4. Determine if At Risk
        // At Risk = Not completed today and today is scheduled.
        val isAtRisk = !isCompletedToday && isScheduledToday

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletions = completedEpochs.size,
            isAtRisk = isAtRisk,
            isCompletedToday = isCompletedToday
        )
    }

    private fun calculateCurrentStreak(
        completedEpochs: Set<Long>,
        creationEpoch: Long,
        todayEpoch: Long,
        isCompletedToday: Boolean,
        selectedDays: List<Boolean>
    ): Int {
        var streak = 0
        var checkEpoch = if (isCompletedToday) todayEpoch else todayEpoch - 1

        while (checkEpoch >= creationEpoch) {
            if (completedEpochs.contains(checkEpoch)) {
                streak++
            } else {
                val dayOfWeek = getDayOfWeekValue(checkEpoch)
                val isScheduled = selectedDays[dayOfWeek - 1]
                if (isScheduled) break
            }
            checkEpoch--
        }
        return streak
    }

    private fun calculateLongestStreak(
        habit: Habit,
        completedEpochs: Set<Long>,
        creationEpoch: Long,
        todayEpoch: Long
    ): Int {
        var longest = 0
        var current = 0
        
        for (epoch in creationEpoch..todayEpoch) {
            if (completedEpochs.contains(epoch)) {
                current++
                if (current > longest) longest = current
            } else {
                val dayOfWeek = getDayOfWeekValue(epoch)
                if (habit.selectedDays[dayOfWeek - 1]) {
                    current = 0
                }
            }
        }
        return longest
    }

    private fun getDayOfWeekValue(epochDay: Long): Int {
        // Thursday is 4. (0 + 3) % 7 + 1 = 4.
        return (((epochDay + 3) % 7 + 7) % 7).toInt() + 1
    }
}
