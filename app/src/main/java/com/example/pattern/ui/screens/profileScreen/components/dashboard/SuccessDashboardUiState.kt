package com.example.pattern.ui.screens.profileScreen.components.dashboard

import androidx.compose.runtime.Immutable
import com.example.pattern.domain.model.HabitStat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class SuccessDashboardUiState(
    val title: String = "Success Score",
    val doneCount: Int = 0,
    val missedCount: Int = 0,
    val successRate: Float = 0f,
    val statusText: String = "",
    val xpPoints: Int = 0,
    val topDoneHabits: ImmutableList<HabitStat> = persistentListOf(),
    val topMissedHabits: ImmutableList<HabitStat> = persistentListOf()
)
