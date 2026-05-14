package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.*
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.calculateStreakFromDates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Staff-engineered UseCase optimized for performance and scalability.
 * 
 * Key optimizations:
 * 1. Uses getCompletedDatesStream() to minimize data transfer from SQLite.
 * 2. O(1) mathematical calculation for scheduled/missed days instead of O(N) loops.
 * 3. Single-pass XP distribution and ranking generation.
 * 4. Offloads all computations to Dispatchers.Default.
 */
class GetProfileStatsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<ProfileStats> {
        return combine(
            repository.getAllHabitsStream(),
            repository.getCompletedDatesStream()
        ) { habits, completedDatesMap ->
            val today = LocalDate.now()

            // 1. Core Metrics Calculation
            var totalDone = 0
            var totalXP = 0
            var buildXP = 0
            var quitXP = 0
            var taskXP = 0
            val dailyXpGains = mutableMapOf<String, Int>()

            val habitStatsList = habits.map { habit ->
                val completionDates = completedDatesMap[habit.id] ?: emptySet()
                val habitDone = completionDates.size
                totalDone += habitDone

                // Calculate XP for this habit
                val habitXpPerCompletion = getXPForHabit(habit)
                val totalHabitXp = habitDone * habitXpPerCompletion
                totalXP += totalHabitXp

                when (habit.type) {
                    HabitType.BUILD -> buildXP += totalHabitXp
                    HabitType.QUIT -> quitXP += totalHabitXp
                    HabitType.TASK -> taskXP += totalHabitXp
                }

                // Update daily XP gains for history charts
                completionDates.forEach { date ->
                    dailyXpGains[date] = (dailyXpGains[date] ?: 0) + habitXpPerCompletion
                }

                val creationDate = Instant.ofEpochMilli(habit.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                // Optimized missed count calculation
                val totalScheduledUntilToday = countScheduledDays(creationDate, today.minusDays(1), habit.selectedDays)
                val completionsUntilYesterday = completionDates.count { LocalDate.parse(it).isBefore(today) }
                val habitMissed = (totalScheduledUntilToday - completionsUntilYesterday).coerceAtLeast(0)

                val streakInfo = calculateStreakFromDates(habit, completionDates, today)
                
                Triple(habit, habitDone, habitMissed) to streakInfo
            }

            val totalMissed = habitStatsList.sumOf { it.first.third }
            val totalAttempts = totalDone + totalMissed
            val successRate = if (totalAttempts > 0) totalDone.toFloat() / totalAttempts else 0f

            val levelInfo = ExperienceUtils.getLevelInfo(totalXP)

            // 2. XP History (Weekly, Monthly, Yearly)
            val weeklyXpHistory = calculateCumulativeHistory(dailyXpGains, today.minusDays(6), 7, false, today)
            val xpHistory = calculateCumulativeHistory(dailyXpGains, today.minusDays(29), 30, false, today)
            val yearlyXpHistory = calculateCumulativeHistory(dailyXpGains, today.minusMonths(11).withDayOfMonth(1), 12, true, today)

            // 3. Rankings
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

            val activeDaysAnalysis = calculateActiveDaysAnalysis(habits, completedDatesMap, today)

            ProfileStats(
                levelInfo = levelInfo,
                weeklyXpHistory = weeklyXpHistory,
                xpHistory = xpHistory,
                yearlyXpHistory = yearlyXpHistory,
                doneCount = totalDone,
                missedCount = totalMissed,
                successRate = successRate,
                totalXp = totalXP,
                totalHabits = habits.size,
                topDoneHabits = topDone,
                topMissedHabits = topMissed,
                bestStreaks = bestStreaks,
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
        completedDatesMap: Map<Int, Set<String>>,
        today: LocalDate
    ): ActiveDaysAnalysis {
        val scheduledCounts = IntArray(7) { 0 }
        val completedCounts = IntArray(7) { 0 }
        
        habits.forEach { habit ->
            val startDate = Instant.ofEpochMilli(habit.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            
            // Still some O(D) here for active days analysis, but minimized
            // Optimization: iterate over selected days instead of all days
            for (dayOfWeek in 1..7) {
                val dayIdx = dayOfWeek - 1
                if (habit.selectedDays.getOrNull(dayIdx) == true) {
                    val scheduledOnThisDay = countOccurrencesOfDayOfWeek(startDate, today, dayOfWeek)
                    scheduledCounts[dayIdx] += scheduledOnThisDay
                }
            }

            completedDatesMap[habit.id]?.forEach { dateStr ->
                try {
                    val date = LocalDate.parse(dateStr)
                    if (!date.isAfter(today)) {
                        completedCounts[date.dayOfWeek.value - 1]++
                    }
                } catch (_: Exception) {}
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

        return ActiveDaysAnalysis(dailyRates, insight, worstDayIndex?.plus(1))
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

    private fun calculateCumulativeHistory(
        dailyXpGains: Map<String, Int>,
        startDate: LocalDate,
        count: Int,
        isMonthly: Boolean,
        today: LocalDate
    ): List<XPDataPoint> {
        val formatter = DateTimeFormatter.ofPattern(if (isMonthly) "MMM" else "MMM dd")
        
        // Calculate baseline XP earned before the window starts - optimized O(history)
        var runningTotal = 0f
        dailyXpGains.forEach { (dateStr, xp) ->
            try {
                if (LocalDate.parse(dateStr).isBefore(startDate)) {
                    runningTotal += xp
                }
            } catch (_: Exception) {}
        }

        return List(count) { i ->
            val date = if (isMonthly) startDate.plusMonths(i.toLong()) else startDate.plusDays(i.toLong())
            
            val periodGains = if (isMonthly) {
                var monthSum = 0
                val nextPeriod = date.plusMonths(1)
                // Filter and sum for month
                dailyXpGains.forEach { (dateStr, xp) ->
                    try {
                        val d = LocalDate.parse(dateStr)
                        if (!d.isBefore(date) && d.isBefore(nextPeriod) && !d.isAfter(today)) {
                            monthSum += xp
                        }
                    } catch (_: Exception) {}
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
