package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileStats(
    val levelInfo: LevelInfo,
    val xpHistory: List<XPDataPoint>,
    val yearlyXpHistory: List<XPDataPoint>,
    val doneCount: Int,
    val missedCount: Int,
    val successRate: Float,
    val totalXp: Int,
    val totalHabits: Int,
    val topDoneHabits: List<HabitStat>,
    val topMissedHabits: List<HabitStat>
)

@Immutable
data class HabitStatsSummary(
    val levelInfo: LevelInfo,
    val doneCount: Int,
    val missedCount: Int,
    val successRate: Float,
    val totalXp: Int,
    val totalHabits: Int,
    val topDoneHabits: List<HabitStat>,
    val topMissedHabits: List<HabitStat>
)

@Immutable
data class LevelInfo(
    val level: Int,
    val title: String,
    val currentXP: Int,
    val nextLevelXP: Int,
    val progress: Float
)

@Immutable
data class XPDataPoint(
    val dayIndex: Int,
    val dateLabel: String,
    val xpValue: Float
)

@Immutable
data class HabitStat(
    val name: String,
    val count: Int,
    val iconCode: String,
    val colorHex: String
)
