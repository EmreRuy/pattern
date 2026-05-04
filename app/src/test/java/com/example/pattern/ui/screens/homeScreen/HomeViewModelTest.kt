package com.example.pattern.ui.screens.homeScreen

import app.cash.turbine.test
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.GetHomeHabitsUseCase
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository = mockk<HabitRepository>(relaxed = true)
    private lateinit var getHomeHabitsUseCase: GetHomeHabitsUseCase
    private lateinit var updateHabitProgressUseCase: UpdateHabitProgressUseCase
    private lateinit var viewModel: HomeViewModel
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getHomeHabitsUseCase = GetHomeHabitsUseCase(repository)
        updateHabitProgressUseCase = mockk<UpdateHabitProgressUseCase>(relaxed = true)
        
        // Default mocks
        every { repository.getSettingsStream() } returns flowOf(Settings(totalXP = 100))
        every { repository.getAllHabitsStream() } returns flowOf(emptyList())
        every { repository.getAllDailyStatesStream() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading or success`() = runTest {
        viewModel = HomeViewModel(repository, getHomeHabitsUseCase, updateHabitProgressUseCase)
        val state = viewModel.uiState.value
        assertTrue("Initial state should be Loading or Success, but was $state", 
            state is HomeUiState.Loading || state is HomeUiState.Success)
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
                timerStartTime = null,
                timerPauseTime = null,
                reminderTime = null,
                motivation = null
            )
        )
        
        every { repository.getAllHabitsStream() } returns flowOf(habits)
        every { repository.getSettingsStream() } returns flowOf(Settings(totalXP = 500))
        
        viewModel = HomeViewModel(repository, getHomeHabitsUseCase, updateHabitProgressUseCase)
        
        viewModel.uiState.test {
            // StateFlow emits current value immediately. Skip Loading states.
            var state = awaitItem()
            while (state is HomeUiState.Loading) {
                state = awaitItem()
            }
            
            assertTrue("Expected Success state but got $state", state is HomeUiState.Success)
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
        
        viewModel = HomeViewModel(repository, getHomeHabitsUseCase, updateHabitProgressUseCase)
        
        viewModel.uiState.test {
            // Wait for initial Success state (today)
            var state = awaitItem()
            while (state is HomeUiState.Loading) {
                state = awaitItem()
            }
            assertTrue("Expected initial Success state but got $state", state is HomeUiState.Success)
            
            // Trigger update
            viewModel.onEvent(HomeUiEvent.OnDateSelected(tomorrow))
            
            // Wait for updated state (tomorrow)
            state = awaitItem()
            while (state is HomeUiState.Success && state.selectedDate != tomorrow) {
                state = awaitItem()
            }
            
            assertTrue("Expected Success state with tomorrow's date but got $state", 
                state is HomeUiState.Success && state.selectedDate == tomorrow)
            val successState = state as HomeUiState.Success
            assertEquals(tomorrow, successState.selectedDate)
            assertFalse(successState.isSelectedDateToday)
        }
    }
}
