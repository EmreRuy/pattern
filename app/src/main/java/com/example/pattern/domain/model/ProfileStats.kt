package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ProfileStats(
    val levelInfo: LevelInfo,
    val weeklyXpHistory: ImmutableList<XPDataPoint>,
    val xpHistory: ImmutableList<XPDataPoint>,
    val yearlyXpHistory: ImmutableList<XPDataPoint>,
    val doneCount: Int,
    val missedCount: Int,
    val successRate: Float,
    val totalXp: Int,
    val totalHabits: Int,
    val topDoneHabits: ImmutableList<HabitStat>,
    val topMissedHabits: ImmutableList<HabitStat>,
    val bestStreaks: ImmutableList<StreakStat> = persistentListOf(),
    val xpDistribution: XPDistribution = XPDistribution(),
    val activeDaysAnalysis: ActiveDaysAnalysis = ActiveDaysAnalysis(persistentListOf())
)

@Immutable
data class ActiveDaysAnalysis(
    val dailyRates: ImmutableList<DayCompletionRate>,
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
