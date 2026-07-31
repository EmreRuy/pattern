package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TaskRing(
    checked: Boolean,
    onToggle: () -> Unit,
    accentColor: Color,
    taskCount: Int = 1,
    completedCount: Int = 0,
    enabled: Boolean = true
) {
    val ringSize = 32.dp
    val iconSize = 18.dp
    val strokeWidthDp = 4.0.dp

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val contentAlpha = if (enabled) 1f else 0.4f

    // High-performance progress animation
    val progress by animateFloatAsState(
        targetValue = when {
            checked -> 1f
            taskCount > 1 -> (completedCount.toFloat() / taskCount.toFloat()).coerceIn(0f, 1f)
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "taskProgress"
    )

    // Interactive scale feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "taskScale"
    )
    val standardColor = if (checked) accentColor else MaterialTheme.colorScheme.onSurface
    val trackColor = accentColor.copy(alpha = 0.12f)
    Box(
        modifier = Modifier
            .size(ringSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = contentAlpha
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        // Optimization: Use drawWithCache to avoid re-calculating Stroke and Paths
        Spacer(
            Modifier
                .matchParentSize()
                .drawWithCache {
                    val stroke = Stroke(
                        width = strokeWidthDp.toPx(),
                        cap = StrokeCap.Round
                    )
                    onDrawBehind {
                        // Draw Background Track
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = stroke
                        )
                        // Draw Active Progress
                        drawArc(
                            color = standardColor,
                            startAngle = 270f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = stroke
                        )
                    }
                }
        )

        // Seamless transition between number and checkmark
        AnimatedContent(
            targetState = checked,
            transitionSpec = {
                if (targetState) {
                    (scaleIn(animationSpec = spring(Spring.DampingRatioMediumBouncy)) + fadeIn())
                        .togetherWith(scaleOut() + fadeOut())
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            label = "TaskContentTransition"
        ) { isChecked ->
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = standardColor,
                    modifier = Modifier.size(iconSize)
                )
            } else if (taskCount > 1) {
                Text(
                    text = completedCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

