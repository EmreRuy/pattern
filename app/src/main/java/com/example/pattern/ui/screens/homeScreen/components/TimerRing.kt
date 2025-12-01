package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TimerRing(
    progress: Float,
    accentColor: Color,
    isCompleted: Boolean,
    isRunning: Boolean,
    isPaused: Boolean,
    showSuccess: Boolean,
    onClick: () -> Unit
) {
    val ringSize = 50.dp
    val iconSize = 32.dp

    // animation for the progress fill
    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    ).value

    // Animation for Ring/Icon Color based on state
    val targetColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isRunning -> accentColor
        isPaused -> accentColor.copy(alpha = 0.6f) // Dimmer when paused
        else -> accentColor
    }

    val animatedRingColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "ringColorAnimation"
    )
    Box(
        modifier = Modifier
            .size(ringSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isCompleted) onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        val backgroundColor = MaterialTheme.colorScheme.surface
        val strokeWidth = 4.dp

        // ProgressIndicator
        Canvas(Modifier.matchParentSize()) {
            val strokeStyle = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

            // Background
            drawArc(
                color = backgroundColor,
                startAngle = 270f, // Start at 12 o'clock
                sweepAngle = 360f,
                useCenter = false,
                style = strokeStyle
            )

            // Foreground
            drawArc(
                color = animatedRingColor,
                startAngle = 270f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = strokeStyle
            )
        }

        // ICON LAYER
        val (icon, tint) = when {
            isCompleted -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
            isRunning -> Icons.Filled.Pause to MaterialTheme.colorScheme.onSurfaceVariant
            isPaused -> Icons.Filled.PlayArrow to MaterialTheme.colorScheme.onSurface
            else -> Icons.Filled.PlayArrow to MaterialTheme.colorScheme.onSurface
        }

        //Success Animation
        val successScale by animateFloatAsState(
            targetValue = if (showSuccess) 1.2f else 1.0f, // Pop effect
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "successScale"
        )

        Icon(
            imageVector = icon,
            contentDescription = when {
                isCompleted -> "Done"
                isRunning -> "Pause timer"
                else -> "Start timer"
            },
            tint = tint,
            modifier = Modifier
                .size(iconSize * successScale)
                .drawBehind {
                    if (!isCompleted) {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.05f),
                            radius = iconSize.toPx() / 2f,
                            center = center,
                            blendMode = BlendMode.Overlay
                        )
                    }
                }
        )
    }
}
