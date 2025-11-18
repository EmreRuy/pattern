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
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.ui.graphics.graphicsLayer
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

    Box(
        modifier = Modifier
            .size(ringSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isCompleted,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val color = MaterialTheme.colorScheme.surface
        // BACK ARC
        Canvas(Modifier.matchParentSize()) {
            drawArc(
                color = color,
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

        // ICON
        val icon = when {
            isCompleted -> Icons.Default.CheckCircle
            isRunning -> Icons.Filled.Pause
            isPaused -> Icons.Outlined.PlayArrow
            else -> Icons.Outlined.PlayArrow
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.scrim,
            modifier = Modifier.size(26.dp)
        )  // gonna work on here , the color and changing icons should be fixed for the best practice

        // SUCCESS ANIMATION
        if (showSuccess) {
            val scale by animateFloatAsState(
                targetValue = if (showSuccess) 1f else 0f,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                label = ""
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size((34 * scale).dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = scale
                    }
            )
        }
    }
}
