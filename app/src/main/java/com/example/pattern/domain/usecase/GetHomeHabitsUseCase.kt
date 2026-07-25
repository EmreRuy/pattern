package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.streak.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use Case to retrieve and process habits for the Home Screen window.
 * Optimized for O(1) status lookup across a date range.
 */
class GetHomeHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val streakCalculator: StreakCalculator
) {
    operator fun invoke(centerDate: LocalDate): Flow<Map<LocalDate, List<HabitWithStatus>>> {
        val today = LocalDate.now()
        
        // Window: +/- 7 days around the center date
        val startDate = centerDate.minusDays(7)
        val dateRange = (0..14).map { startDate.plusDays(it.toLong()) }

        return combine(
            habitRepository.getAllHabitsStream(),
            dailyLogRepository.getDailyStatesFromDateStream(startDate.toString()),
            dailyLogRepository.getCompletedDatesStream()
        ) { allHabits, statesInRange, completedDatesMap ->
            
            val statesByDate = statesInRange.groupBy { it.date }
            
            // Pre-calculate history for all habits to avoid redundant allocations in the loop
            val historiesByHabit = allHabits.associate { habit ->
                val completionDates = completedDatesMap[habit.id] ?: emptySet()
                habit.id to completionDates.map { 
                    HabitDailyState(habitId = habit.id, date = it.toString(), isCompleted = true) 
                }
            }
            
            dateRange.associateWith { date ->
                val dateStr = date.toString()
                val dayOfWeekIndex = date.dayOfWeek.value - 1
                val dailyStatesForDate = statesByDate[dateStr]?.associateBy { it.habitId } ?: emptyMap()
                val isFuture = date.isAfter(today)

                allHabits.mapNotNull { habit ->
                    val wasCreated = !date.isBefore(habit.createdAtLocalDate)
                    val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
                    
                    if (wasCreated && isScheduled) {
                        // Calculate streak for all window days to prevent "pop-in" lag.
                        // However, per user request, we hide the flame icon (streak = 0) for future dates.
                        val streak = if (!isFuture) {
                            val history = historiesByHabit[habit.id] ?: emptyList()
                            streakCalculator.calculate(habit, history, today).currentStreak
                        } else 0
                        
                        HabitWithStatus(
                            habit = habit,
                            dailyState = dailyStatesForDate[habit.id],
                            currentStreak = streak
                        )
                    } else {
                        null
                    }
                }
            }
        }.flowOn(Dispatchers.Default)
    }
}
