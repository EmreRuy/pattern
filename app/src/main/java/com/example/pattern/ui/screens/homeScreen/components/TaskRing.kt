package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer


@Composable
fun TaskRing(
    checked: Boolean,
    onToggle: () -> Unit
) {
    val ringSize = 34.dp
    val iconSize = 20.dp
    val strokeWidthDp = 3.5.dp
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "taskProgress"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(350),
        label = "taskIconAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = 1f,
        label = "taskScale"
    )
    val backgroundRingColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(ringSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Spacer(
            Modifier
                .matchParentSize()
                .drawWithCache {
                    val stroke = Stroke(
                        width = strokeWidthDp.toPx(),
                        cap = StrokeCap.Round
                    )
                    onDrawBehind {
                        drawArc(
                            color = backgroundRingColor,
                            startAngle = 270f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = stroke
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = 270f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = stroke
                        )
                    }
                }
        )

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = primaryColor.copy(alpha = iconAlpha),
            modifier = Modifier.size(iconSize)
        )
    }
}

