package com.example.pattern.domain.usecase

import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.mapper.toCardModel
import com.example.pattern.data.model.HabitCardModel
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.utils.calculateStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Use Case to retrieve and process habits for the Home Screen.
 * Encapsulates filtering by date and calculating streaks.
 */
class GetHomeHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<HabitCardModel>> {
        val habitsFlow = repository.getAllHabitsStream()
        val allDailyStatesFlow = repository.getAllDailyStatesStream()
        
        return combine(habitsFlow, allDailyStatesFlow) { habits, allStates ->
            val dateKey = date.toString()
            val today = LocalDate.now()
            val dayOfWeekIndex = date.dayOfWeek.value - 1
            
            val statesByHabit = allStates.groupBy { it.habitId }
            val dateStatesMap = allStates.filter { it.date == dateKey }
                .associateBy { it.habitId }

            habits.filter { habit ->
                val creationDate = Instant.ofEpochMilli(habit.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                val wasCreated = !date.isBefore(creationDate)
                val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
                
                wasCreated && isScheduled
            }.map { habit ->
                val daily = dateStatesMap[habit.id]
                val streak = calculateStreak(habit, statesByHabit[habit.id] ?: emptyList(), today).currentStreak
                habit.toCardModel(daily, streak)
            }
        }
    }
}
