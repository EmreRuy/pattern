package com.example.pattern.domain.streak

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.StreakInfo
import java.time.LocalDate

/**
 * Domain-level component for calculating habit streaks with precision.
 * 
 * Logic handles:
 * - Current Streak (with grace period for today).
 * - Longest Streak (Historical peak).
 * - Total completions.
 * - Streak state identification (At Risk, Completed).
 */
interface StreakCalculator {
    fun calculate(
        habit: Habit,
        history: List<HabitDailyState>,
        today: LocalDate = LocalDate.now()
    ): StreakInfo
}
