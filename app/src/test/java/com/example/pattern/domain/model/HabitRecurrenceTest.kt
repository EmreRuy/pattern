package com.example.pattern.domain.model

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitRecurrenceTest {

    private val baseHabit = Habit(
        id = 1,
        name = "Test",
        type = HabitType.TASK,
        durationInMinutes = null,
        selectedDays = persistentListOf(true, true, true, true, true, true, true),
        iconCode = "🚀",
        isCompleted = false,
        createdAt = 0,
        accentColorHex = "#000000",
        reminderTime = null,
        motivation = null,
        startDate = LocalDate.of(2024, 1, 1), // Monday
        frequencyType = FrequencyType.DAILY
    )

    @Test
    fun `isScheduledOn returns false before startDate`() {
        val habit = baseHabit.copy(startDate = LocalDate.of(2024, 1, 10))
        assertFalse(habit.isScheduledOn(LocalDate.of(2024, 1, 9)))
    }

    @Test
    fun `isScheduledOn returns false after endDate`() {
        val habit = baseHabit.copy(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 10)
        )
        assertFalse(habit.isScheduledOn(LocalDate.of(2024, 1, 11)))
        assertTrue(habit.isScheduledOn(LocalDate.of(2024, 1, 10)))
    }

    @Test
    fun `DAILY frequency returns true for all dates in range`() {
        val habit = baseHabit.copy(frequencyType = FrequencyType.DAILY)
        assertTrue(habit.isScheduledOn(LocalDate.of(2024, 1, 1)))
        assertTrue(habit.isScheduledOn(LocalDate.of(2024, 1, 2)))
        assertTrue(habit.isScheduledOn(LocalDate.of(2024, 5, 20)))
    }

    @Test
    fun `WEEKLY frequency respects bitmask`() {
        // Mon, Wed, Fri (1, 3, 5) -> (1 << 0) | (1 << 2) | (1 << 4) = 1 + 4 + 16 = 21
        val habit = baseHabit.copy(
            frequencyType = FrequencyType.WEEKLY,
            daysOfWeekBitmask = 21
        )
        
        assertTrue("Monday", habit.isScheduledOn(LocalDate.of(2024, 1, 1)))
        assertFalse("Tuesday", habit.isScheduledOn(LocalDate.of(2024, 1, 2)))
        assertTrue("Wednesday", habit.isScheduledOn(LocalDate.of(2024, 1, 3)))
        assertFalse("Thursday", habit.isScheduledOn(LocalDate.of(2024, 1, 4)))
        assertTrue("Friday", habit.isScheduledOn(LocalDate.of(2024, 1, 5)))
        assertFalse("Saturday", habit.isScheduledOn(LocalDate.of(2024, 1, 6)))
        assertFalse("Sunday", habit.isScheduledOn(LocalDate.of(2024, 1, 7)))
    }

    @Test
    fun `INTERVAL frequency respects interval days`() {
        // Every 3 days starting Jan 1 (Jan 1, Jan 4, Jan 7...)
        val habit = baseHabit.copy(
            frequencyType = FrequencyType.INTERVAL,
            frequencyInterval = 3,
            startDate = LocalDate.of(2024, 1, 1)
        )
        
        assertTrue(habit.isScheduledOn(LocalDate.of(2024, 1, 1)))
        assertFalse(habit.isScheduledOn(LocalDate.of(2024, 1, 2)))
        assertFalse(habit.isScheduledOn(LocalDate.of(2024, 1, 3)))
        assertTrue(habit.isScheduledOn(LocalDate.of(2024, 1, 4)))
        assertTrue(habit.isScheduledOn(LocalDate.of(2024, 1, 7)))
    }
}
