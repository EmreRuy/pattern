package com.example.pattern.domain.usecase

import app.cash.turbine.test
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class GetProfileStatsUseCaseTest {

    private lateinit var repository: HabitRepository
    private lateinit var useCase: GetProfileStatsUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = GetProfileStatsUseCase(repository)
    }

    @Test
    fun `when repository returns habits and completions, stats are calculated correctly`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val createdAt = yesterday.minusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val habits = listOf(
            Habit(
                id = 1,
                name = "Build Habit",
                type = HabitType.BUILD,
                durationInMinutes = 30,
                selectedDays = List(7) { true }.toImmutableList(),
                iconCode = "🔥",
                isCompleted = false,
                createdAt = createdAt,
                accentColorHex = "#FFFFFF",
                reminderTime = null,
                motivation = null
            ),
            Habit(
                id = 2,
                name = "Task Habit",
                type = HabitType.TASK,
                durationInMinutes = null,
                selectedDays = List(7) { true }.toImmutableList(),
                iconCode = "✅",
                isCompleted = false,
                createdAt = createdAt,
                accentColorHex = "#000000",
                reminderTime = null,
                motivation = null
            )
        )

        val completedDatesMap = mapOf(
            1 to setOf(yesterday), // 1 completion for Build Habit
            2 to setOf(yesterday, today) // 2 completions for Task Habit
        )

        every { repository.getAllHabitsStream() } returns flowOf(DataResult.Success(habits))
        every { repository.getCompletedDatesStream() } returns flowOf(DataResult.Success(completedDatesMap))

        useCase().test {
            val result = awaitItem()
            assertTrue(result is DataResult.Success)
            val stats = (result as DataResult.Success).data

            // Build XP: 10 (base) + (30/15 * 5) = 20 XP per completion. 1 completion = 20 XP.
            // Task XP: 15 XP per completion. 2 completions = 30 XP.
            // Total XP = 50.
            assertEquals(50, stats.totalXp)
            assertEquals(3, stats.doneCount)
            assertEquals(2, stats.totalHabits)
            
            assertEquals(20, stats.xpDistribution.buildXP)
            assertEquals(30, stats.xpDistribution.taskXP)
            assertEquals(0, stats.xpDistribution.quitXP)

            // Success rate calculation
            // Habit 1 (Build): Created 11 days ago (10 + today). Scheduled 10 days before today. 
            // Completed 1 day (yesterday). Missed = 10 - 1 = 9.
            // Habit 2 (Task): Scheduled 10 days before today.
            // Completed 2 days (yesterday, today). Missed = 10 - 1 = 9. (Note: today is not counted as missed if not completed yet in countScheduledDays loop, but here it is completed).
            // Actually let's look at countScheduledDays(creationDate, today.minusDays(1), habit.selectedDays)
            // For both habits: countScheduledDays is for 10 days (yesterday and 9 days before).
            // Completions until yesterday: Habit 1: 1. Habit 2: 1.
            // Missed for Habit 1: 10 - 1 = 9.
            // Missed for Habit 2: 10 - 1 = 9.
            // Total missed = 20.
            // Total attempts = 3 (done) + 20 (missed) = 23.
            // Success rate = 3 / 23 = ~0.1304
            assertEquals(3f / 23f, stats.successRate, 0.001f)

            awaitComplete()
        }
    }

    @Test
    fun `when repository returns error, use case returns error`() = runTest {
        val exception = RuntimeException("Database error")
        every { repository.getAllHabitsStream() } returns flowOf(DataResult.Error(exception))
        every { repository.getCompletedDatesStream() } returns flowOf(DataResult.Success(emptyMap()))

        useCase().test {
            val result = awaitItem()
            assertTrue(result is DataResult.Error)
            assertEquals(exception, (result as DataResult.Error).exception)
            awaitComplete()
        }
    }
}
