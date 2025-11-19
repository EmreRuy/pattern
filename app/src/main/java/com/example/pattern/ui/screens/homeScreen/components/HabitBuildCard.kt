package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.data.model.HabitCardModel
import androidx.core.graphics.toColorInt
import com.example.pattern.ui.screens.addHabitScreen.components.blendColors
import kotlinx.coroutines.delay

@Composable
fun HabitBuildCard(
    habit: HabitCardModel,
    isToday: Boolean,
    onStartTimer: (HabitCardModel) -> Unit,
    onPauseTimer: (HabitCardModel) -> Unit,
    onResumeTimer: (HabitCardModel) -> Unit,
    onTimerFinished: (HabitCardModel) -> Unit,
    onCardClick: (Int) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val surface = MaterialTheme.colorScheme.surface
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    val accentColor = remember(habit.accentColorHex, isDark) {
        runCatching { Color(habit.accentColorHex.toColorInt()) }
            .getOrDefault(fallbackColor)
            .let { if (isDark) blendColors(it, surface, 0.35f) else it }
    }
    // Timer state
    val showSuccess = remember { mutableStateOf(false) }
    val totalMillis = (habit.durationInMinutes ?: 0) * 60_000L

    val isRunning = habit.timerStartTime != null && habit.timerPauseTime == null
    val isPaused = habit.timerStartTime != null && habit.timerPauseTime != null
    val isCompleted = habit.isCompleted

    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingTime = remember(
        currentTime,
        habit.timerStartTime,
        habit.timerPauseTime,
        habit.isCompleted
    ) {
        when {
            isCompleted -> 0L
            habit.timerStartTime == null -> totalMillis
            isPaused -> (totalMillis - (habit.timerPauseTime - habit.timerStartTime))
                .coerceAtLeast(0L)

            else -> (totalMillis - (currentTime - habit.timerStartTime))
                .coerceAtLeast(0L)
        }
    }

    LaunchedEffect(remainingTime) {
        if (!isCompleted && remainingTime <= 0) {
            showSuccess.value = true
            onTimerFinished(habit)
            delay(1200)
            showSuccess.value = false
        }
    }

    val formattedTime = remember(remainingTime) {
        val sec = (remainingTime / 1000).coerceAtLeast(0)
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60

        if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    val progress = remember(remainingTime, totalMillis) {
        if (totalMillis == 0L) 0f
        else 1f - (remainingTime.toFloat() / totalMillis.toFloat())
            .coerceIn(0f, 1f)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCardClick(habit.id) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //Left side
            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = habit.iconEmoji.orEmpty(),
                    fontSize = 30.sp,
                    modifier = Modifier.padding(end = 14.dp)
                )

                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            fontSize = 19.sp,
                            letterSpacing = (-0.3).sp
                        )
                    )

                    if ((habit.durationInMinutes ?: 0) > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
            TimerRing(
                progress = progress,
                accentColor = accentColor,
                isCompleted = isCompleted,
                isRunning = isRunning,
                isPaused = isPaused,
                showSuccess = showSuccess.value,
                onClick = {
                    if (!isToday) return@TimerRing
                    when {
                        isCompleted -> Unit
                        isRunning -> onPauseTimer(habit)
                        isPaused -> onResumeTimer(habit)
                        else -> onStartTimer(habit)
                    }
                }
            )
        }
    }
}


