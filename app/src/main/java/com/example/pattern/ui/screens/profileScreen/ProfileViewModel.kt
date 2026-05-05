package com.example.pattern.ui.screens.profileScreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.*
import com.example.pattern.domain.usecase.GetProfileStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@Immutable
data class ProfileUiState(
    val levelInfo: LevelInfo? = null,
    val weeklyXpHistory: List<XPDataPoint> = emptyList(),
    val xpHistory: List<XPDataPoint> = emptyList(),
    val yearlyXpHistory: List<XPDataPoint> = emptyList(),
    val doneCount: Int = 0,
    val missedCount: Int = 0,
    val successRate: Float = 0f,
    val totalXp: Int = 0,
    val totalHabits: Int = 0,
    val isLoading: Boolean = false,
    val topDoneHabits: List<HabitStat> = emptyList(),
    val topMissedHabits: List<HabitStat> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileStatsUseCase: GetProfileStatsUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = getProfileStatsUseCase()
        .map { stats ->
            ProfileUiState(
                levelInfo = stats.levelInfo,
                weeklyXpHistory = stats.weeklyXpHistory,
                xpHistory = stats.xpHistory,
                yearlyXpHistory = stats.yearlyXpHistory,
                doneCount = stats.doneCount,
                missedCount = stats.missedCount,
                successRate = stats.successRate,
                totalXp = stats.totalXp,
                totalHabits = stats.totalHabits,
                topDoneHabits = stats.topDoneHabits,
                topMissedHabits = stats.topMissedHabits,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )
}
