package com.example.pattern.ui.screens.profileScreen.components.dashboard

import androidx.compose.runtime.Immutable
import com.example.pattern.domain.model.HabitStat

@Immutable
data class SuccessDashboardUiState(
    val title: String = "Success Score",
    val doneCount: Int = 0,
    val missedCount: Int = 0,
    val successRate: Float = 0f,
    val statusText: String = "",
    val xpPoints: Int = 0,
    val topDoneHabits: List<HabitStat> = emptyList(),
    val topMissedHabits: List<HabitStat> = emptyList()
)
