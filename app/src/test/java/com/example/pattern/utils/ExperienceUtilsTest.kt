package com.example.pattern.utils

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import kotlinx.collections.immutable.toImmutableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExperienceUtilsTest {

    @Test
    fun `calculateHabitXP returns correct XP for TASK`() {
        val habit = createHabit(HabitType.TASK)
        val dailyState = HabitDailyState(habitId = 1, date = "2024-01-01", isTaskCompleted = true)
        
        val xp = ExperienceUtils.calculateHabitXP(habit, dailyState)
        assertEquals(15, xp)
    }

    @Test
    fun `calculateHabitXP returns correct XP for QUIT`() {
        val habit = createHabit(HabitType.QUIT)
        val dailyState = HabitDailyState(habitId = 1, date = "2024-01-01", isTaskCompleted = true)
        
        val xp = ExperienceUtils.calculateHabitXP(habit, dailyState)
        assertEquals(20, xp)
    }

    @Test
    fun `calculateHabitXP returns correct XP for BUILD with duration`() {
        val habit = createHabit(HabitType.BUILD, durationInMinutes = 45)
        val dailyState = HabitDailyState(habitId = 1, date = "2024-01-01", isCompleted = true)
        
        // Base 10 + (45/15 * 5) = 10 + 15 = 25
        val xp = ExperienceUtils.calculateHabitXP(habit, dailyState)
        assertEquals(25, xp)
    }

    @Test
    fun `calculateHabitXP returns 0 when not completed`() {
        val habit = createHabit(HabitType.BUILD, durationInMinutes = 30)
        val dailyState = HabitDailyState(habitId = 1, date = "2024-01-01", isCompleted = false)
        
        val xp = ExperienceUtils.calculateHabitXP(habit, dailyState)
        assertEquals(0, xp)
    }

    @Test
    fun `getLevelInfo returns correct level and title`() {
        // Thresholds: 0, 100, 300, 700, 1500...
        
        val level1 = ExperienceUtils.getLevelInfo(50)
        assertEquals(1, level1.level)
        assertEquals("Novice", level1.title)
        assertEquals(0.5f, level1.progress)

        val level2 = ExperienceUtils.getLevelInfo(150)
        assertEquals(2, level2.level)
        assertEquals("Beginner", level2.title)
        // (150 - 100) / (300 - 100) = 50 / 200 = 0.25f
        assertEquals(0.25f, level2.progress)
    }

    private fun createHabit(type: HabitType, durationInMinutes: Int? = null): Habit {
        return Habit(
            id = 1,
            name = "Test",
            type = type,
            durationInMinutes = durationInMinutes,
            selectedDays = List(7) { true }.toImmutableList(),
            iconCode = "🔥",
            isCompleted = false,
            createdAt = 0L,
            accentColorHex = "#FFFFFF",
            reminderTime = null,
            motivation = null
        )
    }
}
