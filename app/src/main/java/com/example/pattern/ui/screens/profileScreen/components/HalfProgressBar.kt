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
import androidx.compose.material3.VerticalDivider
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


@Composable
fun ProfileExtraCard(
    title: String = "Extra Score",
    percentage: Float,
    doneCount: Int,
    missedCount: Int,
    xpPoints: Int,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(contentAlignment = Alignment.Center) {
                HalfCircularProgressBar(
                    percentage = percentage,
                    number = (percentage * 100).toInt()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Done",
                    value = doneCount.toString(),
                    color = Color(0xFF386641)
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                StatItem(
                    label = "Missed",
                    value = missedCount.toString(),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                StatItem(
                    label = "Total XP",
                    value = formatXPLabel(xpPoints.toFloat()),
                    color = MaterialTheme.colorScheme.onSurface
                )
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


