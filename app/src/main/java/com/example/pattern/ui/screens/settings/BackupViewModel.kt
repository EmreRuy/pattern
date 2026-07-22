package com.example.pattern.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.usecase.ExportBackupUseCase
import com.example.pattern.domain.usecase.ImportBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            exportBackupUseCase(uri)
                .onSuccess { _uiState.value = BackupUiState.Success("Backup exported successfully") }
                .onFailure { _uiState.value = BackupUiState.Error(it.message ?: "Failed to export backup") }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            importBackupUseCase(uri)
                .onSuccess { _uiState.value = BackupUiState.Success("Backup imported successfully. Please restart the app if changes are not visible.") }
                .onFailure { _uiState.value = BackupUiState.Error(it.message ?: "Failed to import backup") }
        }
    }

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }
}

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}
