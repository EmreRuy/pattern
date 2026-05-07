package com.example.pattern.utils

import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.LevelInfo

object ExperienceUtils {
    // XP Rewards
    private const val XP_TASK_COMPLETION = 15
    private const val XP_QUIT_COMPLETION = 20
    private const val XP_BUILD_BASE = 10
    private const val XP_BUILD_PER_15_MINS = 5

    /**
     * Calculates XP for a single habit completion.
     */
    fun calculateHabitXP(habit: Habit, dailyState: HabitDailyState): Int {
        val isDone = when (habit.type) {
            HabitType.BUILD -> dailyState.isCompleted
            HabitType.TASK, HabitType.QUIT -> dailyState.isTaskCompleted
        }
        
        if (!isDone) return 0

        return when (habit.type) {
            HabitType.TASK -> XP_TASK_COMPLETION
            HabitType.QUIT -> XP_QUIT_COMPLETION
            HabitType.BUILD -> {
                val durationBonus = ((habit.durationInMinutes ?: 0) / 15) * XP_BUILD_PER_15_MINS
                XP_BUILD_BASE + durationBonus
            }
        }
    }

    /**
     * Determines Level and Title based on total accumulated XP.
     */
    fun getLevelInfo(totalXP: Int): LevelInfo {
        val thresholds = listOf(0, 100, 300, 700, 1500, 3100, 6300, 12700, 25500, 51100, 102300, 204700)
        val titles = listOf(
            "Novice",        // Level 1
            "Beginner",      // Level 2
            "Apprentice",    // Level 3
            "Learner",       // Level 4
            "Practitioner",  // Level 5
            "Consistent",    // Level 6
            "Skilled",       // Level 7
            "Advanced",      // Level 8
            "Expert",        // Level 9
            "Elite",         // Level 10
            "Master",        // Level 11
            "Grandmaster"    // Level 12+
        )

        var level = 1
        for (i in 1 until thresholds.size) {
            if (totalXP >= thresholds[i]) {
                level = i + 1
            } else {
                break
            }
        }

        val currentLevelIdx = (level - 1).coerceAtMost(thresholds.size - 1)
        val nextLevelIdx = level.coerceAtMost(thresholds.size - 1)
        
        val currentThreshold = thresholds[currentLevelIdx]
        val nextThreshold = if (nextLevelIdx < thresholds.size) thresholds[nextLevelIdx] else thresholds.last() * 2
        
        val title = titles[currentLevelIdx]
        
        val progress = if (level >= thresholds.size) {
            1.0f 
        } else {
            (totalXP - currentThreshold).toFloat() / (nextThreshold - currentThreshold).toFloat()
        }

        return LevelInfo(
            level = level,
            title = title,
            currentXP = totalXP,
            nextLevelXP = nextThreshold,
            progress = progress.coerceIn(0f, 1f)
        )
    }
}
