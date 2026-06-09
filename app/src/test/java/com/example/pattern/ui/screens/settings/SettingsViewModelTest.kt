package com.example.pattern.ui.screens.settings

import android.net.Uri
import app.cash.turbine.test
import com.example.pattern.data.repository.BackupRepository
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.IsPremiumUserUseCase
import com.example.pattern.domain.util.DataResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val repository = mockk<HabitRepository>()
    private val backupRepository = mockk<BackupRepository>()
    private val isPremiumUserUseCase = mockk<IsPremiumUserUseCase>()
    
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getSettingsStream() } returns flowOf(DataResult.Success(Settings()))
        every { isPremiumUserUseCase() } returns flowOf(false)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Success with default settings`() = runTest {
        viewModel = SettingsViewModel(repository, backupRepository, isPremiumUserUseCase)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(Settings(), state.settings)
            assertFalse(state.isPremium)
        }
    }

    @Test
    fun `updateQuietHours calls repository`() = runTest {
        coEvery { repository.updateQuietHours(any(), any(), any()) } returns Unit
        viewModel = SettingsViewModel(repository, backupRepository, isPremiumUserUseCase)
        
        viewModel.updateQuietHours(true, "22:00", "08:00")
        
        coVerify { repository.updateQuietHours(true, "22:00", "08:00") }
    }

    @Test
    fun `exportBackup updates backupStatus on success`() = runTest {
        val uri = mockk<Uri>()
        coEvery { backupRepository.exportBackup(uri) } returns Result.success(Unit)
        viewModel = SettingsViewModel(repository, backupRepository, isPremiumUserUseCase)
        
        viewModel.exportBackup(uri)
        
        assertTrue(viewModel.uiState.value.backupStatus is BackupStatus.Success)
        assertEquals("Backup exported successfully", (viewModel.uiState.value.backupStatus as BackupStatus.Success).message)
    }

    @Test
    fun `importBackup updates backupStatus on failure`() = runTest {
        val uri = mockk<Uri>()
        coEvery { backupRepository.importBackup(uri) } returns Result.failure(Exception("Import error"))
        viewModel = SettingsViewModel(repository, backupRepository, isPremiumUserUseCase)
        
        viewModel.importBackup(uri)
        
        assertTrue(viewModel.uiState.value.backupStatus is BackupStatus.Error)
        assertEquals("Import error", (viewModel.uiState.value.backupStatus as BackupStatus.Error).message)
    }
}
