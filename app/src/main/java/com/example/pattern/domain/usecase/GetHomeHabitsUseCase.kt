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

/**
 * Use Case to retrieve and process habits for the Home Screen.
 * Optimized for single-pass processing and off-main-thread execution.
 */
class GetHomeHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<HabitWithStatus>> {
        return combine(
            repository.getAllHabitsStream(),
            repository.getAllDailyStatesStream()
        ) { habits, allStates ->
            val dateKey = date.toString()
            val today = LocalDate.now()
            val dayOfWeekIndex = date.dayOfWeek.value - 1
            
            // Single-pass: Group states by habitId and filter for current date
            val statesMap = mutableMapOf<Int, MutableSet<String>>()
            val dateStatesMap = mutableMapOf<Int, HabitDailyState>()
            
            for (state in allStates) {
                if (state.isCompleted || state.isTaskCompleted) {
                    statesMap.getOrPut(state.habitId) { mutableSetOf() }.add(state.date)
                }
                if (state.date == dateKey) {
                    dateStatesMap[state.habitId] = state
                }
            }

            habits.mapNotNull { habit ->
                val creationDate = Instant.ofEpochMilli(habit.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                val wasCreated = !date.isBefore(creationDate)
                val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
                
                if (wasCreated && isScheduled) {
                    val completedDates = statesMap[habit.id] ?: emptySet()
                    
                    val streakInfo = calculateStreakFromDates(habit, completedDates, today)
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
