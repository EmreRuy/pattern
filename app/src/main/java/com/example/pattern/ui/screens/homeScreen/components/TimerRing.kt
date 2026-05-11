package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun TimerRing(
    progress: Float,
    isCompleted: Boolean,
    isRunning: Boolean,
    isPaused: Boolean,
    showSuccess: Boolean,
    onClick: () -> Unit
) {
    val ringSize = 32.dp
    val iconSize = 20.dp
    val strokeWidthDp = 3.5.dp
    // Theme color capture
    val backgroundRingColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    //Progress Animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "timerProgress"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(350),
        label = "timerIconAlpha"
    )
    val successScale by animateFloatAsState(
        targetValue = if (showSuccess) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "successScale"
    )
    Box(
        modifier = Modifier
            .size(ringSize)
            .graphicsLayer {
                scaleX = successScale
                scaleY = successScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isCompleted) onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val stroke = Stroke(
                        width = strokeWidthDp.toPx(),
                        cap = StrokeCap.Round
                    )
                    onDrawBehind {
                        // Background Track
                        drawArc(
                            color = backgroundRingColor,
                            startAngle = 270f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = stroke
                        )
                        val arcColor = when {
                            isCompleted -> primaryColor
                            isPaused -> primaryColor.copy(alpha = 0.6f)
                            else -> primaryColor
                        }
                        drawArc(
                            color = arcColor,
                            startAngle = 270f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = stroke
                        )
                    }
                }
        )
        // ICON LAYER
        val icon = when {
            isCompleted -> Icons.Default.Check
            isRunning -> Icons.Filled.Pause
            else -> Icons.Filled.PlayArrow
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isCompleted) {
                primaryColor.copy(alpha = iconAlpha)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.size(iconSize)
        )
    }
}
