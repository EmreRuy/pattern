package com.example.pattern.ui.screens.profileScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.usecase.GetProfileStatsUseCase
import com.example.pattern.ui.screens.profileScreen.mapper.ProfileStateMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Staff-level ProfileViewModel.
 * Focused exclusively on data orchestration and state exposure.
 * Logic is delegated to Domain UseCases and UI Mappers.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileStatsUseCase: GetProfileStatsUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = getProfileStatsUseCase()
        .map { stats -> ProfileStateMapper.mapToUiState(stats) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )
}
