package com.example.pattern.ui.screens.profileScreen

import app.cash.turbine.test
import com.example.pattern.domain.model.LevelInfo
import com.example.pattern.domain.model.ProfileStats
import com.example.pattern.domain.usecase.GetProfileStatsUseCase
import com.example.pattern.domain.util.DataResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val getProfileStatsUseCase = mockk<GetProfileStatsUseCase>()
    private lateinit var viewModel: ProfileViewModel
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
    fun `initial state is loading`() = runTest {
        every { getProfileStatsUseCase() } returns flowOf(DataResult.Loading)
        viewModel = ProfileViewModel(getProfileStatsUseCase)
        
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `state updates to success when use case returns data`() = runTest {
        val stats = ProfileStats(
            levelInfo = LevelInfo(1, "Novice", 0, 100, 0f),
            weeklyXpHistory = persistentListOf(),
            xpHistory = persistentListOf(),
            yearlyXpHistory = persistentListOf(),
            doneCount = 10,
            missedCount = 2,
            successRate = 0.83f,
            totalXp = 150,
            totalHabits = 5,
            topDoneHabits = persistentListOf(),
            topMissedHabits = persistentListOf()
        )
        
        every { getProfileStatsUseCase() } returns flowOf(DataResult.Success(stats))
        viewModel = ProfileViewModel(getProfileStatsUseCase)
        
        viewModel.uiState.test {
            var state = awaitItem()
            // Skip initial loading state
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertEquals(150, state.successDashboard.xpPoints)
            assertEquals("OPTIMAL", state.successDashboard.statusText)
        }
    }

    @Test
    fun `state updates to error when use case returns error`() = runTest {
        every { getProfileStatsUseCase() } returns flowOf(DataResult.Error(RuntimeException("Error")))
        viewModel = ProfileViewModel(getProfileStatsUseCase)
        
        viewModel.uiState.test {
            var state = awaitItem()
            // Skip initial loading state
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertEquals("Error", state.errorMessage)
        }
    }
}
