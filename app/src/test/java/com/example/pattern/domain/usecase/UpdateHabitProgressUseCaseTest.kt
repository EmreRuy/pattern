package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import io.mockk.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UpdateHabitProgressUseCaseTest {

    private val repository = mockk<HabitRepository>(relaxed = true)
    private lateinit var useCase: UpdateHabitProgressUseCase
    private val today = LocalDate.now()

    @BeforeEach
    fun setup() {
        useCase = UpdateHabitProgressUseCase(repository)
        
        // Default transaction behavior
        coEvery { repository.withTransaction<Any>(any()) } coAnswers {
            val block = firstArg<suspend () -> Any>()
            block()
        }
    }

    @Test
    fun `startTimer sets activeSessionStartMs when habit exists and is not completed`() = runTest {
        val habitId = 1
        val habit = createHabit(habitId, HabitType.BUILD)
        coEvery { repository.getHabitOnce(habitId) } returns habit
        coEvery { repository.getDailyStateOnce(habitId, any()) } returns null

        useCase.startTimer(habitId, today)

        val stateSlot = slot<HabitDailyState>()
        coVerify { repository.upsertDailyState(capture(stateSlot)) }
        assertNotNull(stateSlot.captured.activeSessionStartMs)
        assertEquals(habitId, stateSlot.captured.habitId)
    }

    @Test
    fun `pauseTimer updates accumulatedTimeMs and clears activeSessionStartMs`() = runTest {
        val habitId = 1
        val startTime = System.currentTimeMillis() - 1000 // 1 second ago
        val initialState = HabitDailyState(
            habitId = habitId,
            date = today.toString(),
            activeSessionStartMs = startTime,
            accumulatedTimeMs = 500
        )
        coEvery { repository.getDailyStateOnce(habitId, today.toString()) } returns initialState

        useCase.pauseTimer(habitId, today)

        val stateSlot = slot<HabitDailyState>()
        coVerify { repository.upsertDailyState(capture(stateSlot)) }
        assertNull(stateSlot.captured.activeSessionStartMs)
        assertTrue(stateSlot.captured.accumulatedTimeMs >= 1500) // 500 + 1000
    }

    @Test
    fun `finishTimer marks habit as completed and adds XP`() = runTest {
        val habitId = 1
        val habit = createHabit(habitId, HabitType.BUILD, durationInMinutes = 30)
        val initialState = HabitDailyState(habitId = habitId, date = today.toString(), accumulatedTimeMs = 1800000) // 30 mins
        
        coEvery { repository.getHabitOnce(habitId) } returns habit
        coEvery { repository.getDailyStateOnce(habitId, today.toString()) } returns initialState

        useCase.finishTimer(habitId, today)

        coVerify { repository.upsertDailyState(match { it.isCompleted }) }
        // XP: 10 + (30/15 * 5) = 20
        coVerify { repository.addXP(20) }
    }

    @Test
    fun `toggleTask updates completion status and XP correctly`() = runTest {
        val habitId = 1
        val habit = createHabit(habitId, HabitType.TASK)
        coEvery { repository.getHabitOnce(habitId) } returns habit
        coEvery { repository.getDailyStateOnce(habitId, today.toString()) } returns null

        // Toggle ON
        useCase.toggleTask(habitId, today, true)
        coVerify { repository.upsertDailyState(match { it.isTaskCompleted }) }
        coVerify { repository.addXP(15) }

        // Toggle OFF
        coEvery { repository.getDailyStateOnce(habitId, today.toString()) } returns 
            HabitDailyState(habitId = habitId, date = today.toString(), isTaskCompleted = true)
        
        useCase.toggleTask(habitId, today, false)
        coVerify { repository.upsertDailyState(match { !it.isTaskCompleted }) }
        coVerify { repository.addXP(-15) }
    }

    private fun createHabit(id: Int, type: HabitType, durationInMinutes: Int? = null): Habit {
        return Habit(
            id = id,
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
