package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A polished linear progress bar with the classic gradient brush.
 */
@Composable
fun LinearProgressBar(
    percentage: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF386641),
    backgroundColor: Color = Color(0xFF386641).copy(alpha = 0.12f)
) {
    val polishedGradient = Brush.linearGradient(
        colors = listOf(
            color.copy(alpha = 0.9f),
            Color(0xFF8DB58D),
            color
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(percentage.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(polishedGradient)
        )
    }
}
