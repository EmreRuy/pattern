package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileStats(
    val levelInfo: LevelInfo,
    val weeklyXpHistory: List<XPDataPoint>,
    val xpHistory: List<XPDataPoint>,
    val yearlyXpHistory: List<XPDataPoint>,
    val doneCount: Int,
    val missedCount: Int,
    val successRate: Float,
    val totalXp: Int,
    val totalHabits: Int,
    val topDoneHabits: List<HabitStat>,
    val topMissedHabits: List<HabitStat>,
    val bestStreaks: List<StreakStat> = emptyList(),
    val xpDistribution: XPDistribution = XPDistribution(),
    val activeDaysAnalysis: ActiveDaysAnalysis = ActiveDaysAnalysis(emptyList())
)

@Immutable
data class ActiveDaysAnalysis(
    val dailyRates: List<DayCompletionRate>,
    val insightMessage: String? = null,
    val worstDay: Int? = null // 1..7
)

@Immutable
data class DayCompletionRate(
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val rate: Float
)

@Immutable
data class StreakStat(
    val name: String,
    val streakCount: Int,
    val iconCode: String,
    val colorHex: String
)

@Immutable
data class XPDistribution(
    val buildXP: Int = 0,
    val quitXP: Int = 0,
    val taskXP: Int = 0,
    val totalXP: Int = 0
) {
    val buildPercentage: Float get() = if (totalXP > 0) buildXP.toFloat() / totalXP else 0f
    val quitPercentage: Float get() = if (totalXP > 0) quitXP.toFloat() / totalXP else 0f
    val taskPercentage: Float get() = if (totalXP > 0) taskXP.toFloat() / totalXP else 0f
}

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
