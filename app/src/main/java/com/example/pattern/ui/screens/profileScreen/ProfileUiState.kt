package com.example.pattern.ui.screens.profileScreen

import androidx.compose.runtime.Immutable
import com.example.pattern.domain.model.*
import com.example.pattern.ui.screens.profileScreen.components.dashboard.SuccessDashboardUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ProfileUiState(
    val levelInfo: LevelInfo? = null,
    val weeklyXpHistory: ImmutableList<XPDataPoint> = persistentListOf(),
    val xpHistory: ImmutableList<XPDataPoint> = persistentListOf(),
    val yearlyXpHistory: ImmutableList<XPDataPoint> = persistentListOf(),
    val successDashboard: SuccessDashboardUiState = SuccessDashboardUiState(),
    val xpDistribution: XPDistribution = XPDistribution(),
    val totalHabits: Int = 0,
    val successRate: Float = 0f,
    val bestStreaks: ImmutableList<StreakStat> = persistentListOf(),
    val streakInsight: InsightData? = null,
    val activeDaysAnalysis: ActiveDaysAnalysis = ActiveDaysAnalysis(persistentListOf()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
