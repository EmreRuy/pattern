package com.example.pattern.ui.screens.profileScreen

import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.ui.screens.profileScreen.components.XPDataPoint
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.LevelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ProfileUiState(
    val levelInfo: LevelInfo = ExperienceUtils.getLevelInfo(0),
    val xpHistory: List<XPDataPoint> = emptyList(),
    val yearlyXpHistory: List<XPDataPoint> = emptyList(),
    val doneCount: Int = 0,
    val missedCount: Int = 0,
    val successRate: Float = 0f,
    val totalXp: Int = 0,
    val totalHabits: Int = 0,
    val isLoading: Boolean = false,
    val topDoneHabits: List<HabitStat> = emptyList(),
    val topMissedHabits: List<HabitStat> = emptyList()
)

data class HabitStat(
    val name: String,
    val count: Int,
    val iconCode: String,
    val colorHex: String
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    /**
     * The UI State is now reactively driven by the source of truth.
     * Calculations are performed by iterating over the current list of active habits
     * and checking their history against their specific schedules.
     */
    val uiState: StateFlow<ProfileUiState> = combine(
        repository.getAllHabitsStream(),
        repository.getAllDailyStatesStream()
    ) { habits, allStates ->
        val stateMap = allStates.groupBy { it.habitId }
        val today = LocalDate.now()
        
        var totalDone = 0
        var totalMissed = 0
        var currentTotalXp = 0

        val habitStatsList = habits.map { habit ->
            val states = stateMap[habit.id] ?: emptyList()
            
            // 1. Calculate Done and Total XP from existing states
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
            
            // 2. Calculate Missed days (scheduled but not completed)
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
        
        ProfileUiState(
            levelInfo = ExperienceUtils.getLevelInfo(currentTotalXp),
            xpHistory = calculateRealXpHistory(habits, allStates),
            yearlyXpHistory = calculateYearlyXpHistory(habits, allStates),
            doneCount = totalDone,
            missedCount = totalMissed,
            successRate = rate,
            totalXp = currentTotalXp,
            totalHabits = habits.size,
            topDoneHabits = topDone,
            topMissedHabits = topMissed,
            isLoading = false
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )

    private fun calculateRealXpHistory(habits: List<Habit>, allStates: List<HabitDailyState>): List<XPDataPoint> {
        val formatter = DateTimeFormatter.ofPattern("MMM dd")
        val today = LocalDate.now()
        val habitMap = habits.associateBy { it.id }
        
        val dailyGains = allStates.groupBy { it.date }
            .mapValues { (_, states) ->
                states.sumOf { state ->
                    val habit = habitMap[state.habitId]
                    if (habit != null) ExperienceUtils.calculateHabitXP(habit, state) else 0
                }
            }

        val startDate = today.minusDays(29)
        var runningTotal = 0f
        
        // Baseline XP from currently active habits only
        allStates.forEach { state ->
            val habit = habitMap[state.habitId]
            if (habit != null) {
                val stateDate = try { LocalDate.parse(state.date) } catch (_: Exception) { null }
                if (stateDate != null && stateDate.isBefore(startDate)) {
                    runningTotal += ExperienceUtils.calculateHabitXP(habit, state)
                }
            }
        }

        return List(30) { i ->
            val date = startDate.plusDays(i.toLong())
            val dateString = date.toString()
            runningTotal += (dailyGains[dateString] ?: 0)
            XPDataPoint(i + 1, date.format(formatter), runningTotal)
        }
    }

    private fun calculateYearlyXpHistory(habits: List<Habit>, allStates: List<HabitDailyState>): List<XPDataPoint> {
        val formatter = DateTimeFormatter.ofPattern("MMM")
        val today = LocalDate.now()
        val habitMap = habits.associateBy { it.id }

        val dailyGains = allStates.groupBy { it.date }
            .mapValues { (_, states) ->
                states.sumOf { state ->
                    val habit = habitMap[state.habitId]
                    if (habit != null) ExperienceUtils.calculateHabitXP(habit, state) else 0
                }
            }

        val startDate = today.minusMonths(11).withDayOfMonth(1)
        var runningTotal = 0f

        // Baseline XP: sum all XP before startDate
        allStates.forEach { state ->
            val habit = habitMap[state.habitId]
            if (habit != null) {
                val stateDate = try { LocalDate.parse(state.date) } catch (_: Exception) { null }
                if (stateDate != null && stateDate.isBefore(startDate)) {
                    runningTotal += ExperienceUtils.calculateHabitXP(habit, state)
                }
            }
        }

        return List(12) { i ->
            val monthDate = startDate.plusMonths(i.toLong())
            // Sum all daily gains in this month
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
