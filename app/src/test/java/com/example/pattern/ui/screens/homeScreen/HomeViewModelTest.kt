package com.example.pattern.ui.screens.homeScreen

import app.cash.turbine.test
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository = mockk<HabitRepository>(relaxed = true)
    private lateinit var updateHabitProgressUseCase: UpdateHabitProgressUseCase
    private lateinit var viewModel: HomeViewModel
    
    // Lead Expert Tip: Use UnconfinedTestDispatcher for StateFlow/Turbine tests 
    // to ensure emissions are handled eagerly and synchronously.
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        updateHabitProgressUseCase = mockk<UpdateHabitProgressUseCase>(relaxed = true)
        
        // Default mocks - Ensure EVERY flow in the combine block is mocked and emits!
        every { repository.getSettingsStream() } returns flowOf(Settings(totalXP = 100))
        every { repository.getAllHabitsStream() } returns flowOf(emptyList())
        every { repository.getAllDailyStatesStream() } returns flowOf(emptyList())
        every { repository.getCompletedDatesStream() } returns flowOf(emptyMap())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is success with loading flag`() = runTest {
        viewModel = HomeViewModel(repository, updateHabitProgressUseCase, testDispatcher)
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success && state.isLoading,
            "Initial state should be Success with isLoading=true, but was $state")
    }

    @Test
    fun `uiState emits habits correctly when repository has data`() = runTest {
        val habits = listOf(
            Habit(
                id = 1,
                name = "Test Habit",
                type = HabitType.BUILD,
                durationInMinutes = 30,
                selectedDays = List(7) { true },
                iconCode = "🔥",
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                accentColorHex = "#77DD77",
                accumulatedTimeMs = 0L,
                activeSessionStartMs = null,
                reminderTime = null,
                motivation = null
            )
        )
        
        every { repository.getAllHabitsStream() } returns flowOf(habits)
        every { repository.getSettingsStream() } returns flowOf(Settings(totalXP = 500))
        
        viewModel = HomeViewModel(repository, updateHabitProgressUseCase, testDispatcher)
        
        viewModel.uiState.test {
            // Wait for initial Success state with isLoading=true
            val initialState = awaitItem()
            assertTrue(initialState is HomeUiState.Success && (initialState as HomeUiState.Success).isLoading)
            
            // Wait for data emission
            val state = awaitItem()
            
            assertTrue(state is HomeUiState.Success && !(state as HomeUiState.Success).isLoading, 
                "Expected Success state with isLoading=false but got $state")
            val successState = state as HomeUiState.Success
            assertEquals(1, successState.habits.size)
            assertEquals("Test Habit", successState.habits[0].name)
            assertEquals(500, successState.levelInfo.currentXP)
            assertTrue(successState.hasAnyHabits)
        }
    }

    @Test
    fun `onDateSelected updates selectedDate and habits`() = runTest {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        viewModel = HomeViewModel(repository, updateHabitProgressUseCase, testDispatcher)
        
        viewModel.uiState.test {
            // Skip initial state
            awaitItem() 
            // Wait for initial data Success state (today)
            val stateAtStart = awaitItem()
            assertTrue(stateAtStart is HomeUiState.Success && !(stateAtStart as HomeUiState.Success).isLoading, 
                "Expected initial Success state with data but got $stateAtStart")
            
            // Trigger update
            viewModel.onEvent(HomeUiEvent.OnDateSelected(tomorrow))
            
            // In StateFlow + Unconfined, the emission should be immediate.
            val stateAfterUpdate = awaitItem()
            
            assertTrue(stateAfterUpdate is HomeUiState.Success && stateAfterUpdate.selectedDate == tomorrow,
                "Expected Success state with tomorrow's date but got $stateAfterUpdate")
            val successState = stateAfterUpdate as HomeUiState.Success
            assertEquals(tomorrow, successState.selectedDate)
            assertFalse(successState.isSelectedDateToday)
        }
    }
}
