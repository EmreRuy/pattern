package com.example.pattern.ui.screens.settings

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.repository.BackupRepository
import com.example.pattern.data.repository.PremiumRepository
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BackupStatus {
    object Idle : BackupStatus
    object Loading : BackupStatus
    data class Success(val message: String) : BackupStatus
    data class Error(val message: String) : BackupStatus
}

@Immutable
data class SettingsUiState(
    val settings: Settings = Settings(),
    val isLoading: Boolean = false,
    val backupStatus: BackupStatus = BackupStatus.Idle,
    val isPremium: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val backupRepository: BackupRepository,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getSettingsStream(),
        _backupStatus,
        premiumRepository.isPremiumUser()
    ) { settingsRes, backupStatus, isPremium ->
        val settings = (settingsRes as? DataResult.Success)?.data ?: Settings()
        val isLoading = settingsRes is DataResult.Loading
        
        SettingsUiState(
            settings = settings,
            isLoading = isLoading,
            backupStatus = backupStatus,
            isPremium = isPremium
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        viewModelScope.launch {
            repository.updateQuietHours(enabled, start, end)
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.Loading
            backupRepository.exportBackup(uri)
                .onSuccess { _backupStatus.value = BackupStatus.Success("Backup exported successfully") }
                .onFailure { _backupStatus.value = BackupStatus.Error(it.message ?: "Export failed") }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.Loading
            backupRepository.importBackup(uri)
                .onSuccess { _backupStatus.value = BackupStatus.Success("Backup restored successfully") }
                .onFailure { _backupStatus.value = BackupStatus.Error(it.message ?: "Restore failed") }
        }
    }

    fun clearBackupStatus() {
        _backupStatus.value = BackupStatus.Idle
    }
}
