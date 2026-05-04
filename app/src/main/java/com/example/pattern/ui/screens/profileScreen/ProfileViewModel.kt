package com.example.pattern.ui.screens.profileScreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.*
import com.example.pattern.domain.usecase.GetHabitStatsSummaryUseCase
import com.example.pattern.domain.usecase.GetXpHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@Immutable
data class ProfileUiState(
    val levelInfo: LevelInfo? = null,
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
    private val getHabitStatsSummaryUseCase: GetHabitStatsSummaryUseCase,
    private val getXpHistoryUseCase: GetXpHistoryUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        getHabitStatsSummaryUseCase(),
        getXpHistoryUseCase()
    ) { summary, history ->
        ProfileUiState(
            levelInfo = summary.levelInfo,
            xpHistory = history.first,
            yearlyXpHistory = history.second,
            doneCount = summary.doneCount,
            missedCount = summary.missedCount,
            successRate = summary.successRate,
            totalXp = summary.totalXp,
            totalHabits = summary.totalHabits,
            topDoneHabits = summary.topDoneHabits,
            topMissedHabits = summary.topMissedHabits,
            isLoading = false
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )
}
