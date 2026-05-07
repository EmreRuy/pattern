package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

import com.example.pattern.domain.model.XPDataPoint

/**
 * A professional, borderless area chart interface card.
 * Features animated page indicators, interactive tooltips, and padded drawing to prevent clipping.
 */
@Composable
fun XPProgressChartCard(
    modifier: Modifier = Modifier,
    title: String = "XP PROGRESS",
    weeklyDataPoints: List<XPDataPoint> = emptyList(),
    monthlyDataPoints: List<XPDataPoint> = emptyList(),
    yearlyDataPoints: List<XPDataPoint> = emptyList(),
    accentColor: Color = Color(0xFF386641)
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    // Borderless Card Container - Clean flat white background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(28.dp))
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
                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                    fadeOut(animationSpec = tween(90))
                        },
                        label = "TitleAnim"
                    ) { page ->
                        Text(
                            text = when (page) {
                                0 -> "Weekly"
                                1 -> "Monthly"
                                else -> "Yearly"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Page Indicator Dots
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 18.dp else 6.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.Black else Color.LightGray.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = true,
                pageSpacing = 16.dp
            ) { page ->
                val currentData = when (page) {
                    0 -> weeklyDataPoints
                    1 -> monthlyDataPoints
                    else -> yearlyDataPoints
                }
                ChartPage(
                    dataPoints = currentData,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun ChartPage(
    dataPoints: List<XPDataPoint>,
    accentColor: Color
) {
    val animationProgress = remember { Animatable(0f) }
    var selectedPoint by remember { mutableStateOf<XPDataPoint?>(null) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(dataPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    if (dataPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available yet",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }
    } else {
        Column {
            val currentMax = remember(dataPoints) { dataPoints.maxOfOrNull { it.xpValue }?.toInt() ?: 0 }
            val maxYValue = remember(dataPoints) { (dataPoints.maxOfOrNull { it.xpValue } ?: 0f).coerceAtLeast(100f) * 1.3f }

            // Tooltip / Stat Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = selectedPoint != null,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()
                ) {
                    selectedPoint?.let { point ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${point.dateLabel}: ",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${point.xpValue.toInt()} XP",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                    }
                }

                if (selectedPoint == null) {
                    Text(
                        text = "Peak: $currentMax XP",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val yLabels = listOf(maxYValue, maxYValue * 0.5f, 0f)
                    yLabels.forEach { value ->
                        Text(
                            text = formatXPLabel(value),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Gray.copy(alpha = 0.4f)
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
                        accentColor = accentColor,
                        onPointSelected = { point ->
                            if (selectedPoint != point) {
                                selectedPoint = point
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onRelease = { selectedPoint = null }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labels = if (dataPoints.size >= 3) {
                    listOf(dataPoints.first(), dataPoints[dataPoints.size / 2], dataPoints.last())
                } else dataPoints

                labels.forEach { point ->
                    Text(
                        text = point.dateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartDrawing(
    dataPoints: List<XPDataPoint>,
    maxYValue: Float,
    animationProgress: Float,
    accentColor: Color,
    onPointSelected: (XPDataPoint) -> Unit,
    onRelease: () -> Unit
) {
    var touchX by remember { mutableStateOf<Float?>(null) }
    val density = LocalDensity.current
    val paddingPx = with(density) { 16.dp.toPx() } // Internal padding to prevent point clipping

    val fillPath = remember { Path() }
    val strokePath = remember { Path() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(dataPoints) {
                detectTapGestures(
                    onPress = { offset ->
                        val width = size.width
                        val drawableWidth = width - (paddingPx * 2)
                        
                        touchX = offset.x
                        val index = (((offset.x - paddingPx) / drawableWidth) * (dataPoints.size - 1))
                            .roundToInt()
                            .coerceIn(0, dataPoints.size - 1)
                        onPointSelected(dataPoints[index])
                        
                        tryAwaitRelease()
                        touchX = null
                        onRelease()
                    }
                )
            }
            .pointerInput(dataPoints) {
                detectDragGestures(
                    onDragStart = { offset -> touchX = offset.x },
                    onDragEnd = { touchX = null; onRelease() },
                    onDragCancel = { touchX = null; onRelease() },
                    onDrag = { change, _ ->
                        val width = size.width
                        val drawableWidth = width - (paddingPx * 2)
                        
                        touchX = change.position.x
                        val index = (((change.position.x - paddingPx) / drawableWidth) * (dataPoints.size - 1))
                            .roundToInt()
                            .coerceIn(0, dataPoints.size - 1)
                        onPointSelected(dataPoints[index])
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val drawableWidth = width - (paddingPx * 2)
        val drawableHeight = height - (paddingPx * 2)
        val divisor = (dataPoints.size - 1).coerceAtLeast(1).toFloat()

        // Grid lines
        val gridColor = Color(0xFFF8F8F8)
        drawLine(gridColor, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, paddingPx), Offset(width, paddingPx), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, height - paddingPx), Offset(width, height - paddingPx), strokeWidth = 1.dp.toPx())

        if (dataPoints.size < 2) return@Canvas

        // Optimization: Avoid list allocation in draw loop
        // We still need the points for interaction and pulse, but we can avoid mapping the whole list twice
        val points = dataPoints.mapIndexed { index, point ->
            Offset(
                x = paddingPx + (index / divisor) * drawableWidth,
                y = (height - paddingPx) - ((point.xpValue / maxYValue) * drawableHeight * animationProgress)
            )
        }

        // Area Fill
        fillPath.reset()
        fillPath.moveTo(points.first().x, height - paddingPx)
        for (i in 0 until points.size) {
            val current = points[i]
            if (i == 0) fillPath.lineTo(current.x, current.y)
            else {
                val prev = points[i - 1]
                fillPath.cubicTo(prev.x + (current.x - prev.x) / 2, prev.y, prev.x + (current.x - prev.x) / 2, current.y, current.x, current.y)
            }
        }
        fillPath.lineTo(points.last().x, height - paddingPx)
        fillPath.close()
        drawPath(fillPath, Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.2f), Color.Transparent)))

        // Main Line
        strokePath.reset()
        strokePath.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val current = points[i]
            strokePath.cubicTo(prev.x + (current.x - prev.x) / 2, prev.y, prev.x + (current.x - prev.x) / 2, current.y, current.x, current.y)
        }
        drawPath(strokePath, accentColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Interaction indicators
        touchX?.let { x ->
            val index = (((x - paddingPx) / drawableWidth) * divisor).roundToInt().coerceIn(0, dataPoints.size - 1)
            val p = points[index]
            
            drawLine(accentColor.copy(alpha = 0.2f), Offset(p.x, paddingPx), Offset(p.x, height - paddingPx), strokeWidth = 2.dp.toPx())
            drawCircle(Color.White, 8.dp.toPx(), p)
            drawCircle(accentColor, 6.dp.toPx(), p)
        } ?: run {
            // Pulse on last point
            drawCircle(accentColor.copy(alpha = 0.1f * animationProgress), 12.dp.toPx(), points.last())
            drawCircle(accentColor, 5.dp.toPx(), points.last())
            drawCircle(Color.White, 2.dp.toPx(), points.last())
        }
    }
}

private fun formatXPLabel(value: Float): String {
    return when {
        value >= 1000f -> String.format(Locale.US, "%.1fk", value / 1000f)
        else -> value.toInt().toString()
    }
}
