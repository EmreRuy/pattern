package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.streak.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use Case to retrieve and process habits for the Home Screen.
 * Optimized for high-performance single-pass processing.
 */
class GetHomeHabitsUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val streakCalculator: StreakCalculator
) {
    operator fun invoke(date: LocalDate): Flow<List<HabitWithStatus>> {
        val dateKey = date.toString()
        return combine(
            repository.getAllHabitsStream(),
            repository.getAllDailyStatesStream(),
            repository.getDailyStatesForDate(dateKey)
        ) { habits, allHistory, dailyStatesForDate ->
            val today = LocalDate.now()
            val dayOfWeekIndex = date.dayOfWeek.value - 1
            
            val dateStatesMap = dailyStatesForDate.associateBy { it.habitId }
            val historyByHabit = allHistory.groupBy { it.habitId }

            habits.mapNotNull { habit ->
                val wasCreated = !date.isBefore(habit.createdAtLocalDate)
                val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
                
                if (wasCreated && isScheduled) {
                    val history = historyByHabit[habit.id] ?: emptyList()
                    val streakInfo = streakCalculator.calculate(habit, history, today)
                    
                    HabitWithStatus(
                        habit = habit,
                        dailyState = dateStatesMap[habit.id],
                        currentStreak = streakInfo.currentStreak
                    )
                } else {
                    null
                }
            }
        }.flowOn(Dispatchers.Default)
    }
}
