package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    // Sliding window: 4 days starting from one day before current streak (e.g., if streak is 1, show D1-D4)
    val days = remember(currentStreak) {
        val startDay = (currentStreak - 1).coerceAtLeast(1)
        (startDay until startDay + 4).map { day ->
            TimelineDayState(
                day = day,
                isAchieved = day <= currentStreak,
                isTarget = day == currentStreak + 1,
                isMilestone = isMilestoneDay(day)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, state ->
            TimelineNode(state = state)

            if (index < days.size - 1) {
                // Line is active if the current node is achieved
                val isLineActive = state.isAchieved
                
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(4.dp)
                        .background(
                            color = if (isLineActive) StreakDesign.BrandOrange else StreakDesign.InactiveGray.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun TimelineNode(state: TimelineDayState) {
    val nodeColor = if (state.isAchieved) StreakDesign.BrandOrange else StreakDesign.InactiveGray.copy(alpha = 0.5f)
    val textColor = if (state.isAchieved) StreakDesign.BrandOrange else StreakDesign.TextSecondary.copy(alpha = 0.4f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            if (state.isMilestone) {
                MilestoneHalo(
                    modifier = Modifier.fillMaxSize(),
                    color = StreakDesign.MilestoneGold.copy(alpha = 0.6f)
                )
            }

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = nodeColor,
                shadowElevation = if (state.isAchieved) 8.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "D${state.day}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 12.sp
            )
        )
    }
}

private fun isMilestoneDay(day: Int): Boolean {
    val keyMilestones = setOf(2, 7, 14, 30, 50, 100, 365)
    return day in keyMilestones || (day > 0 && day % 50 == 0)
}

@Composable
private fun MilestoneHalo(modifier: Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = 24.dp.toPx()
        val count = 20
        
        for (i in 0 until count) {
            val angle = i * (360f / count)
            val angleRad = Math.toRadians(angle.toDouble())
            
            val x = centerX + radius * cos(angleRad).toFloat()
            val y = centerY + radius * sin(angleRad).toFloat()
            
            rotate(degrees = angle + 90f, pivot = Offset(x, y)) {
                drawOval(
                    color = color,
                    topLeft = Offset(x - 1.dp.toPx(), y - 3.dp.toPx()),
                    size = Size(2.dp.toPx(), 6.dp.toPx())
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakCardPreview() {
    MaterialTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StreakCard(currentStreak = 1)
        }
    }
}
