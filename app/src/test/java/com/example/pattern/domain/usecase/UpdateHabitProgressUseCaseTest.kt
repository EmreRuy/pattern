package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.repository.UserRepository
import io.mockk.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UpdateHabitProgressUseCaseTest {

    private val habitRepository = mockk<HabitRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var useCase: UpdateHabitProgressUseCase
    private val today = LocalDate.now()

    @BeforeEach
    fun setup() {
        useCase = UpdateHabitProgressUseCase(habitRepository, userRepository)
        
        // Default transaction behavior
        coEvery { habitRepository.withTransaction<Any>(any()) } coAnswers {
            val block = firstArg<suspend () -> Any>()
            block()
        }
    }

    @Test
    fun `startTimer delegates to repository`() = runTest {
        val habitId = 1
        useCase.startTimer(habitId, today)
        coVerify { habitRepository.startTimer(habitId, today.toString(), any()) }
    }

    @Test
    fun `pauseTimer delegates to repository`() = runTest {
        val habitId = 1
        useCase.pauseTimer(habitId, today)
        coVerify { habitRepository.pauseTimer(habitId, today.toString(), any()) }
    }

    @Test
    fun `finishTimer marks habit as completed and adds XP`() = runTest {
        val habitId = 1
        val habit = createHabit(habitId, HabitType.BUILD, durationInMinutes = 30)
        val finishedState = HabitDailyState(
            habitId = habitId, 
            date = today.toString(), 
            accumulatedTimeMs = 1800000, 
            isCompleted = true
        )
        
        coEvery { habitRepository.getHabitOnce(habitId) } returns habit
        coEvery { habitRepository.finishTimer(habitId, today.toString(), any()) } returns finishedState

        useCase.finishTimer(habitId, today)

        coVerify { habitRepository.finishTimer(habitId, today.toString(), any()) }
        // XP: 10 + (30/15 * 5) = 20
        coVerify { userRepository.addXP(20) }
    }

    @Test
    fun `toggleTask updates completion status and XP correctly`() = runTest {
        val habitId = 1
        val habit = createHabit(habitId, HabitType.TASK)
        coEvery { habitRepository.getHabitOnce(habitId) } returns habit
        coEvery { habitRepository.getDailyStateOnce(habitId, today.toString()) } returns null andThen 
            HabitDailyState(habitId = habitId, date = today.toString(), isTaskCompleted = true)

        // Toggle ON
        useCase.toggleTask(habitId, today, true)
        coVerify { habitRepository.setTaskCompleted(habitId, today.toString(), true) }
        coVerify { userRepository.addXP(15) }

        // Toggle OFF
        coEvery { habitRepository.getDailyStateOnce(habitId, today.toString()) } returns 
            HabitDailyState(habitId = habitId, date = today.toString(), isTaskCompleted = true) andThen
            HabitDailyState(habitId = habitId, date = today.toString(), isTaskCompleted = false)
        
        useCase.toggleTask(habitId, today, false)
        coVerify { habitRepository.setTaskCompleted(habitId, today.toString(), false) }
        coVerify { userRepository.addXP(-15) }
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
