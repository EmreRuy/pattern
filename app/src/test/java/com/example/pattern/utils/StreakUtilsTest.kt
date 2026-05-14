package com.example.pattern.utils

import com.example.pattern.domain.model.Habit as DomainHabit
import com.example.pattern.domain.model.HabitDailyState as DomainHabitDailyState
import com.example.pattern.domain.model.HabitType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakUtilsTest {

    private val everyDay = List(7) { true }
    private val weekDays = listOf(true, true, true, true, true, false, false)

    private fun createHabit(selectedDays: List<Boolean> = everyDay): DomainHabit {
        return DomainHabit(
            id = 1,
            name = "Test Habit",
            type = HabitType.BUILD,
            durationInMinutes = null,
            selectedDays = selectedDays,
            iconCode = "icon",
            isCompleted = false,
            createdAt = 1704067200000L, // Jan 1st 2024
            accentColorHex = "#77DD77",
            accumulatedTimeMs = 0L,
            activeSessionStartMs = null,
            reminderTime = null,
            motivation = null
        )
    }

    private fun createState(date: LocalDate, isCompleted: Boolean = true): DomainHabitDailyState {
        return DomainHabitDailyState(
            habitId = 1,
            date = date.toString(),
            isCompleted = isCompleted
        )
    }

    @Test
    fun `calculateStreak returns zero for no completions`() {
        val habit = createHabit()
        val dailyStates = emptyList<DomainHabitDailyState>()
        
        val result = calculateStreak(habit, dailyStates)
        
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.totalCompletions)
    }

    @Test
    fun `calculateStreak correctly calculates everyday habit streak`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val dailyStates = listOf(
            createState(today),
            createState(today.minusDays(1)),
            createState(today.minusDays(2))
        )
        
        val result = calculateStreak(habit, dailyStates, today)
        
        assertEquals(3, result.currentStreak)
        assertEquals(3, result.totalCompletions)
    }

    @Test
    fun `calculateStreak handles broken streak for everyday habit`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val dailyStates = listOf(
            createState(today),
            createState(today.minusDays(2))
        )
        
        val result = calculateStreak(habit, dailyStates, today)
        
        assertEquals(1, result.currentStreak)
        assertEquals(2, result.totalCompletions)
    }

    @Test
    fun `calculateStreak preserves streak on non-scheduled days`() {
        // Monday 2024-05-20 (Scheduled)
        // Sunday 2024-05-19 (Not Scheduled)
        // Saturday 2024-05-18 (Not Scheduled)
        // Friday 2024-05-17 (Scheduled)
        
        val habit = createHabit(weekDays) // Mon-Fri
        val today = LocalDate.of(2024, 5, 20)
        
        val dailyStates = listOf(
            createState(today), // Monday
            createState(today.minusDays(3)) // Friday
        )
        
        val result = calculateStreak(habit, dailyStates, today)
        
        // Streak should be 2 because Sat/Sun are not scheduled
        assertEquals(2, result.currentStreak)
        assertEquals(2, result.totalCompletions)
    }

    @Test
    fun `calculateStreak handles streak ending yesterday`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val dailyStates = listOf(
            createState(today.minusDays(1)), // Sunday
            createState(today.minusDays(2))  // Saturday
        )
        
        val result = calculateStreak(habit, dailyStates, today)
        
        assertEquals(2, result.currentStreak)
        assertEquals(2, result.totalCompletions)
    }

    @Test
    fun `calculateStreak breaks if not completed today and yesterday was scheduled`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val dailyStates = listOf(
            createState(today.minusDays(2)) // Saturday
        )
        
        val result = calculateStreak(habit, dailyStates, today)
        
        assertEquals(0, result.currentStreak)
        assertEquals(1, result.totalCompletions)
    }
}
