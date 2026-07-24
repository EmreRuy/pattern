package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.pattern.domain.model.*
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import com.example.pattern.domain.streak.StreakCalculator
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository = mockk<HabitRepository>(relaxed = true)
    private val streakCalculator = mockk<StreakCalculator>(relaxed = true)
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
    private lateinit var updateHabitProgressUseCase: UpdateHabitProgressUseCase
    private lateinit var viewModel: HomeViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()

    private val selectedDateFlow = MutableStateFlow(LocalDate.now())
    private val settingsFlow = MutableSharedFlow<Settings>(replay = 1)
    private val habitsFlow = MutableSharedFlow<List<Habit>>(replay = 1)
    private val dailyStatesFlow = MutableSharedFlow<List<HabitDailyState>>(replay = 1)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        updateHabitProgressUseCase = mockk<UpdateHabitProgressUseCase>(relaxed = true)
        
        every { savedStateHandle.getStateFlow("selected_date", any<LocalDate>()) } returns selectedDateFlow
        every { repository.getSettingsStream() } returns settingsFlow
        every { repository.getAllHabitsStream() } returns habitsFlow
        every { repository.getAllDailyStatesStream() } returns dailyStatesFlow
        
        settingsFlow.tryEmit(Settings(totalXP = 100))
        habitsFlow.tryEmit(emptyList())
        dailyStatesFlow.tryEmit(emptyList())
        
        every { streakCalculator.calculate(any(), any(), any()) } returns StreakInfo(5, 10, 50)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is success with loading flag`() = runTest {
        viewModel = HomeViewModel(repository, updateHabitProgressUseCase, streakCalculator, savedStateHandle, testDispatcher)
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success && state.isLoading,
            "Initial state should be Success with isLoading=true, but was $state")
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
                selectedDays = persistentListOf(true, true, true, true, true, true, true),
                iconCode = "🔥",
                isCompleted = false,
                createdAt = today.minusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                accentColorHex = "#77DD77",
                reminderTime = null,
                motivation = null
            )
        )
        
        viewModel = HomeViewModel(repository, updateHabitProgressUseCase, streakCalculator, savedStateHandle, testDispatcher)
        
        viewModel.uiState.test {
            var state = awaitItem() as HomeUiState.Success
            
            settingsFlow.emit(Settings(totalXP = 500))
            habitsFlow.emit(habits)

            while (state.isLoading || state.levelInfo.currentXP != 500 || state.habits.isEmpty()) {
                state = awaitItem() as HomeUiState.Success
            }
            
            assertFalse(state.isLoading)
            assertEquals(500, state.levelInfo.currentXP)
            assertEquals(1, state.habits.size)
            assertEquals("Test Habit", state.habits[0].name)
            assertEquals(5, state.habits[0].currentStreak)
        }
    }

    @Test
    fun `onDateSelected updates selectedDate and habits`() = runTest {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        viewModel = HomeViewModel(repository, updateHabitProgressUseCase, streakCalculator, savedStateHandle, testDispatcher)
        
        viewModel.uiState.test {
            var state = awaitItem() as HomeUiState.Success
            while (state.isLoading) {
                state = awaitItem() as HomeUiState.Success
            }
            
            viewModel.onEvent(HomeUiEvent.OnDateSelected(tomorrow))
            
            selectedDateFlow.value = tomorrow
            
            while (state.selectedDate != tomorrow) {
                state = awaitItem() as HomeUiState.Success
            }
            
            assertEquals(tomorrow, state.selectedDate)
            assertFalse(state.isSelectedDateToday)
        }
    }
}
