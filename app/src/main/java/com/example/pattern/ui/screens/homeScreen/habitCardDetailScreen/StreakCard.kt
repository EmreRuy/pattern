package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * Optimized Streak Branding Colors
 */
private object StreakDesign {
    val BrandOrange = Color(0xFFFF5722)
    val MilestoneGold = Color(0xFFFFD600)
    val InactiveGray = Color(0xFFDDE1E6)
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF9E9E9E)
}

@Composable
fun StreakCard(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = surfaceColor,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(top = 56.dp, bottom = 48.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High-End Iconic Centerpiece
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to StreakDesign.BrandOrange.copy(alpha = 0.15f),
                                0.6f to StreakDesign.BrandOrange.copy(alpha = 0.04f),
                                1.0f to Color.Transparent,
                            ),
                            radius = size.maxDimension / 1.2f
                        )
                    }
            ) {
                Icon(
                    imageVector = if (currentStreak >= 30) Icons.Rounded.EmojiEvents else Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = if (currentStreak >= 30) StreakDesign.MilestoneGold else StreakDesign.BrandOrange
                )
            }

            Spacer(Modifier.height(16.dp))

            // Premium Typography: "12 day streak" with pluralization logic
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                        append(currentStreak.toString())
                    }
                    append(" ")
                    val streakSuffix = if (currentStreak == 1) {
                         stringResource(R.string.detail_day_streak).lowercase()
                    } else {
                        // Simple fallback for pluralization if resource doesn't handle it
                        val base = stringResource(R.string.detail_day_streak).lowercase()
                        if (base.endsWith("s")) base else "${base}s"
                    }
                    append(streakSuffix)
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = StreakDesign.TextPrimary,
                    letterSpacing = (-0.8).sp,
                    fontSize = 34.sp
                )
            )
            
            Text(
                text = "YOUR CONSISTENCY IS UNSTOPPABLE",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = StreakDesign.TextSecondary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(56.dp))

            // Professional Sliding Timeline
            StreakTimeline(currentStreak = currentStreak)
        }
    }
}

@Immutable
private data class TimelineDayState(
    val day: Int,
    val isAchieved: Boolean,
    val isTarget: Boolean,
    val isMilestone: Boolean
)

@Composable
private fun StreakTimeline(currentStreak: Int) {
    // Sliding window logic: shows 5 days, centered around current streak where possible
    val days = remember(currentStreak) {
        val startDay = (currentStreak - 2).coerceAtLeast(1)
        (startDay until startDay + 5).map { day ->
            TimelineDayState(
                day = day,
                isAchieved = day <= currentStreak,
                isTarget = day == currentStreak + 1,
                isMilestone = isMilestoneDay(day)
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, state ->
            TimelineNode(state = state)

            if (index < days.size - 1) {
                val nextState = days[index + 1]
                // Line is active if it connects two achieved days or leads to the target
                val isLineActive = state.isAchieved && (nextState.isAchieved || nextState.isTarget)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 6.dp)
                        .background(
                            color = if (isLineActive) StreakDesign.BrandOrange else StreakDesign.InactiveGray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun TimelineNode(state: TimelineDayState) {
    val nodeColor = when {
        state.isAchieved && state.isMilestone -> StreakDesign.MilestoneGold
        state.isAchieved -> StreakDesign.BrandOrange
        else -> StreakDesign.InactiveGray
    }
    
    val icon = when {
        state.day >= 30 && state.isAchieved -> Icons.Rounded.EmojiEvents
        state.isMilestone && state.isAchieved -> Icons.Rounded.Bolt
        else -> Icons.Rounded.LocalFireDepartment
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            if (state.isMilestone) {
                LaurelWreath(
                    modifier = Modifier.fillMaxSize(),
                    color = when {
                        state.isAchieved -> StreakDesign.MilestoneGold
                        state.isTarget -> StreakDesign.MilestoneGold.copy(alpha = 0.15f)
                        else -> StreakDesign.InactiveGray.copy(alpha = 0.1f)
                    }
                )
            }

            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = nodeColor,
                shadowElevation = if (state.isAchieved && !state.isMilestone) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (state.isAchieved) Color.White else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(10.dp))
        
        Text(
            text = "D${state.day}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (state.isAchieved) FontWeight.Black else FontWeight.Bold,
                color = if (state.isAchieved) StreakDesign.BrandOrange else StreakDesign.TextSecondary.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        )
    }
}

private fun isMilestoneDay(day: Int): Boolean {
    val keyMilestones = setOf(2, 7, 14, 30, 50, 100, 365)
    return day in keyMilestones || (day > 0 && day % 50 == 0)
}

@Composable
private fun LaurelWreath(modifier: Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 1.dp.toPx()
        val radius = (size.minDimension / 2) - 4.dp.toPx()
        
        // Refined arc sizing for a more open, high-end feel
        val arcSize = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx())
        val arcOffset = Offset(5.dp.toPx(), 5.dp.toPx())

        // Left Arc - subtly open at the top
        drawArc(
            color = color,
            startAngle = 105f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )

        // Right Arc
        drawArc(
            color = color,
            startAngle = 300f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )

        // Optimized Leaf Density
        for (i in 0..6) {
            val angleLeft = 110f + (i * 22f)
            val angleRight = 305f + (i * 22f)
            drawLeaf(angleLeft, radius, color)
            drawLeaf(angleRight, radius, color)
        }
    }
}

private fun DrawScope.drawLeaf(angleDeg: Float, radius: Float, color: Color) {
    val angleRad = Math.toRadians(angleDeg.toDouble())
    val centerX = size.width / 2
    val centerY = size.height / 2
    val x = centerX + radius * cos(angleRad).toFloat()
    val y = centerY + radius * sin(angleRad).toFloat()
    
    rotate(degrees = angleDeg + 90f, pivot = Offset(x, y)) {
        drawOval(
            color = color,
            topLeft = Offset(x - 2.dp.toPx(), y - 3.dp.toPx()),
            size = Size(4.dp.toPx(), 7.dp.toPx())
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakCardPreview() {
    MaterialTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StreakCard(currentStreak = 10)
        }
    }
}
