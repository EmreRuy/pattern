package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import com.example.pattern.domain.model.HabitStat
import androidx.core.graphics.toColorInt
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Immutable
data class SuccessDashboardUiState(
    val title: String = "Success Score",
    val doneCount: Int = 0,
    val missedCount: Int = 0,
    val successRate: Float = 0f,
    val statusText: String = "",
    val xpPoints: Int = 0,
    val topDoneHabits: List<HabitStat> = emptyList(),
    val topMissedHabits: List<HabitStat> = emptyList(),
    val insight: InsightData = InsightData("", "", "", "")
)

@Immutable
data class InsightData(
    val title: String,
    val message: String,
    val action: String,
    val emoji: String
)

@Composable
fun ProfileExtraCard(
    uiState: SuccessDashboardUiState,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> SuccessScoreOverview(
                        title = uiState.title,
                        percentage = uiState.successRate,
                        statusText = uiState.statusText,
                        doneCount = uiState.doneCount,
                        missedCount = uiState.missedCount,
                        xpPoints = uiState.xpPoints
                    )

                    1 -> TopHabitsList(
                        title = "TOP 3 DONE",
                        habits = uiState.topDoneHabits,
                        emptyMessage = "Keep going! No habits done yet."
                    )

                    2 -> TopHabitsList(
                        title = "TOP 3 MISSED",
                        habits = uiState.topMissedHabits,
                        emptyMessage = "Perfect! No missed habits."
                    )

                    3 -> HabitInsights(insight = uiState.insight)
                }
            }

            // Pager Indicator
            Row(
                Modifier
                    .padding(top = 4.dp)
                    .height(4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 18f else 6f,
                        animationSpec = tween(durationMillis = 300),
                        label = "width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            .size(width = width.dp, height = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessScoreOverview(
    title: String,
    percentage: Float,
    statusText: String,
    doneCount: Int,
    missedCount: Int,
    xpPoints: Int
) {
    // Lead Engineer Note: Hoisting animation to ensure synchronized motion between Gauge and Text.
    // Specifying a duration of 1800ms with FastOutSlowInEasing for a premium, non-linear feel.
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "SuccessScoreAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF588157))
            )
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SuccessInstrumentGauge(
                percentage = percentage, // Internal gauge uses its own matching animation
                primaryColor = Color(0xFF588157)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "${(animatedPercentage * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFF588157).copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "Done", value = doneCount.toString(), color = Color(0xFF386641))
            VerticalDivider()
            StatItem(label = "Missed", value = missedCount.toString(), color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            VerticalDivider()
            StatItem(label = "Total XP", value = formatXPLabel(xpPoints.toFloat()), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .size(1.dp, 20.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    )
}

@Composable
private fun TopHabitsList(
    title: String,
    habits: List<HabitStat>,
    emptyMessage: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Header without accent line
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (habits.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            // High-performance list rendering
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val top3 = remember(habits) { habits.take(3) }

                top3.forEachIndexed { index, habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rank Indicator with consistent width
                        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.CenterStart) {
                            val rankColor = if (title.contains("MISSED", ignoreCase = true)) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                            } else {
                                Color(0xFF588157).copy(alpha = 0.6f)
                            }
                            Text(
                                text = "#${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = rankColor
                                )
                            )
                        }

                        // Icon Backdrop
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    try { Color(habit.colorHex.toColorInt()).copy(alpha = 0.12f) }
                                    catch (_: Exception) { MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = habit.iconCode,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Text Content
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        // Numeric Count
                        Text(
                            text = habit.count.toString(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
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
private fun HabitInsights(insight: InsightData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        // Decorative Emoji Header
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = insight.emoji,
                fontSize = 32.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 26.sp,
                    letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Action Coaching - Refined (Removed tinted background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )
            Text(
                text = insight.action,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.2).sp,
                fontSize = 15.sp
            ),
            color = color
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                fontSize = 9.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun SuccessInstrumentGauge(
    percentage: Float,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF588157),
    animDuration: Int = 1800,
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
            .fillMaxWidth(0.8f)
            .height(110.dp)
    ) {
        val strokeWidthPx = 10.dp.toPx()
        val tickPaddingPx = 10.dp.toPx()
        
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
            val currentTickHeight = if (isMainTick) 8.dp.toPx() else 4.dp.toPx()
            
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
                radius = 2.5.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }
    }
}
