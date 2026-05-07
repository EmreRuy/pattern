package com.example.pattern.ui.screens.profileScreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.*
import com.example.pattern.domain.usecase.GetProfileStatsUseCase
import com.example.pattern.ui.screens.profileScreen.components.SuccessDashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileStatsUseCase: GetProfileStatsUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = getProfileStatsUseCase()
        .map { stats ->
            ProfileUiState(
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )

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

        // Staff Engineer: Deep Behavioral Analysis
        val totalHistoryCount = stats.weeklyXpHistory.size
        val recentTrend = if (totalHistoryCount >= 2) {
            val last = stats.weeklyXpHistory.last().xpValue
            val prev = stats.weeklyXpHistory[totalHistoryCount - 2].xpValue
            when {
                last > prev * 1.2f -> "UPWARD"
                last < prev * 0.8f -> "DOWNWARD"
                else -> "STABLE"
            }
        } else "NEW"

        val insight = when {
            // Case 1: High Success + Upward Trend (Momentum)
            successRate >= 0.8f && recentTrend == "UPWARD" -> {
                InsightData(
                    title = "PEAK PERFORMANCE",
                    message = "You're in a high-growth phase. Your momentum is increasing linearly with your consistency.",
                    action = "This is the optimal time to anchor a new challenging habit. Strike while the iron is hot.",
                    emoji = "🚀"
                )
            }
            // Case 2: High Done, but some specific misses (The Friction point)
            stats.topDoneHabits.isNotEmpty() && stats.topMissedHabits.isNotEmpty() -> {
                val toughHabit = stats.topMissedHabits.first().name
                InsightData(
                    title = "IDENTIFIED FRICTION",
                    message = "You've mastered most of your routine, but '$toughHabit' is acting as a cognitive bottleneck.",
                    action = "Try 'Habit Stacking': perform $toughHabit immediately after your best habit, '${stats.topDoneHabits.first().name}'.",
                    emoji = "🧠"
                )
            }
            // Case 3: Downward Trend (Burnout Warning)
            recentTrend == "DOWNWARD" && successRate < 0.6f -> {
                InsightData(
                    title = "RECOVERY MODE",
                    message = "We've detected a slight dip in engagement. Complexity might be causing burnout.",
                    action = "Strip back to just one 'Non-Negotiable' habit for the next 3 days. Rebuild the foundation.",
                    emoji = "🔋"
                )
            }
            // Case 4: Perfect Record (Mastery)
            successRate >= 0.95f && doneCount > 10 -> {
                InsightData(
                    title = "SYSTEM MASTERY",
                    message = "Your execution is flawless. You are no longer just tracking habits; you are embodying them.",
                    action = "Focus on the quality of execution now. How can you perform these same habits with more mindfulness?",
                    emoji = "🏆"
                )
            }
            // Case 5: Starting out or low data
            else -> {
                InsightData(
                    title = "FOUNDATION",
                    message = "Consistency is a skill, not a trait. Every 'Done' is a vote for the person you wish to become.",
                    action = "Focus on the 'Never Miss Twice' rule this week to stabilize your Success Score.",
                    emoji = "🏗️"
                )
            }
        }

        return SuccessDashboardUiState(
            title = "Success Score",
            doneCount = doneCount,
            missedCount = missedCount,
            successRate = successRate,
            statusText = statusText,
            xpPoints = stats.totalXp,
            topDoneHabits = stats.topDoneHabits,
            topMissedHabits = stats.topMissedHabits,
            insight = insight
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
