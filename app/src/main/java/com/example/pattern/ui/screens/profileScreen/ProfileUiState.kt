package com.example.pattern.ui.screens.profileScreen

import androidx.compose.runtime.Immutable
import com.example.pattern.domain.model.*
import com.example.pattern.ui.screens.profileScreen.components.dashboard.SuccessDashboardUiState

@Immutable
data class ProfileUiState(
    val levelInfo: LevelInfo? = null,
    val weeklyXpHistory: List<XPDataPoint> = emptyList(),
    val xpHistory: List<XPDataPoint> = emptyList(),
    val yearlyXpHistory: List<XPDataPoint> = emptyList(),
    val successDashboard: SuccessDashboardUiState = SuccessDashboardUiState(),
    val xpDistribution: XPDistribution = XPDistribution(),
    val totalHabits: Int = 0,
    val successRate: Float = 0f,
    val bestStreaks: List<StreakStat> = emptyList(),
    val streakInsight: InsightData? = null,
    val activeDaysAnalysis: ActiveDaysAnalysis = ActiveDaysAnalysis(emptyList()),
    val isLoading: Boolean = false
)
