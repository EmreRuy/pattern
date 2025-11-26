package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
    val ringSize = 48.dp
    val iconSize = 28.dp

    Box(
        modifier = Modifier
            .size(ringSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isCompleted) onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        val surfaceColor = MaterialTheme.colorScheme.surface
        // RING
        Canvas(Modifier.matchParentSize()) {
            drawArc(
                color = surfaceColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
        if (isCompleted) {
            val scale by animateFloatAsState(
                targetValue = if (showSuccess) 1f else 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = ""
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Done",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(iconSize * scale)
            )
        } else {
            val icon = when {
                isRunning -> Icons.Filled.Pause
                isPaused -> Icons.Filled.PlayArrow
                else -> Icons.Filled.PlayArrow
            }
            val tint = Color.Gray
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
