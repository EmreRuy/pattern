package com.example.pattern.ui.screens.profileScreen.components.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SuccessInstrumentGauge(
    percentage: Float,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF588157),
    animDuration: Int = 2000,
) {
    var animationTarget by remember { mutableFloatStateOf(0f) }
    val curPercentage = animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
        label = "GaugeAnimation"
    )

    LaunchedEffect(percentage) {
        animationTarget = percentage.coerceIn(0f, 1f)
    }
    val tickCount = 20
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val strokeWidthPx = 11.dp.toPx()
        val tickPaddingPx = 11.dp.toPx()
        
        val radius = min(size.width / 2f, size.height) - (strokeWidthPx / 2f)
        val center = Offset(size.width / 2f, size.height - (strokeWidthPx / 2f))

        val arcRect = Rect(
            left = center.x - radius,
            top = center.y - radius,
            right = center.x + radius,
            bottom = center.y + radius
        )

        drawArc(
            color = primaryColor.copy(alpha = 0.08f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            topLeft = arcRect.topLeft,
            size = arcRect.size
        )

        for (i in 0..tickCount) {
            val angleDeg = 180f + (i * (180f / tickCount))
            val isMainTick = i % 5 == 0
            val currentTickHeight = if (isMainTick) 10.dp.toPx() else 6.dp.toPx()
            
            val tickColor = if (angleDeg - 180f <= curPercentage.value * 180f + 0.1f)
                primaryColor.copy(alpha = 0.5f)
            else
                primaryColor.copy(alpha = 0.12f)

            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
            val innerR = radius - tickPaddingPx
            val outerR = innerR - currentTickHeight

            val start = Offset(
                x = center.x + innerR * cos(angleRad),
                y = center.y + innerR * sin(angleRad)
            )
            val end = Offset(
                x = center.x + outerR * cos(angleRad),
                y = center.y + outerR * sin(angleRad)
            )

            drawLine(
                color = tickColor,
                start = start,
                end = end,
                strokeWidth = if (isMainTick) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawArc(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f),
                    primaryColor,
                    primaryColor.copy(alpha = 0.7f)
                ),
                startX = arcRect.left,
                endX = arcRect.right
            ),
            startAngle = 180f,
            sweepAngle = 180f * curPercentage.value,
            useCenter = false,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            topLeft = arcRect.topLeft,
            size = arcRect.size
        )
        
        if (curPercentage.value > 0.01f) {
            val tipAngleRad = Math.toRadians(180.0 + (180.0 * curPercentage.value)).toFloat()
            val tipX = center.x + radius * cos(tipAngleRad)
            val tipY = center.y + radius * sin(tipAngleRad)
            
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }
    }
}
