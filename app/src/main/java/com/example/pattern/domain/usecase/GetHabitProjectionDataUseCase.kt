package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.StreakInfo
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.streak.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

data class HabitProjectionData(
    val allHabits: List<Habit>,
    val streaks: Map<Int, StreakInfo>,
    val dailyStates: Map<LocalDate, Map<Int, HabitDailyState>>
)

class GetHabitProjectionDataUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val streakCalculator: StreakCalculator
) {
    operator fun invoke(): Flow<HabitProjectionData> {
        val today = LocalDate.now()
        val rangeStart = today.minusMonths(3)
        val rangeEnd = today.plusMonths(1)

        return combine(
            habitRepository.getAllHabitsStream(),
            dailyLogRepository.getCompletedDatesStream(),
            dailyLogRepository.getDailyStatesInRangeStream(rangeStart.toString(), rangeEnd.toString())
        ) { allHabits, completedDatesMap, rangeDailyStates ->
            
            val streaks = allHabits.associate { habit ->
                val completionDates = completedDatesMap[habit.id] ?: emptySet()
                val history = completionDates.map { 
                    HabitDailyState(habitId = habit.id, date = it.toString(), isCompleted = true) 
                }
                habit.id to streakCalculator.calculate(habit, history, today)
            }

            val dailyStatesMap = rangeDailyStates
                .groupBy { LocalDate.parse(it.date) }
                .mapValues { entry -> entry.value.associateBy { it.habitId } }

            HabitProjectionData(
                allHabits = allHabits,
                streaks = streaks,
                dailyStates = dailyStatesMap
            )
        }
    }
}
