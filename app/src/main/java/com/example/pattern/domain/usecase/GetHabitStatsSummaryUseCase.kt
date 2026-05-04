package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.*
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetHabitStatsSummaryUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<HabitStatsSummary> {
        return combine(
            repository.getAllHabitsStream(),
            repository.getAllDailyStatesStream() // Summary might still need all for "Total XP"
        ) { habits, allStates ->
            val stateMap = allStates.groupBy { it.habitId }
            val today = LocalDate.now()

            var totalDone = 0
            var totalMissed = 0
            var currentTotalXp = 0

            val habitStatsList = habits.map { habit ->
                val states = stateMap[habit.id] ?: emptyList()

                val completionDates = mutableSetOf<String>()
                var habitDone = 0
                states.forEach { state ->
                    val isDone = when (habit.type) {
                        HabitType.BUILD -> state.isCompleted
                        HabitType.TASK, HabitType.QUIT -> state.isTaskCompleted
                    }
                    if (isDone) {
                        completionDates.add(state.date)
                        habitDone++
                        totalDone++
                        currentTotalXp += ExperienceUtils.calculateHabitXP(habit, state)
                    }
                }

                val startDate = Instant.ofEpochMilli(habit.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                var habitMissed = 0
                var checkDate = startDate
                while (checkDate.isBefore(today)) {
                    val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
                    val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true

                    if (isScheduled && !completionDates.contains(checkDate.toString())) {
                        habitMissed++
                        totalMissed++
                    }
                    checkDate = checkDate.plusDays(1)
                }
                Triple(habit, habitDone, habitMissed)
            }

            val topDone = habitStatsList
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
                .take(3)
                .map { HabitStat(it.first.name, it.second, it.first.iconCode, it.first.accentColorHex) }

            val topMissed = habitStatsList
                .filter { it.third > 0 }
                .sortedByDescending { it.third }
                .take(3)
                .map { HabitStat(it.first.name, it.third, it.first.iconCode, it.first.accentColorHex) }

            val totalAttempts = totalDone + totalMissed
            val rate = if (totalAttempts > 0) totalDone.toFloat() / totalAttempts else 0f

            val levelInfoDomain = ExperienceUtils.getLevelInfo(currentTotalXp).let {
                LevelInfo(
                    level = it.level,
                    title = it.title,
                    currentXP = it.currentXP,
                    nextLevelXP = it.nextLevelXP,
                    progress = it.progress
                )
            }

            HabitStatsSummary(
                levelInfo = levelInfoDomain,
                doneCount = totalDone,
                missedCount = totalMissed,
                successRate = rate,
                totalXp = currentTotalXp,
                totalHabits = habits.size,
                topDoneHabits = topDone,
                topMissedHabits = topMissed
            )
        }
    }
}
