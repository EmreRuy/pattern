package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.*
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.calculateStreakFromDates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Staff-engineered UseCase that consolidates all profile-related calculations into a single,
 * efficient pass over the data. This avoids redundant flow combinations and ensures
 * consistency across all profile components.
 */
class GetProfileStatsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<ProfileStats> {
        return combine(
            repository.getAllHabitsStream(),
            repository.getAllDailyStatesStream()
        ) { habits, allStates ->
            val habitMap = habits.associateBy { it.id }
            val stateMap = allStates.groupBy { it.habitId }
            val today = LocalDate.now()

            // 1. Calculate Core Metrics & Success Rates
            var totalDone = 0
            var currentTotalXp = 0
            var buildXP = 0
            var quitXP = 0
            var taskXP = 0

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
                        val xpGained = ExperienceUtils.calculateHabitXP(habit, state)
                        currentTotalXp += xpGained
                        
                        when (habit.type) {
                            HabitType.BUILD -> buildXP += xpGained
                            HabitType.QUIT -> quitXP += xpGained
                            HabitType.TASK -> taskXP += xpGained
                        }
                    }
                }

                val habitMissed = calculateMissedCount(habit, completionDates, today)
                val streakInfo = calculateStreakFromDates(habit, completionDates, today)
                
                Triple(habit, habitDone, habitMissed) to streakInfo
            }

            val totalMissed = habitStatsList.sumOf { it.first.third }
            val totalAttempts = totalDone + totalMissed
            val successRate = if (totalAttempts > 0) totalDone.toFloat() / totalAttempts else 0f

            val levelInfo = ExperienceUtils.getLevelInfo(currentTotalXp).let {
                LevelInfo(
                    level = it.level,
                    title = it.title,
                    currentXP = it.currentXP,
                    nextLevelXP = it.nextLevelXP,
                    progress = it.progress
                )
            }

            // 2. Generate Cumulative XP History (Weekly, Daily/Monthly, Yearly)
            val dailyXpGains = allStates.groupBy { it.date }.mapValues { (_, statesForDate) ->
                statesForDate.sumOf { state ->
                    val habit = habitMap[state.habitId]
                    if (habit != null) ExperienceUtils.calculateHabitXP(habit, state) else 0
                }
            }

            val weeklyXpHistory = calculateCumulativeHistory(
                dailyXpGains = dailyXpGains,
                startDate = today.minusDays(6),
                count = 7,
                isMonthly = false,
                today = today
            )

            val xpHistory = calculateCumulativeHistory(
                dailyXpGains = dailyXpGains,
                startDate = today.minusDays(29),
                count = 30,
                isMonthly = false,
                today = today
            )

            val yearlyXpHistory = calculateCumulativeHistory(
                dailyXpGains = dailyXpGains,
                startDate = today.minusMonths(11).withDayOfMonth(1),
                count = 12,
                isMonthly = true,
                today = today
            )

            // 3. Extract Rankings
            val topDone = habitStatsList
                .filter { it.first.second > 0 }
                .sortedByDescending { it.first.second }
                .take(3)
                .map { HabitStat(it.first.first.name, it.first.second, it.first.first.iconCode, it.first.first.accentColorHex) }

            val topMissed = habitStatsList
                .filter { it.first.third > 0 }
                .sortedByDescending { it.first.third }
                .take(3)
                .map { HabitStat(it.first.first.name, it.first.third, it.first.first.iconCode, it.first.first.accentColorHex) }

            val bestStreaks = habitStatsList
                .filter { it.second.longestStreak > 0 }
                .sortedByDescending { it.second.longestStreak }
                .take(3)
                .map { StreakStat(it.first.first.name, it.second.longestStreak, it.first.first.iconCode, it.first.first.accentColorHex) }

            val activeDaysAnalysis = calculateActiveDaysAnalysis(habits, allStates, today)

            ProfileStats(
                levelInfo = levelInfo,
                weeklyXpHistory = weeklyXpHistory,
                xpHistory = xpHistory,
                yearlyXpHistory = yearlyXpHistory,
                doneCount = totalDone,
                missedCount = totalMissed,
                successRate = successRate,
                totalXp = currentTotalXp,
                totalHabits = habits.size,
                topDoneHabits = topDone,
                topMissedHabits = topMissed,
                bestStreaks = bestStreaks,
                xpDistribution = XPDistribution(
                    buildXP = buildXP,
                    quitXP = quitXP,
                    taskXP = taskXP,
                    totalXP = currentTotalXp
                ),
                activeDaysAnalysis = activeDaysAnalysis
            )
        }
    }

    private fun calculateActiveDaysAnalysis(
        habits: List<Habit>,
        allStates: List<HabitDailyState>,
        today: LocalDate
    ): ActiveDaysAnalysis {
        val scheduledCounts = IntArray(7) { 0 }
        val completedCounts = IntArray(7) { 0 }
        
        val stateMap = allStates.groupBy { it.habitId to it.date }

        habits.forEach { habit ->
            val startDate = Instant.ofEpochMilli(habit.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            
            var checkDate = startDate
            while (!checkDate.isAfter(today)) {
                val dayOfWeekIndex = checkDate.dayOfWeek.value - 1 // 0 (Mon) to 6 (Sun)
                val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
                
                if (isScheduled) {
                    scheduledCounts[dayOfWeekIndex]++
                    val state = stateMap[habit.id to checkDate.toString()]?.firstOrNull()
                    val isDone = state?.let {
                        when (habit.type) {
                            HabitType.BUILD -> it.isCompleted
                            HabitType.TASK, HabitType.QUIT -> it.isTaskCompleted
                        }
                    } ?: false
                    
                    if (isDone) {
                        completedCounts[dayOfWeekIndex]++
                    }
                }
                checkDate = checkDate.plusDays(1)
            }
        }

        val dailyRates = (0..6).map { i ->
            val rate = if (scheduledCounts[i] > 0) {
                completedCounts[i].toFloat() / scheduledCounts[i]
            } else 0f
            DayCompletionRate(i + 1, rate)
        }

        // Find the worst day (minimum rate among scheduled days)
        val worstDayIndex = scheduledCounts.indices
            .filter { scheduledCounts[it] > 0 }
            .minByOrNull { i -> completedCounts[i].toFloat() / scheduledCounts[i] }

        val insight = worstDayIndex?.let { idx ->
            val rate = if (scheduledCounts[idx] > 0) completedCounts[idx].toFloat() / scheduledCounts[idx] else 1f
            if (rate < 0.9f) { // Only show insight if there's actually a dip
                val dayName = LocalDate.now().with(java.time.DayOfWeek.of(idx + 1)).format(DateTimeFormatter.ofPattern("EEEE"))
                val avgRate = dailyRates.map { it.rate }.average().toFloat()
                val diff = (avgRate - rate) * 100
                if (diff > 10) {
                    "You are ${diff.toInt()}% more likely to miss habits on ${dayName}s."
                } else null
            } else null
        }

        return ActiveDaysAnalysis(
            dailyRates = dailyRates,
            insightMessage = insight,
            worstDay = worstDayIndex?.plus(1)
        )
    }

    private fun calculateMissedCount(habit: Habit, completionDates: Set<String>, today: LocalDate): Int {
        val startDate = Instant.ofEpochMilli(habit.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        var missed = 0
        var checkDate = startDate
        while (checkDate.isBefore(today)) {
            val dayOfWeekIndex = checkDate.dayOfWeek.value - 1
            val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
            if (isScheduled && !completionDates.contains(checkDate.toString())) {
                missed++
            }
            checkDate = checkDate.plusDays(1)
        }
        return missed
    }

    private fun calculateCumulativeHistory(
        dailyXpGains: Map<String, Int>,
        startDate: LocalDate,
        count: Int,
        isMonthly: Boolean,
        today: LocalDate
    ): List<XPDataPoint> {
        val formatter = DateTimeFormatter.ofPattern(if (isMonthly) "MMM" else "MMM dd")
        
        // Calculate baseline XP earned before the window starts - optimized
        val sortedDateStrings = dailyXpGains.keys.filter { it.isNotEmpty() }.sorted()
        var runningTotal = 0f
        for (dateStr in sortedDateStrings) {
            try {
                if (LocalDate.parse(dateStr).isBefore(startDate)) {
                    runningTotal += dailyXpGains[dateStr] ?: 0
                } else {
                    break // Since it's sorted, we can stop early
                }
            } catch (_: Exception) {
                // Ignore malformed dates
            }
        }

        return List(count) { i ->
            val date = if (isMonthly) startDate.plusMonths(i.toLong()) else startDate.plusDays(i.toLong())
            
            val periodGains = if (isMonthly) {
                var monthSum = 0
                var checkDate = date
                val nextPeriod = date.plusMonths(1)
                while (checkDate.isBefore(nextPeriod) && !checkDate.isAfter(today)) {
                    monthSum += dailyXpGains[checkDate.toString()] ?: 0
                    checkDate = checkDate.plusDays(1)
                }
                monthSum
            } else {
                dailyXpGains[date.toString()] ?: 0
            }
            
            runningTotal += periodGains
            XPDataPoint(i + 1, date.format(formatter), runningTotal)
        }
    }
}
