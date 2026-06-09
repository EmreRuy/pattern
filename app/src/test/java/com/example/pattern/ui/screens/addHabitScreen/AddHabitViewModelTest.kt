package com.example.pattern.ui.screens.addHabitScreen

import app.cash.turbine.test
import com.example.pattern.domain.usecase.CheckHabitLimitUseCase
import com.example.pattern.domain.usecase.CreateHabitUseCase
import com.example.pattern.domain.usecase.HabitLimitStatus
import com.example.pattern.domain.usecase.IsPremiumUserUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class AddHabitViewModelTest {

    private val createHabitUseCase = mockk<CreateHabitUseCase>()
    private val isPremiumUserUseCase = mockk<IsPremiumUserUseCase>()
    private val checkHabitLimitUseCase = mockk<CheckHabitLimitUseCase>()
    
    private lateinit var viewModel: AddHabitViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { isPremiumUserUseCase() } returns flowOf(false)
        every { checkHabitLimitUseCase() } returns flowOf(HabitLimitStatus.Allowed(1, 5))
        
        viewModel = AddHabitViewModel(
            createHabitUseCase,
            isPremiumUserUseCase,
            checkHabitLimitUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.habitName)
        assertEquals("Grow", state.habitType)
        assertFalse(state.isPremium)
    }

    @Test
    fun `onNameChange updates habitName in uiState`() = runTest {
        viewModel.onNameChange("New Habit")
        assertEquals("New Habit", viewModel.uiState.value.habitName)
    }

    @Test
    fun `onDaysChange updates buildHabitDays in uiState`() = runTest {
        val days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        viewModel.onDaysChange(days)
        assertEquals(days, viewModel.uiState.value.buildHabitDays)
    }

    @Test
    fun `saveNewHabit calls createHabitUseCase and onSuccess when limit is not reached`() = runTest {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        
        coEvery { 
            createHabitUseCase.execute(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            ) 
        } returns 1L
        
        viewModel.onNameChange("Test Habit")
        viewModel.onColorChange("#FFFFFF")
        
        viewModel.saveNewHabit(onSuccess)
        
        coVerify { 
            createHabitUseCase.execute(
                name = "Test Habit",
                type = any(),
                emoji = any(),
                color = "#FFFFFF",
                selectedDays = any(),
                motivation = any(),
                reminderEnabled = any(),
                reminderTime = any(),
                durationHours = any(),
                durationMinutes = any(),
                taskCount = any()
            )
        }
        verify { onSuccess() }
    }

    @Test
    fun `saveNewHabit does not call createHabitUseCase when limit is reached`() = runTest {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        every { checkHabitLimitUseCase() } returns flowOf(HabitLimitStatus.Reached(5))
        
        // Re-initialize ViewModel to pick up the new mock for checkHabitLimitUseCase if needed, 
        // but here it's called inside saveNewHabit so it should be fine if we mock it before calling saveNewHabit.
        
        viewModel.saveNewHabit(onSuccess)
        
        coVerify(exactly = 0) { createHabitUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
        verify(exactly = 0) { onSuccess() }
    }
}
