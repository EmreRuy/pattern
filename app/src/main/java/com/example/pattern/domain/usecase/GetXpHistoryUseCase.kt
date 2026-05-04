package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.XPDataPoint
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetXpHistoryUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<Pair<List<XPDataPoint>, List<XPDataPoint>>> {
        val twelveMonthsAgo = LocalDate.now().minusMonths(12).withDayOfMonth(1).toString()
        
        return combine(
            repository.getAllHabitsStream(),
            repository.getDailyStatesFromDateStream(twelveMonthsAgo)
        ) { habits, states ->
            val habitMap = habits.associateBy { it.id }
            
            val dailyXp = calculateRealXpHistory(habitMap, states)
            val yearlyXp = calculateYearlyXpHistory(habitMap, states)
            
            Pair(dailyXp, yearlyXp)
        }
    }

    private fun calculateRealXpHistory(
        habitMap: Map<Int, com.example.pattern.domain.model.Habit>, 
        allStates: List<com.example.pattern.domain.model.HabitDailyState>
    ): List<XPDataPoint> {
        val formatter = DateTimeFormatter.ofPattern("MMM dd")
        val today = LocalDate.now()
        val startDate = today.minusDays(29)

        val dailyGains = allStates.groupBy { it.date }
            .mapValues { (_, states) ->
                states.sumOf { state ->
                    val habit = habitMap[state.habitId]
                    if (habit != null) ExperienceUtils.calculateHabitXP(habit, state) else 0
                }
            }

        var runningTotal = 0f
        // In a real app, we might want to fetch the baseline XP (total XP before startDate) 
        // from a separate source or a single query to avoid fetching all history.
        // For now, we use the states we have.

        return List(30) { i ->
            val date = startDate.plusDays(i.toLong())
            val dateString = date.toString()
            runningTotal += (dailyGains[dateString] ?: 0)
            XPDataPoint(i + 1, date.format(formatter), runningTotal)
        }
    }

    private fun calculateYearlyXpHistory(
        habitMap: Map<Int, com.example.pattern.domain.model.Habit>, 
        allStates: List<com.example.pattern.domain.model.HabitDailyState>
    ): List<XPDataPoint> {
        val formatter = DateTimeFormatter.ofPattern("MMM")
        val today = LocalDate.now()
        val startDate = today.minusMonths(11).withDayOfMonth(1)

        val dailyGains = allStates.groupBy { it.date }
            .mapValues { (_, states) ->
                states.sumOf { state ->
                    val habit = habitMap[state.habitId]
                    if (habit != null) ExperienceUtils.calculateHabitXP(habit, state) else 0
                }
            }

        var runningTotal = 0f

        return List(12) { i ->
            val monthDate = startDate.plusMonths(i.toLong())
            var monthTotal = 0f
            var checkDate = monthDate
            val nextMonth = monthDate.plusMonths(1)
            while (checkDate.isBefore(nextMonth) && !checkDate.isAfter(today)) {
                monthTotal += (dailyGains[checkDate.toString()] ?: 0).toFloat()
                checkDate = checkDate.plusDays(1)
            }
            runningTotal += monthTotal
            XPDataPoint(i + 1, monthDate.format(formatter), runningTotal)
        }
    }
}
