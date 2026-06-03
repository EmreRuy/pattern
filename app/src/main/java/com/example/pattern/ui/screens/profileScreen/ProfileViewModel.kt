package com.example.pattern.ui.screens.profileScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.usecase.GetProfileStatsUseCase
import com.example.pattern.domain.util.DataResult
import com.example.pattern.ui.screens.profileScreen.mapper.ProfileStateMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Staff-level ProfileViewModel.
 * Now integrated with DataResult for unified state handling.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileStatsUseCase: GetProfileStatsUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = getProfileStatsUseCase()
        .map { result ->
            when (result) {
                is DataResult.Loading -> ProfileUiState(isLoading = true)
                is DataResult.Error -> ProfileUiState(isLoading = false, errorMessage = result.exception.message)
                is DataResult.Success -> ProfileStateMapper.mapToUiState(result.data).copy(isLoading = false)
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )
}
