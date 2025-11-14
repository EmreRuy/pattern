package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.data.model.HabitCard
import androidx.core.graphics.toColorInt
import com.example.pattern.ui.screens.addHabitScreen.components.blendColors
import kotlinx.coroutines.delay

@Composable
fun HabitBuildCard(
    habit: HabitCard,
    onTimerFinished: () -> Unit,
    onCardClick: (Int) -> Unit,
    onStartTimer: (HabitCard) -> Unit,
    onStopTimer: (HabitCard) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val surface = MaterialTheme.colorScheme.surface
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainer
    val accentColor = remember(habit.accentColorHex, isDark) {
        val base = try {
            Color(habit.accentColorHex.toColorInt())
        } catch (_: Exception) {
            fallbackColor
        }
        if (isDark) {
            blendColors(base, surface, 0.4f)
        } else {
            base
        }
    }
    val showSuccess = remember { mutableStateOf(false) }
    val totalMillis = (habit.durationInMinutes ?: 0) * 60 * 1000L

    //start time (may be null)
    val timerStartTime = habit.timerStartTime

    val isPlaying = remember(timerStartTime) { timerStartTime != null }


    // update "current time"
    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }

    //remaining time
    val remainingTime = remember(currentTime, timerStartTime) {
        if (timerStartTime == null) totalMillis
        else (totalMillis - (currentTime - timerStartTime)).coerceAtLeast(0L)
    }
    LaunchedEffect(remainingTime) {
        if (remainingTime <= 0) {
            showSuccess.value = true
            onTimerFinished()
            delay(1200)
            showSuccess.value = false
        }
    }

    val formattedTime = remember(remainingTime) {
        val totalSec = (remainingTime / 1000).coerceAtLeast(0)
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    val progress = remember(remainingTime, totalMillis) {
        if (totalMillis == 0L) 0f
        else 1f - (remainingTime.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCardClick(habit.id) },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(pressedElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(22.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side — Icon + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = habit.iconEmoji.orEmpty(),
                    fontSize = 26.sp,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                )
            }

            // Right side — Timer + Progress ring
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (habit.durationInMinutes != null && habit.durationInMinutes > 0) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        // Background ring
                        drawArc(
                            color = Color.Black,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                        // Foreground ring (progress)
                        drawArc(
                            color = accentColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                    }
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val color = MaterialTheme.colorScheme.surfaceVariant
                        val secondColor = MaterialTheme.colorScheme.onSurface
                        // Background circular progress ring
                        Canvas(Modifier.matchParentSize()) {
                            drawArc(
                                color = color,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 5f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = secondColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 5f, cap = StrokeCap.Round)
                            )
                        }
                        if (showSuccess.value) {
                            val scale by animateFloatAsState(
                                targetValue = if (showSuccess.value) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                ),
                                label = "successScale"
                            )

                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = accentColor.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .size((28 * scale).dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        alpha = scale
                                    }
                            )
                        }
                        Icon(
                            imageVector = if (showSuccess.value) {
                                Icons.Default.CheckCircle
                            } else if (isPlaying) {
                                Icons.Filled.Pause
                            } else {
                                Icons.Outlined.PlayArrow
                            },
                            contentDescription = when {
                                showSuccess.value -> "Habit Done"
                                isPlaying -> "Pause"
                                else -> "Play"
                            },
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .size(26.dp)
                                .clickable {
                                    if (isPlaying) {
                                        onStopTimer(habit)
                                    } else {
                                        onStartTimer(habit)
                                    }
                                }
                        )
                    }

                }
            }
        }
    }
}
