package com.example.pattern.ui.screens.homeScreen.components

import android.os.SystemClock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.pattern.R
import com.example.pattern.ui.model.HabitCardModel
import kotlinx.coroutines.delay

/**
 * Staff Engineer Optimized HabitBuildCard.
 * 
 * Top 1% Refinements:
 * 1. Clock-Jump Protection: Uses SystemClock.elapsedRealtime() for the UI ticker.
 *    This ensures the countdown remains accurate even if the system clock shifts.
 */
@Composable
fun HabitBuildCard(
    habit: HabitCardModel,
    isToday: Boolean,
    onStartTimer: (HabitCardModel) -> Unit,
    onPauseTimer: (HabitCardModel) -> Unit,
    onResumeTimer: (HabitCardModel) -> Unit,
    onTimerFinished: (HabitCardModel) -> Unit,
    onUnfinishTimer: (Int) -> Unit,
    onCardClick: (Int) -> Unit,
) {
    val totalMillis = remember(habit.durationInMinutes) {
        (habit.durationInMinutes ?: 0) * 60_000L
    }

    // Capture baseline values to make the ticker immune to wall-clock changes.
    val baseTimeState = remember(habit.id, habit.activeSessionStartMs, habit.accumulatedTimeMs, habit.durationInMinutes) {
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        val initialElapsed = habit.calculateTotalTimeMs(nowWall)
        initialElapsed to nowElapsed
    }

    val currentElapsedMs = produceState(initialValue = baseTimeState.first, keys = arrayOf(baseTimeState, habit.isTimerRunning)) {
        if (habit.isTimerRunning) {
            while (true) {
                delay(1000)
                val nowElapsed = SystemClock.elapsedRealtime()
                val sessionIncrement = (nowElapsed - baseTimeState.second).coerceAtLeast(0L)
                value = baseTimeState.first + sessionIncrement
            }
        } else {
            value = baseTimeState.first
        }
    }

    // Derived states for UI
    val remainingMillis = remember(currentElapsedMs.value, totalMillis) {
        (totalMillis - currentElapsedMs.value).coerceAtLeast(0L)
    }

    val progress = remember(remainingMillis, totalMillis) {
        if (totalMillis == 0L) 0f
        else 1f - (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    }

    val formattedElapsed = remember(currentElapsedMs.value, totalMillis) {
        // Cap displayed elapsed time at the goal to prevent UI confusion
        val displayElapsed = if (habit.isCompleted) totalMillis else currentElapsedMs.value.coerceAtMost(totalMillis)
        formatDurationFast(displayElapsed)
    }

    val formattedTotal = remember(totalMillis) {
        formatDurationFast(totalMillis)
    }

    val showSuccess = remember { mutableStateOf(false) }

    val isTimerFinished = remember(habit.isCompleted, habit.activeSessionStartMs, remainingMillis) {
        !habit.isCompleted && remainingMillis <= 0 && habit.activeSessionStartMs != null
    }

    LaunchedEffect(isTimerFinished) {
        if (isTimerFinished) {
            showSuccess.value = true
            onTimerFinished(habit)
            delay(1200)
            showSuccess.value = false
        }
    }

    BaseHabitCard(
        habit = habit,
        onCardClick = onCardClick,
        enabled = !habit.isReadOnly,
        subtitle = {
            if (totalMillis > 0) {
                val text = if (habit.isCompleted) {
                    stringResource(R.string.habit_goal_reached)
                } else {
                    "$formattedElapsed / $formattedTotal"
                }
                
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFeatureSettings = "tnum",
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        action = { accentColor ->
            if (!habit.isReadOnly) {
                TimerRing(
                    progress = progress,
                    isCompleted = habit.isCompleted,
                    isRunning = habit.isTimerRunning,
                    isPaused = !habit.isTimerRunning && habit.accumulatedTimeMs > 0 && !habit.isCompleted,
                    showSuccess = showSuccess.value,
                    accentColor = accentColor,
                    enabled = isToday,
                    onClick = {
                        if (habit.isCompleted) {
                            onUnfinishTimer(habit.id)
                            return@TimerRing
                        }

                        when {
                            habit.isTimerRunning -> onPauseTimer(habit)
                            habit.accumulatedTimeMs > 0 -> onResumeTimer(habit)
                            else -> onStartTimer(habit)
                        }
                    }
                )
            }
        }
    )
}

/**
 * Staff-level manual formatter.
 * Avoids String.format which uses reflection and regex internally.
 */
private fun formatDurationFast(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString(8) {
        if (hours > 0) {
            append(hours)
            append(':')
            if (minutes < 10) append('0')
        }
        append(minutes)
        append(':')
        if (seconds < 10) append('0')
        append(seconds)
    }
}
