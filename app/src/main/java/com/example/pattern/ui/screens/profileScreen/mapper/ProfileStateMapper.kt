package com.example.pattern.ui.screens.profileScreen.mapper

import com.example.pattern.domain.model.*
import com.example.pattern.ui.screens.profileScreen.ProfileUiState
import com.example.pattern.ui.screens.profileScreen.components.dashboard.SuccessDashboardUiState

object ProfileStateMapper {

    fun mapToUiState(stats: ProfileStats): ProfileUiState {
        return ProfileUiState(
            levelInfo = stats.levelInfo,
            weeklyXpHistory = stats.weeklyXpHistory,
            xpHistory = stats.xpHistory,
            yearlyXpHistory = stats.yearlyXpHistory,
            successDashboard = calculateSuccessDashboard(stats),
            xpDistribution = stats.xpDistribution,
            totalHabits = stats.totalHabits,
            successRate = stats.successRate,
            bestStreaks = stats.bestStreaks,
            streakInsight = calculateStreakInsight(stats.bestStreaks),
            activeDaysAnalysis = stats.activeDaysAnalysis,
            isLoading = false
        )
    }

    private fun calculateSuccessDashboard(stats: ProfileStats): SuccessDashboardUiState {
        val doneCount = stats.doneCount
        val missedCount = stats.missedCount
        
        val successRate = if (doneCount + missedCount > 0) {
            doneCount.toFloat() / (doneCount + missedCount)
        } else 0f

        val statusText = when {
            successRate >= 0.9f -> "ELITE"
            successRate >= 0.75f -> "OPTIMAL"
            successRate >= 0.5f -> "STABLE"
            successRate >= 0.25f -> "GAINING"
            else -> "STARTING"
        }

        return SuccessDashboardUiState(
            title = "Success Score",
            doneCount = doneCount,
            missedCount = missedCount,
            successRate = successRate,
            statusText = statusText,
            xpPoints = stats.totalXp,
            topDoneHabits = stats.topDoneHabits,
            topMissedHabits = stats.topMissedHabits
        )
    }

    private fun calculateStreakInsight(bestStreaks: List<StreakStat>): InsightData? {
        val topStreak = bestStreaks.firstOrNull()?.streakCount ?: 0
        if (topStreak == 0) return null

        return when {
            topStreak >= 66 -> InsightData(
                title = "AUTOMATICITY REACHED",
                message = "You've surpassed the 66-day threshold for '${bestStreaks.first().name}'. This habit is now neurologically encoded.",
                action = "You no longer need willpower for this. Redirect that focus to your next big challenge.",
                emoji = "🧬"
            )
            topStreak >= 21 -> InsightData(
                title = "HABIT FORMATION",
                message = "Your 21-day consistency has built a strong neural pathway. Resistance is starting to fade.",
                action = "Don't break the chain now. You're in the 'Valley of Latent Potential'. Keep pushing.",
                emoji = "🏗️"
            )
            topStreak >= 7 -> InsightData(
                title = "MOMENTUM GAINED",
                message = "A full week of consistency! You've successfully transitioned from 'trying' to 'doing'.",
                action = "Prepare for tomorrow's friction. The second week is often where motivation dips.",
                emoji = "🔥"
            )
            else -> InsightData(
                title = "SPARK IGNITED",
                message = "Every legend starts with a few days. You are currently building the foundation of discipline.",
                action = "Focus on just showing up. The quality of work matters less than the act of showing up.",
                emoji = "🌱"
            )
        }
    }
}
