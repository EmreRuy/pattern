package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.calculateStreakFromDates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

import com.example.pattern.utils.calculateCurrentStreak

/**
 * Use Case to retrieve and process habits for the Home Screen.
 * Optimized for high-performance single-pass processing.
 */
class GetHomeHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<HabitWithStatus>> {
        val dateKey = date.toString()
        return combine(
            repository.getAllHabitsStream(),
            repository.getCompletedDatesStream(),
            repository.getDailyStatesForDate(dateKey)
        ) { habits, completedDatesByHabit, dailyStatesForDate ->
            val today = LocalDate.now()
            val dayOfWeekIndex = date.dayOfWeek.value - 1
            
            val dateStatesMap = dailyStatesForDate.associateBy { it.habitId }

            habits.mapNotNull { habit ->
                val creationDate = Instant.ofEpochMilli(habit.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                val wasCreated = !date.isBefore(creationDate)
                val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
                
                if (wasCreated && isScheduled) {
                    val completedDates = completedDatesByHabit[habit.id] ?: emptySet()
                    
                    // Staff Optimization: Use calculateCurrentStreak which stops early
                    val currentStreak = calculateCurrentStreak(habit, completedDates, today)
                    
                    HabitWithStatus(
                        habit = habit,
                        dailyState = dateStatesMap[habit.id],
                        currentStreak = currentStreak
                    )
                } else {
                    null
                }
            }
        }.flowOn(Dispatchers.Default)
    }
}
