package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.pattern.R
import com.example.pattern.data.model.HabitCardModel
import kotlinx.coroutines.delay

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
    // Isolate the ticking state to this specific card instance, 
    // but we will pass it down to narrow the recomposition scope.
    val totalMillis = remember(habit.durationInMinutes) {
        (habit.durationInMinutes ?: 0) * 60_000L
    }

    // This state updates every second. 
    // By using it ONLY inside the 'subtitle' and 'action' blocks of BaseHabitCard,
    // we prevent the rest of the card (icon, name, background) from recomposing.
    val currentTime by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    val timerData by remember(currentTime, habit.timerStartTime, habit.timerPauseTime, habit.isCompleted) {
        derivedStateOf {
            calculateTimerData(habit, totalMillis, currentTime)
        }
    }

    val (remainingTime, formattedTime, progress) = timerData
    val showSuccess = remember { mutableStateOf(false) }

    LaunchedEffect(remainingTime) {
        if (!habit.isCompleted && remainingTime <= 0 && habit.timerStartTime != null) {
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
                Text(
                    text = if (habit.isCompleted) stringResource(R.string.habit_goal_reached) else formattedTime,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFeatureSettings = "tnum", // Tabular figures for stable timer width
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        action = { accentColor ->
            TimerRing(
                progress = progress,
                accentColor = accentColor,
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
