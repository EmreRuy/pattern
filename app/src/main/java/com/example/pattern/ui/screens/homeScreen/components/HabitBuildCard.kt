package com.example.pattern.ui.screens.homeScreen.components

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
 * Optimized HabitBuildCard.
 * 
 * Performance Enhancements:
 * 1. Uses State objects inside derivedStateOf without destructuring at the top level, 
 *    preventing the entire card from recomposing every second.
 * 2. Recomposition is isolated to the subtitle and action lambdas.
 * 3. LaunchedEffect uses a specific derived finished state to avoid redundant executions.
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

    // Isolate the ticking state to this specific card instance.
    // We keep the State object reference stable.
    val currentTimeState = produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    // timerDataState handles the updates efficiently.
    // It depends on currentTimeState.value but doesn't cause recomposition of the outer function
    // as long as timerDataState.value is not read here.
    val timerDataState = remember(habit.timerStartTime, habit.timerPauseTime, habit.isCompleted, totalMillis) {
        derivedStateOf {
            calculateTimerData(habit, totalMillis, currentTimeState.value)
        }
    }

    val showSuccess = remember { mutableStateOf(false) }

    // Isolate the "is finished" check to avoid re-triggering LaunchedEffect every second.
    val isTimerFinished = remember(habit.isCompleted, habit.timerStartTime) {
        derivedStateOf {
            !habit.isCompleted && timerDataState.value.first <= 0 && habit.timerStartTime != null
        }
    }

    LaunchedEffect(isTimerFinished.value) {
        if (isTimerFinished.value) {
            showSuccess.value = true
            onTimerFinished(habit)
            delay(1200)
            showSuccess.value = false
        }
    }

    BaseHabitCard(
        habit = habit,
        onCardClick = onCardClick,
        subtitle = {
            if (totalMillis > 0) {
                // Reading timerDataState.value here isolates recomposition to this lambda.
                val formattedTime = if (habit.isCompleted) {
                    stringResource(R.string.habit_goal_reached)
                } else {
                    timerDataState.value.second
                }
                
                Text(
                    text = formattedTime,
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
            // Reading progress here isolates recomposition to this lambda.
            val progress = timerDataState.value.third
            TimerRing(
                progress = progress,
                isCompleted = habit.isCompleted,
                isRunning = habit.timerStartTime != null && habit.timerPauseTime == null,
                isPaused = habit.timerStartTime != null && habit.timerPauseTime != null,
                showSuccess = showSuccess.value,
                onClick = {
                    if (!isToday) return@TimerRing
                    if (habit.isCompleted) {
                        onUnfinishTimer(habit.id)
                        return@TimerRing
                    }
                    val isRunning = habit.timerStartTime != null && habit.timerPauseTime == null
                    val isPaused = habit.timerStartTime != null && habit.timerPauseTime != null
                    when {
                        isRunning -> onPauseTimer(habit)
                        isPaused -> onResumeTimer(habit)
                        else -> onStartTimer(habit)
                    }
                }
            )
        }
    )
}

private fun calculateTimerData(
    habit: HabitCardModel,
    totalMillis: Long,
    currentTime: Long
): Triple<Long, String, Float> {
    val remaining = when {
        habit.isCompleted -> 0L
        habit.timerStartTime == null -> totalMillis
        habit.timerPauseTime != null ->
            (totalMillis - (habit.timerPauseTime - habit.timerStartTime))
        else ->
            (totalMillis - (currentTime - habit.timerStartTime))
    }.coerceAtLeast(0L)

    val sec = (remaining / 1000).coerceAtLeast(0)
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60

    val formatted = if (h > 0)
        "%d:%02d:%02d".format(h, m, s)
    else
        "%02d:%02d".format(m, s)

    val progress = if (totalMillis == 0L) 0f
    else 1f - (remaining.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)

    return Triple(remaining, formatted, progress)
}
