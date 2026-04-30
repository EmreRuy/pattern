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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import com.example.pattern.ui.screens.profileScreen.HabitStat


import androidx.core.graphics.toColorInt

@Composable
fun ProfileExtraCard(
    title: String = "Extra Score",
    percentage: Float,
    doneCount: Int,
    missedCount: Int,
    xpPoints: Int,
    topDoneHabits: List<HabitStat> = emptyList(),
    topMissedHabits: List<HabitStat> = emptyList(),
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
                    .height(260.dp), // Fixed height to prevent jumping
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> SuccessScoreOverview(
                        title = title,
                        percentage = percentage,
                        doneCount = doneCount,
                        missedCount = missedCount,
                        xpPoints = xpPoints
                    )

                    1 -> TopHabitsList(
                        title = "TOP 3 DONE",
                        habits = topDoneHabits,
                        emptyMessage = "Keep going! No habits done yet."
                    )

                    2 -> TopHabitsList(
                        title = "TOP 3 MISSED",
                        habits = topMissedHabits,
                        emptyMessage = "Perfect! No missed habits."
                    )

                    3 -> HabitInsights(
                        topDone = topDoneHabits,
                        topMissed = topMissedHabits
                    )
                }
            }

            // Pager Indicator - Minimalistic
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
    doneCount: Int,
    missedCount: Int,
    xpPoints: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(contentAlignment = Alignment.Center) {
            HalfCircularProgressBar(
                percentage = percentage,
                number = (percentage * 100).toInt()
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Done",
                value = doneCount.toString(),
                color = Color(0xFF386641)
            )

            Box(modifier = Modifier.size(1.dp, 24.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))

            StatItem(
                label = "Missed",
                value = missedCount.toString(),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )

            Box(modifier = Modifier.size(1.dp, 24.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))

            StatItem(
                label = "Total XP",
                value = formatXPLabel(xpPoints.toFloat()),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                habits.forEach { habit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Minimalist Icon Circle - 32dp
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    try { Color(habit.colorHex.toColorInt()).copy(alpha = 0.12f) }
                                    catch (_: Exception) { MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = habit.iconCode,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = habit.count.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
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
private fun HabitInsights(
    topDone: List<HabitStat>,
    topMissed: List<HabitStat>
) {
    val insight = remember(topDone, topMissed) {
        when {
            topDone.isNotEmpty() && topMissed.isEmpty() -> {
                InsightData(
                    title = "UNSTOPPABLE",
                    message = "Your consistency is legendary. You've mastered your current routine.",
                    action = "Time to level up? Consider adding a small, high-impact habit to your morning.",
                    emoji = "🔥"
                )
            }
            topDone.isNotEmpty() -> {
                val bestHabit = topDone.first().name
                val toughHabit = topMissed.first().name
                InsightData(
                    title = "MOMENTUM & FRICTION",
                    message = "You're crushing '$bestHabit', but '$toughHabit' seems to be a hurdle lately.",
                    action = "Try the '2-minute rule' for $toughHabit: make it so easy you can't say no.",
                    emoji = "⚖️"
                )
            }
            topMissed.isNotEmpty() -> {
                InsightData(
                    title = "FRESH START",
                    message = "The first step is always the hardest. Don't let the misses define you.",
                    action = "Pick one habit for tomorrow and focus only on showing up. Just 1% better.",
                    emoji = "🌱"
                )
            }
            else -> {
                InsightData(
                    title = "YOUR JOURNEY",
                    message = "Every master was once a beginner. Start small, stay consistent.",
                    action = "Track your first habit today to unlock personalized insights.",
                    emoji = "✨"
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${insight.emoji} ${insight.title}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = insight.message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                letterSpacing = 0.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = insight.action,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private data class InsightData(
    val title: String,
    val message: String,
    val action: String,
    val emoji: String
)

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

//I will fix this late, there is a padding, offset problem here, maybe I'll change the UI
@Composable
fun HalfCircularProgressBar(
    percentage: Float,
    number: Int,
    modifier: Modifier = Modifier,
    width: Dp = 180.dp,
    strokeWidth: Dp = 12.dp,
    // Using your established Sage Green color
    color: Color = Color(0xFF588157),
    animDuration: Int = 2000,
) {
    val height = width / 2
    var animationTarget by remember { mutableFloatStateOf(0f) }

    val curPercentage = animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(
            durationMillis = animDuration,
            easing = FastOutSlowInEasing
        ),
        label = "ProgressAnimation"
    )

    LaunchedEffect(percentage) {
        animationTarget = percentage.coerceIn(0f, 1f)
    }
    val polishedBrush = Brush.linearGradient(
        colors = listOf(
            color.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.primary,
            color
        )
    )
    Box(
        modifier = modifier.size(width = width, height = height),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(
                width = size.width - strokePx,
                height = (size.height * 2) - strokePx
            )

            // Background Track
            drawArc(
                color = color.copy(alpha = 0.12f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(strokePx, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = Offset(strokePx / 2, strokePx / 2)
            )

            // Active Progress Track
            drawArc(
                brush = polishedBrush,
                startAngle = 180f,
                sweepAngle = 180f * curPercentage.value,
                useCenter = false,
                style = Stroke(strokePx, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = Offset(strokePx / 2, strokePx / 2)
            )
        }
        Text(
            text = (curPercentage.value * number).toInt().toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
/*
@Composable
fun StreakFlame(
    streak: Int,
    maxStreak: Int = 30,
    size: Dp = 100.dp,
    flameColor: Color = MaterialTheme.colorScheme.primary
) {
    val fillFraction = (streak / maxStreak.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background flame (gray)
        Icon(
            painter = painterResource(id = R.drawable.settings), // use your flame drawable
            contentDescription = "Flame Background",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxSize()
        )

        // Filled flame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fillFraction)
                .background(flameColor)
        )
    }
} */


