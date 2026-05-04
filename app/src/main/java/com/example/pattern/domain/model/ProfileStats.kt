package com.example.pattern.domain.model

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

data class LevelInfo(
    val level: Int,
    val title: String,
    val currentXP: Int,
    val nextLevelXP: Int,
    val progress: Float
)

data class XPDataPoint(
    val dayIndex: Int,
    val dateLabel: String,
    val xpValue: Float
)

data class HabitStat(
    val name: String,
    val count: Int,
    val iconCode: String,
    val colorHex: String
)
