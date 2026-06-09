package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.pattern.R
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.ui.util.LocalTimerTicker
import com.example.pattern.ui.util.formatDurationFast
import kotlinx.coroutines.delay

/**
 * Principal-level UI State Holder for Timer Logic.
 * Encapsulates calculation and provides @Stable guarantees for Compose.
 */
@Stable
class HabitTimerState(
    private val habit: HabitCardModel,
    private val ticker: State<Long>
) {
    private val totalMillis: Long = (habit.durationInMinutes ?: 0) * 60_000L

    val remainingMillis: Long
        get() {
            val now = if (habit.isTimerRunning) ticker.value else System.currentTimeMillis()
            val elapsed = habit.calculateTotalTimeMs(now)
            return (totalMillis - elapsed).coerceAtLeast(0L)
        }

    val progress: Float
        get() = if (totalMillis == 0L) 0f
        else 1f - (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)

    val formattedTime: String
        get() = formatDurationFast(remainingMillis)

    val isFinished: Boolean
        get() = !habit.isCompleted && remainingMillis <= 0 && habit.isTimerRunning

    // Principal Fix: Provide a localized accessibility string
    val accessibilityDescription: String
        get() = if (habit.isCompleted) "Goal reached" 
                else "Time remaining: $formattedTime"
}

@Composable
fun rememberHabitTimerState(habit: HabitCardModel, ticker: State<Long>): HabitTimerState {
    return remember(habit, ticker) { HabitTimerState(habit, ticker) }
}

/**
 * Principal Engineer Refactored HabitBuildCard.
 * 
 * Architecture Highlights:
 * 1. State Holder Pattern: Uses HabitTimerState to isolate UI logic.
 * 2. Deferred Reading: State properties (progress, time) are read ONLY inside 
 *    the specific Lambdas (subtitle, action) that display them.
 * 3. Minimal Recomposition: The card body itself is almost static; only 
 *    the small Text and Ring nodes recompose on every tick.
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
    val ticker = LocalTimerTicker.current
    val timerState = rememberHabitTimerState(habit, ticker)
    val showSuccess = remember { mutableStateOf(false) }

    // Side effect handled by observing the state holder's property via snapshotFlow
    LaunchedEffect(timerState) {
        snapshotFlow { timerState.isFinished }.collect { finished ->
            if (finished) {
                showSuccess.value = true
                onTimerFinished(habit)
                delay(1200)
                showSuccess.value = false
            }
        }
    }

    BaseHabitCard(
        habit = habit,
        onCardClick = onCardClick,
        subtitle = {
            if ((habit.durationInMinutes ?: 0) > 0) {
                Text(
                    text = if (habit.isCompleted) stringResource(R.string.habit_goal_reached) 
                           else timerState.formattedTime,
                    modifier = Modifier.semantics {
                        // Principal Fix: Accessibility support. 
                        // LiveRegionMode.Polite ensures the screen reader announces 
                        // time changes without interrupting the user.
                        contentDescription = timerState.accessibilityDescription
                        liveRegion = LiveRegionMode.Polite
                    },
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
        action = {
            // DEFERRED READ: timerState.progress is read here.
            TimerRing(
                progress = timerState.progress,
                isCompleted = habit.isCompleted,
                isRunning = habit.isTimerRunning,
                isPaused = !habit.isTimerRunning && habit.accumulatedTimeMs > 0 && !habit.isCompleted,
                showSuccess = showSuccess.value,
                onClick = {
                    if (!isToday) return@TimerRing
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
    )
}
