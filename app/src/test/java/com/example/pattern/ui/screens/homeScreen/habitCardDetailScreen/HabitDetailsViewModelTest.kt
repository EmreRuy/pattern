package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.HabitWithHistory
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
import com.example.pattern.notifications.ReminderManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailsViewModelTest {

    private val repository = mockk<HabitRepository>()
    private val reminderManager = mockk<ReminderManager>(relaxed = true)
    private lateinit var viewModel: HabitDetailsViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()
    private val habitId = 1

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val savedStateHandle = SavedStateHandle(mapOf("habitId" to habitId))
        
        val habit = Habit(
            id = habitId,
            name = "Test Habit",
            type = HabitType.BUILD,
            durationInMinutes = 30,
            selectedDays = List(7) { true }.toImmutableList(),
            iconCode = "🔥",
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            accentColorHex = "#FFFFFF",
            reminderTime = "09:00",
            motivation = "Go for it"
        )
        
        every { repository.getHabitWithHistoryStream(habitId) } returns flowOf(DataResult.Success(HabitWithHistory(habit, emptyList())))
        
        viewModel = HabitDetailsViewModel(repository, reminderManager, savedStateHandle)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Success when repository returns data`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            // Skip initial Loading state from stateIn
            if (state is HabitDetailsUiState.Loading) {
                state = awaitItem()
            }
            
            assertTrue(state is HabitDetailsUiState.Success, "Expected Success state but got $state")
            val successState = state as HabitDetailsUiState.Success
            assertEquals("Test Habit", successState.habit.name)
            assertEquals("09:00", successState.habit.reminderTime)
        }
    }

    @Test
    fun `deleteHabit calls repository and reminderManager`() = runTest {
        val habit = mockk<Habit>()
        coEvery { repository.getHabitOnce(habitId) } returns habit
        coEvery { repository.deleteHabit(habit) } returns Unit
        
        viewModel.deleteHabit(habitId)
        
        coVerify { repository.deleteHabit(habit) }
        verify { reminderManager.cancelReminder(habitId) }
    }
}
