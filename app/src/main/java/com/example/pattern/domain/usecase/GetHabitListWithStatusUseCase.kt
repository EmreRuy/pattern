package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class HabitStatusModel(
    val habit: Habit,
    val isCompleted: Boolean,
    val currentXP: Int,
    val maxXP: Int
)

@Immutable
data class HabitSummary(
    val total: Int = 0,
    val completed: Int = 0,
    val dailyXP: Int = 0
)

class GetHabitListWithStatusUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val dailyLogRepository: DailyLogRepository
) {
    operator fun invoke(date: LocalDate): Flow<ImmutableList<HabitStatusModel>> {
        return combine(
            habitRepository.getAllHabitsStream(),
            dailyLogRepository.getDailyStatesForDate(date.toString())
        ) { allHabits, todayStates ->
            val statesByHabit = todayStates.associateBy { it.habitId }
            val dateStr = date.toString()

            allHabits.map { habit ->
                val todayState = statesByHabit[habit.id] ?: HabitDailyState(habitId = habit.id, date = dateStr)
                
                val isCompleted = todayState.isCompleted

                val currentXP = ExperienceUtils.calculateHabitXP(habit, todayState)
                val maxXP = ExperienceUtils.calculateMaxXP(habit)
                
                HabitStatusModel(
                    habit = habit,
                    isCompleted = isCompleted,
                    currentXP = currentXP,
                    maxXP = maxXP
                )
            }.toImmutableList()
        }
    }
}
