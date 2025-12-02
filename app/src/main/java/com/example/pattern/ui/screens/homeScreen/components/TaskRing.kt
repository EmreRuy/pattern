package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment


@Composable
fun TaskRing(
    checked: Boolean,
    accentColor: Color,
    onToggle: () -> Unit
) {
    val ringSize = 44.dp
    val iconSize = 26.dp
    val strokeWidth = 4.dp

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
        targetValue = if (checked) 1.15f else 1f,
        animationSpec = tween(250),
        label = "taskScale"
    )
    val backgroundRingColor =
        MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .size(ringSize)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() },
        contentAlignment = Alignment.Center
    ) {

        Canvas(Modifier.matchParentSize()) {
            val stroke = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Background ring
            drawArc(
                color = backgroundRingColor,
                startAngle = 270f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            // Foreground animated ring
            drawArc(
                color = accentColor,
                startAngle = 270f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = stroke
            )
        }

        // CHECK ICON
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = accentColor.copy(alpha = iconAlpha),
            modifier = Modifier.size(iconSize)
        )
    }
}

