package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class XPDataPoint(val dayIndex: Int, val dateLabel: String, val xpValue: Float)

/**
 * A professional, minimalistic area chart interface card.
 * Features a clean white rounded rectangle, labeled Y-axis with Total XP,
 * and X-axis with dates.
 */
@Composable
fun XPProgressChartCard(
    modifier: Modifier = Modifier,
    title: String = "XP PROGRESS",
    dataPoints: List<XPDataPoint> = emptyList(),
    accentColor: Color = Color(0xFF386641)
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.LightGray
                    )
                    Text(
                        text = "Total XP Gained",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                val currentMax = dataPoints.maxOfOrNull { it.xpValue }?.toInt() ?: 0
                Text(
                    text = "Peak: $currentMax XP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = accentColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val maxYValue = (dataPoints.maxOfOrNull { it.xpValue } ?: 100f) * 1.2f
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Y-Axis Labels (Left)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val yLabels = listOf(maxYValue, maxYValue * 0.66f, maxYValue * 0.33f, 0f)
                    yLabels.forEach { value ->
                        Text(
                            text = formatXPLabel(value),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )
                    }
                }

                // Chart Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    ChartDrawing(
                        dataPoints = dataPoints,
                        maxYValue = maxYValue,
                        animationProgress = animationProgress.value,
                        accentColor = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // X-Axis Labels (Bottom - Dates)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp), // Approximate width of Y-axis labels + padding
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (dataPoints.isNotEmpty()) {
                    val first = dataPoints.first()
                    val mid = dataPoints[dataPoints.size / 2]
                    val last = dataPoints.last()
                    
                    Text(text = first.dateLabel, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    Text(text = mid.dateLabel, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    Text(text = last.dateLabel, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                }
            }
        }
    }
}

private fun formatXPLabel(value: Float): String {
    return when {
        value >= 1000f -> String.format(Locale.US, "%.1fk", value / 1000f)
        else -> value.toInt().toString()
    }
}

@Composable
private fun ChartDrawing(
    dataPoints: List<XPDataPoint>,
    maxYValue: Float,
    animationProgress: Float,
    accentColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Grid setup
        val gridColor = Color(0xFFF5F5F5)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        
        // Horizontal Grid
        for (i in 0..3) {
            val y = height * (i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
        }
        
        // Vertical Grid
        for (i in 0..2) {
            val x = width * (i / 2f)
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
        }

        if (dataPoints.size < 2) return@Canvas

        val points = dataPoints.map {
            Offset(
                x = ((it.dayIndex - 1) / 29f) * width,
                y = height - ((it.xpValue / maxYValue) * height * animationProgress)
            )
        }

        // Draw Area Fill
        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            var lastPoint = points.first()
            lineTo(lastPoint.x, lastPoint.y)
            
            for (i in 1 until points.size) {
                val currentPoint = points[i]
                val cp1 = Offset(lastPoint.x + (currentPoint.x - lastPoint.x) / 2, lastPoint.y)
                val cp2 = Offset(lastPoint.x + (currentPoint.x - lastPoint.x) / 2, currentPoint.y)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, currentPoint.x, currentPoint.y)
                lastPoint = currentPoint
            }
            lineTo(width, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
            )
        )

        // Draw Stroke Line
        val strokePath = Path().apply {
            var lastPoint = points.first()
            moveTo(lastPoint.x, lastPoint.y)
            for (i in 1 until points.size) {
                val currentPoint = points[i]
                val cp1 = Offset(lastPoint.x + (currentPoint.x - lastPoint.x) / 2, lastPoint.y)
                val cp2 = Offset(lastPoint.x + (currentPoint.x - lastPoint.x) / 2, currentPoint.y)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, currentPoint.x, currentPoint.y)
                lastPoint = currentPoint
            }
        }

        drawPath(
            path = strokePath,
            color = accentColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        
        // Last point indicator
        drawCircle(
            color = accentColor,
            radius = 5.dp.toPx(),
            center = points.last()
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = points.last()
        )
    }
}
