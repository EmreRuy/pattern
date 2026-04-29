package com.example.pattern.ui.screens.profileScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.LevelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUiState(
    val levelInfo: LevelInfo = ExperienceUtils.getLevelInfo(0),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = repository.getSettingsStream()
        .map { settings ->
            ProfileUiState(
                levelInfo = ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0),
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )
}
