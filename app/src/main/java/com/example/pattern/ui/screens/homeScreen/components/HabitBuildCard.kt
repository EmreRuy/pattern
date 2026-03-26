package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
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
            .let {
                if (isDark) ColorUtils.blendARGB(it.toArgb(), surface.toArgb(), 0.35f)
                    .let(::Color) else it
            }
    }
    val showSuccess = remember { mutableStateOf(false) }
    val totalMillis = remember(habit.durationInMinutes) { (habit.durationInMinutes ?: 0) * 60_000L }
    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }
    val timerData by remember(
        currentTime,
        habit.timerStartTime,
        habit.timerPauseTime,
        habit.isCompleted
    ) {
        derivedStateOf {
            val remaining = when {
                habit.isCompleted -> 0L
                habit.timerStartTime == null -> totalMillis
                habit.timerPauseTime != null -> (totalMillis - (habit.timerPauseTime - habit.timerStartTime)).coerceAtLeast(
                    0L
                )

                else -> (totalMillis - (currentTime - habit.timerStartTime)).coerceAtLeast(0L)
            }
            val sec = (remaining / 1000).coerceAtLeast(0)
            val h = sec / 3600
            val m = (sec % 3600) / 60
            val s = sec % 60

            val formatted = if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
            val prog =
                if (totalMillis == 0L) 0f else 1f - (remaining.toFloat() / totalMillis.toFloat()).coerceIn(
                    0f,
                    1f
                )

            Triple(remaining, formatted, prog)
        }
    }
    val (remainingTime, formattedTime, progress) = timerData
    LaunchedEffect(remainingTime) {
        if (!habit.isCompleted && remainingTime <= 0 && habit.timerStartTime != null) {
            showSuccess.value = true
            onTimerFinished(habit)
            delay(1200)
            showSuccess.value = false
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCardClick(habit.id) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Icon and Title
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.iconEmoji.orEmpty(),
                    fontSize = 30.sp,
                    modifier = Modifier.padding(end = 14.dp)
                )

                Column {
                    Text(
                        text = habit.name.replaceFirstChar { it.uppercase() },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 19.sp
                        )
                    )
                    if (totalMillis > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
            TimerRing(
                progress = progress,
                accentColor = accentColor,
                isCompleted = habit.isCompleted,
                isRunning = habit.timerStartTime != null && habit.timerPauseTime == null,
                isPaused = habit.timerStartTime != null && habit.timerPauseTime != null,
                showSuccess = showSuccess.value,
                onClick = {
                    if (!isToday) return@TimerRing
                    val isRunning = habit.timerStartTime != null && habit.timerPauseTime == null
                    val isPaused = habit.timerStartTime != null && habit.timerPauseTime != null

                    when {
                        habit.isCompleted -> Unit
                        isRunning -> onPauseTimer(habit)
                        isPaused -> onResumeTimer(habit)
                        else -> onStartTimer(habit)
                    }
                }
            )
        }
    }
}


