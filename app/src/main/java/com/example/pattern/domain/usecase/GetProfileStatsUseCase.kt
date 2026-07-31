package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.*
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.domain.streak.StreakCalculator
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * 1. Zero-Allocation Dates: Uses Set<LocalDate> directly from the repository.
 * 2. Reference Performance: Uses pre-calculated habit.createdAtLocalDate.
 * 3. Single-pass XP distribution and ranking generation.
 * 4. Offloads all computations to Dispatchers.Default.
 */
class GetProfileStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val streakCalculator: StreakCalculator
) {
    operator fun invoke(): Flow<ProfileStats> {
        return combine(
            habitRepository.getAllHabitsStream(),
            dailyLogRepository.getAllDailyStatesStream()
        ) { habits, allHistory ->
            val today = LocalDate.now()
            val historyByHabit = allHistory.groupBy { it.habitId }
            val completedDatesMap = allHistory
                .filter { it.isCompleted }
                .groupBy { it.habitId }
                .mapValues { (_, states) -> states.map { LocalDate.parse(it.date) }.toSet() }

            // 1. XP Gains tracking - Map<LocalDate, Int>
            val dailyXpGains = mutableMapOf<LocalDate, Int>()

            // 2. Core Metrics Calculation
            var totalDone = 0
            var totalXP = 0
            var buildXP = 0
            var quitXP = 0
            var taskXP = 0

            val habitStatsList = habits.map { habit ->
                val completionDates = completedDatesMap[habit.id] ?: emptySet()
                val habitDone = completionDates.size
                totalDone += habitDone

                val habitXpPerCompletion = getXPForHabit(habit)
                val totalHabitXp = habitDone * habitXpPerCompletion
                totalXP += totalHabitXp

                when (habit.type) {
                    HabitType.BUILD -> buildXP += totalHabitXp
                    HabitType.QUIT -> quitXP += totalHabitXp
                    HabitType.TASK -> taskXP += totalHabitXp
                }

                completionDates.forEach { date ->
                    dailyXpGains[date] = (dailyXpGains[date] ?: 0) + habitXpPerCompletion
                }

                val creationDate = habit.createdAtLocalDate
                
                val totalScheduledUntilToday = countScheduledDays(creationDate, today.minusDays(1), habit.selectedDays)
                val completionsUntilYesterday = completionDates.count { it.isBefore(today) }
                val habitMissed = (totalScheduledUntilToday - completionsUntilYesterday).coerceAtLeast(0)

                val history = historyByHabit[habit.id] ?: emptyList()
                val streakInfo = streakCalculator.calculate(habit, history, today)
                
                HabitStatInternal(habit, habitDone, habitMissed, streakInfo)
            }

            val totalMissed = habitStatsList.sumOf { it.missed }
            val totalAttempts = totalDone + totalMissed
            val successRate = if (totalAttempts > 0) totalDone.toFloat() / totalAttempts else 0f

            val levelInfo = ExperienceUtils.getLevelInfo(totalXP)

            // 3. XP History (Weekly, Monthly, Yearly) - Uses pre-parsed dates
            val weeklyXpHistory = calculateCumulativeHistory(dailyXpGains, today.minusDays(6), 7, false, today)
            val xpHistory = calculateCumulativeHistory(dailyXpGains, today.minusDays(29), 30, false, today)
            val yearlyXpHistory = calculateCumulativeHistory(dailyXpGains, today.minusMonths(11).withDayOfMonth(1), 12, true, today)

            // 4. Rankings
            val topDone = habitStatsList
                .filter { it.done > 0 }
                .sortedByDescending { it.done }
                .take(3)
                .map { HabitStat(it.habit.name, it.done, it.habit.iconCode, it.habit.accentColorHex) }

            val topMissed = habitStatsList
                .filter { it.missed > 0 }
                .sortedByDescending { it.missed }
                .take(3)
                .map { HabitStat(it.habit.name, it.missed, it.habit.iconCode, it.habit.accentColorHex) }

            val bestStreaks = habitStatsList
                .filter { it.streakInfo.longestStreak > 0 }
                .sortedByDescending { it.streakInfo.longestStreak }
                .take(3)
                .map { StreakStat(it.habit.name, it.streakInfo.longestStreak, it.habit.iconCode, it.habit.accentColorHex) }

            val activeDaysAnalysis = calculateActiveDaysAnalysis(habits, completedDatesMap, today)

            ProfileStats(
                levelInfo = levelInfo,
                weeklyXpHistory = weeklyXpHistory.toImmutableList(),
                xpHistory = xpHistory.toImmutableList(),
                yearlyXpHistory = yearlyXpHistory.toImmutableList(),
                doneCount = totalDone,
                missedCount = totalMissed,
                successRate = successRate,
                totalXp = totalXP,
                totalHabits = habits.size,
                topDoneHabits = topDone.toImmutableList(),
                topMissedHabits = topMissed.toImmutableList(),
                bestStreaks = bestStreaks.toImmutableList(),
                xpDistribution = XPDistribution(
                    buildXP = buildXP,
                    quitXP = quitXP,
                    taskXP = taskXP,
                    totalXP = totalXP
                ),
                activeDaysAnalysis = activeDaysAnalysis
            )
        }.flowOn(Dispatchers.Default)
    }

    private fun getXPForHabit(habit: Habit): Int {
        return when (habit.type) {
            HabitType.TASK -> 15
            HabitType.QUIT -> 20
            HabitType.BUILD -> 10 + ((habit.durationInMinutes ?: 0) / 15) * 5
        }
    }

    private fun countScheduledDays(startDate: LocalDate, endDate: LocalDate, selectedDays: List<Boolean>): Int {
        if (startDate.isAfter(endDate)) return 0
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()
        val fullWeeks = daysBetween / 7
        val remainingDays = daysBetween % 7
        
        var count = fullWeeks * selectedDays.count { it }
        for (i in 0 until remainingDays) {
            val dayIdx = startDate.plusDays(i.toLong()).dayOfWeek.value - 1
            if (selectedDays.getOrNull(dayIdx) == true) count++
        }
        return count
    }

    private fun calculateActiveDaysAnalysis(
        habits: List<Habit>,
        completedDatesMap: Map<Int, Set<LocalDate>>,
        today: LocalDate
    ): ActiveDaysAnalysis {
        val scheduledCounts = IntArray(7)
        val completedCounts = IntArray(7)
        
        habits.forEach { habit ->
            val startDate = habit.createdAtLocalDate
            
            for (dayOfWeek in 1..7) {
                val dayIdx = dayOfWeek - 1
                if (habit.selectedDays.getOrNull(dayIdx) == true) {
                    scheduledCounts[dayIdx] += countOccurrencesOfDayOfWeek(startDate, today, dayOfWeek)
                }
            }

            completedDatesMap[habit.id]?.forEach { date ->
                if (!date.isAfter(today)) {
                    completedCounts[date.dayOfWeek.value - 1]++
                }
            }
        }

        val dailyRates = (0..6).map { i ->
            val rate = if (scheduledCounts[i] > 0) {
                completedCounts[i].toFloat() / scheduledCounts[i]
            } else 0f
            DayCompletionRate(i + 1, rate)
        }

        val worstDayIndex = scheduledCounts.indices
            .filter { scheduledCounts[it] > 0 }
            .minByOrNull { i -> completedCounts[i].toFloat() / scheduledCounts[i] }

        val insight = worstDayIndex?.let { idx ->
            val rate = if (scheduledCounts[idx] > 0) completedCounts[idx].toFloat() / scheduledCounts[idx] else 1f
            if (rate < 0.9f) {
                val dayName = today.with(java.time.DayOfWeek.of(idx + 1)).format(DateTimeFormatter.ofPattern("EEEE"))
                val avgRate = dailyRates.map { it.rate }.average().toFloat()
                val diff = (avgRate - rate) * 100
                if (diff > 10) "You are ${diff.toInt()}% more likely to miss habits on ${dayName}s." else null
            } else null
        }

        return ActiveDaysAnalysis(dailyRates.toImmutableList(), insight, worstDayIndex?.plus(1))
    }

    private fun countOccurrencesOfDayOfWeek(start: LocalDate, end: LocalDate, dayOfWeek: Int): Int {
        if (start.isAfter(end)) return 0
        val daysBetween = ChronoUnit.DAYS.between(start, end.plusDays(1)).toInt()
        val fullWeeks = daysBetween / 7
        val remainingDays = daysBetween % 7
        var count = fullWeeks
        for (i in 0 until remainingDays) {
            if (start.plusDays(i.toLong()).dayOfWeek.value == dayOfWeek) count++
        }
        return count
    }

    private data class HabitStatInternal(
        val habit: Habit,
        val done: Int,
        val missed: Int,
        val streakInfo: StreakInfo
    )

    private fun calculateCumulativeHistory(
        dailyXpGains: Map<LocalDate, Int>,
        startDate: LocalDate,
        count: Int,
        isMonthly: Boolean,
        today: LocalDate
    ): List<XPDataPoint> {
        val formatter = DateTimeFormatter.ofPattern(if (isMonthly) "MMM" else "MMM dd")
        
        // Calculate baseline XP earned before the window starts - optimized O(history)
        var runningTotal = 0f
        dailyXpGains.forEach { (date, xp) ->
            if (date.isBefore(startDate)) {
                runningTotal += xp
            }
        }

        return List(count) { i ->
            val date = if (isMonthly) startDate.plusMonths(i.toLong()) else startDate.plusDays(i.toLong())
            
            val periodGains = if (isMonthly) {
                var monthSum = 0
                val nextPeriod = date.plusMonths(1)
                dailyXpGains.forEach { (d, xp) ->
                    if (!d.isBefore(date) && d.isBefore(nextPeriod) && !d.isAfter(today)) {
                        monthSum += xp
                    }
                }
                monthSum
            } else {
                dailyXpGains[date] ?: 0
            }
            
            runningTotal += periodGains
            XPDataPoint(i + 1, date.format(formatter), runningTotal)
        }
    }
}
