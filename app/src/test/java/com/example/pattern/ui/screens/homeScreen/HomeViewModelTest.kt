package com.example.pattern.ui.screens.homeScreen

import app.cash.turbine.test
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.domain.usecase.GetHomeHabitsUseCase
import io.mockk.coEvery
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
    private lateinit var viewModel: HomeViewModel
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getHomeHabitsUseCase = GetHomeHabitsUseCase(repository)
        
        // Default mocks
        every { repository.getSettingsStream() } returns flowOf(SettingsEntity(totalXP = 100))
        every { repository.getAllHabitsStream() } returns flowOf(emptyList())
        every { repository.getAllDailyStatesStream() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel = HomeViewModel(repository, getHomeHabitsUseCase)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `uiState emits habits correctly when repository has data`() = runTest {
        val today = LocalDate.now()
        val habits = listOf(
            Habit(
                id = 1,
                name = "Test Habit",
                type = HabitType.BUILD,
                durationInMinutes = 30,
                selectedDays = List(7) { true },
                iconCode = "🔥"
            )
        )
        
        every { repository.getAllHabitsStream() } returns flowOf(habits)
        every { repository.getSettingsStream() } returns flowOf(SettingsEntity(totalXP = 500))
        
        viewModel = HomeViewModel(repository, getHomeHabitsUseCase)
        
        viewModel.uiState.test {
            // First emission might be initial value
            val first = awaitItem()
            
            // Second emission should be the loaded state (using StandardTestDispatcher we need to advance)
            testScheduler.advanceUntilIdle()
            val state = awaitItem()
            
            assertFalse(state.isLoading)
            assertEquals(1, state.habits.size)
            assertEquals("Test Habit", state.habits[0].name)
            assertEquals(500, state.levelInfo.totalXp)
            assertTrue(state.hasAnyHabits)
        }
    }

    @Test
    fun `onDateSelected updates selectedDate and habits`() = runTest {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        viewModel = HomeViewModel(repository, getHomeHabitsUseCase)
        
        viewModel.onDateSelected(tomorrow)
        
        viewModel.uiState.test {
            testScheduler.advanceUntilIdle()
            val state = awaitItem()
            assertEquals(tomorrow, state.selectedDate)
            assertFalse(state.isSelectedDateToday)
        }
    }
}
