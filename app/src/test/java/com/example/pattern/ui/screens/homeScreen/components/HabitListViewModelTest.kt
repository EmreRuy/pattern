package com.example.pattern.ui.screens.homeScreen.components

import app.cash.turbine.test
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitListViewModelTest {

    private val repository = mockk<HabitRepository>()
    private lateinit var viewModel: HabitListViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        every { repository.getAllHabitsStream() } returns flowOf(DataResult.Loading)
        every { repository.getDailyStatesForDate(any()) } returns flowOf(DataResult.Loading)
        
        viewModel = HabitListViewModel(repository)
        
        assertEquals(HabitListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `state updates to Success when repository returns data`() = runTest {
        val habits = listOf(
            Habit(
                id = 1,
                name = "Test Habit",
                type = HabitType.BUILD,
                durationInMinutes = 30,
                selectedDays = List(7) { true }.toImmutableList(),
                iconCode = "🔥",
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                accentColorHex = "#FFFFFF",
                reminderTime = null,
                motivation = null
            )
        )
        val dailyStates = listOf(
            HabitDailyState(habitId = 1, date = LocalDate.now().toString(), isCompleted = true)
        )
        
        every { repository.getAllHabitsStream() } returns flowOf(DataResult.Success(habits))
        every { repository.getDailyStatesForDate(LocalDate.now().toString()) } returns flowOf(DataResult.Success(dailyStates))
        
        viewModel = HabitListViewModel(repository)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HabitListUiState.Success)
            val successState = state as HabitListUiState.Success
            assertEquals(1, successState.habits.items.size)
            assertTrue(successState.todayStates.states.containsKey(1))
            assertTrue(successState.todayStates.states[1]?.isCompleted == true)
        }
    }

    @Test
    fun `state updates to Error when repository returns error`() = runTest {
        every { repository.getAllHabitsStream() } returns flowOf(DataResult.Error(RuntimeException("Habit Error")))
        every { repository.getDailyStatesForDate(any()) } returns flowOf(DataResult.Success(emptyList()))
        
        viewModel = HabitListViewModel(repository)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HabitListUiState.Error)
            assertEquals("Habit Error", (state as HabitListUiState.Error).message)
        }
    }
}
