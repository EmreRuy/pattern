package com.example.pattern.domain.streak

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val calculator = StreakCalculatorImpl()
    private val everyDay = List(7) { true }.toImmutableList()
    private val weekDays = listOf(true, true, true, true, true, false, false).toImmutableList()

    private fun createHabit(selectedDays: List<Boolean> = everyDay): Habit {
        return Habit(
            id = 1,
            name = "Test Habit",
            type = HabitType.BUILD,
            durationInMinutes = null,
            selectedDays = selectedDays.toImmutableList(),
            iconCode = "icon",
            isCompleted = false,
            createdAt = 1704067200000L, // Jan 1st 2024
            accentColorHex = "#77DD77",
            reminderTime = null,
            motivation = null
        )
    }

    private fun createState(date: LocalDate, isCompleted: Boolean = true): HabitDailyState {
        return HabitDailyState(
            habitId = 1,
            date = date.toString(),
            isCompleted = isCompleted
        )
    }

    @Test
    fun `calculate returns zero for no completions`() {
        val habit = createHabit()
        val history = emptyList<HabitDailyState>()
        val today = LocalDate.of(2024, 1, 10)
        
        val result = calculator.calculate(habit, history, today)
        
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.totalCompletions)
        assertFalse(result.isCompletedToday)
    }

    @Test
    fun `calculate correctly calculates everyday habit streak`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val history = listOf(
            createState(today),
            createState(today.minusDays(1)),
            createState(today.minusDays(2))
        )
        
        val result = calculator.calculate(habit, history, today)
        
        assertEquals(3, result.currentStreak)
        assertEquals(3, result.totalCompletions)
        assertTrue(result.isCompletedToday)
        assertFalse(result.isAtRisk)
    }

    @Test
    fun `calculate handles broken streak for everyday habit`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val history = listOf(
            createState(today),
            createState(today.minusDays(2))
        )
        
        val result = calculator.calculate(habit, history, today)
        
        assertEquals(1, result.currentStreak)
        assertEquals(2, result.totalCompletions)
    }

    @Test
    fun `calculate preserves streak on non-scheduled days`() {
        // Monday 2024-05-20 (Scheduled)
        // Sunday 2024-05-19 (Not Scheduled)
        // Saturday 2024-05-18 (Not Scheduled)
        // Friday 2024-05-17 (Scheduled)
        
        val habit = createHabit(weekDays) // Mon-Fri
        val today = LocalDate.of(2024, 5, 20)
        
        val history = listOf(
            createState(today), // Monday
            createState(today.minusDays(3)) // Friday
        )
        
        val result = calculator.calculate(habit, history, today)
        
        // Streak should be 2 because Sat/Sun are not scheduled
        assertEquals(2, result.currentStreak)
    }

    @Test
    fun `calculate preserves streak if not completed today but scheduled (Grace Period)`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val history = listOf(
            createState(today.minusDays(1)), // Sunday
            createState(today.minusDays(2))  // Saturday
        )
        
        val result = calculator.calculate(habit, history, today)
        
        // Current streak should still be 2 because today isn't over yet
        assertEquals(2, result.currentStreak)
        assertTrue(result.isAtRisk)
        assertFalse(result.isCompletedToday)
    }

    @Test
    fun `calculate breaks if yesterday was scheduled and missed`() {
        val habit = createHabit(everyDay)
        val today = LocalDate.of(2024, 5, 20) // Monday
        val history = listOf(
            createState(today.minusDays(2)) // Saturday missed Sunday
        )
        
        val result = calculator.calculate(habit, history, today)
        
        assertEquals(0, result.currentStreak)
    }
}
