package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

@Composable
fun StreakCard(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val brandOrange = Color(0xFFFF5722)
    val brandGold = Color(0xFFFFD600)

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
                        // Sophisticated multi-layered glow for a premium feel
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to brandOrange.copy(alpha = 0.18f),
                                0.6f to brandOrange.copy(alpha = 0.04f),
                                1.0f to Color.Transparent,
                            ),
                            radius = size.maxDimension / 1.2f
                        )
                    }
            ) {
                // Symmetrical Refined Flame Icon for a more professional look
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp),
                    tint = brandOrange
                )
            }

            Spacer(Modifier.height(16.dp))

            // Premium Typography: "12 day streak"
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                        append(currentStreak.toString())
                    }
                    append(" ")
                    append(stringResource(R.string.detail_day_streak).lowercase())
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF212121), // High-contrast for premium look
                    letterSpacing = (-0.8).sp,
                    fontSize = 34.sp
                )
            )
            
            Text(
                text = "YOUR CONSISTENCY IS UNSTOPPABLE",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(56.dp))

            // Senior Level Milestone Timeline
            StreakTimeline(currentStreak = currentStreak)
        }
    }
}

@Composable
private fun StreakTimeline(currentStreak: Int) {
    val days = listOf(1, 2, currentStreak, currentStreak + 1, currentStreak + 2, currentStreak + 3)
        .distinct()
        .sorted()
        .take(6)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, day ->
            val isCompleted = day < currentStreak
            val isCurrent = day == currentStreak
            val isSpecial = day % 10 == 0 || day == 2 || (isCurrent && day > 1)
            
            TimelineNode(
                day = day,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                isSpecial = isSpecial
            )

            if (index < days.size - 1) {
                val nextDay = days[index + 1]
                val lineActive = isCompleted || (isCurrent && nextDay == currentStreak + 1)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (lineActive) Color(0xFFFF9100) else Color(0xFFDDE1E6).copy(alpha = 0.6f),
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun TimelineNode(
    day: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isSpecial: Boolean
) {
    val activeColor = if (isSpecial) Color(0xFFFFA000) else Color(0xFFFF5722)
    val inactiveColor = Color(0xFFDDE1E6)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
            if (isSpecial) {
                LaurelWreath(
                    modifier = Modifier.fillMaxSize(),
                    color = activeColor.copy(alpha = if (isCompleted || isCurrent) 1f else 0.25f)
                )
            }

            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = if (isCompleted || isCurrent) activeColor else inactiveColor,
                shadowElevation = if (isCurrent) 8.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSpecial && isCurrent) Icons.Rounded.Bolt else Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isCompleted || isCurrent) Color.White else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(10.dp))
        
        Text(
            text = "D$day",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                color = if (isCompleted || isCurrent) activeColor else Color.Gray.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun LaurelWreath(modifier: Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 1.2.dp.toPx()
        val radius = (size.minDimension / 2) - 4.dp.toPx()
        
        val arcPathLeft = Path().apply {
            addArc(
                oval = Rect(Offset(4.dp.toPx(), 4.dp.toPx()), Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())),
                startAngleDegrees = 100f,
                sweepAngleDegrees = 140f
            )
        }
        val arcPathRight = Path().apply {
            addArc(
                oval = Rect(Offset(4.dp.toPx(), 4.dp.toPx()), Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())),
                startAngleDegrees = 300f,
                sweepAngleDegrees = 140f
            )
        }
        
        drawPath(arcPathLeft, color, style = Stroke(width = strokeWidth))
        drawPath(arcPathRight, color, style = Stroke(width = strokeWidth))

        for (i in 0..6) {
            val angleLeft = 100f + (i * 23f)
            val angleRight = 300f + (i * 23f)
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
            topLeft = Offset(x - 2.dp.toPx(), y - 4.dp.toPx()),
            size = Size(4.dp.toPx(), 8.dp.toPx())
        )
    }
}

@Preview
@Composable
private fun StreakCardPreview() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            StreakCard(currentStreak = 12)
        }
    }
}
