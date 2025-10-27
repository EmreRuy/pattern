package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ProfileExtraCard(
    title: String = "Extra Score",
    percentage: Float,
    number: Int,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            HalfCircularProgressBar(
                percentage = percentage,
                number = number
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoSquare(label = "Done", number = 176, color = MaterialTheme.colorScheme.primary)
                InfoSquare(label = "Skip", number = 75, color = MaterialTheme.colorScheme.secondary)
                InfoSquare(
                    label = "Total XP",
                    number = 1286,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

//I will fix this late, there is a padding, offset problem here, maybe I'll change the UI
@Composable
fun HalfCircularProgressBar(
    percentage: Float,
    number: Int,
    fontSize: TextUnit = 32.sp,
    width: Dp = 170.dp,
    height: Dp = 150.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    strokeWidth: Dp = 12.dp,
    animDuration: Int = 3000,
    animDelay: Int = 0,
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val curPercentage = animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = animDelay
        ), label = ""
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(width = width, height = height)) {
            val arcSize = Size(size.width, size.height)
            // Draw background arc
            drawArc(
                color = backgroundColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
                size = arcSize,
                topLeft = Offset.Zero
            )
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f * curPercentage.value,
                useCenter = false,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
                size = arcSize,
                topLeft = Offset.Zero
            )
        }
        Box(
            modifier = Modifier.offset(y = (-height / 12))
        ) {
            Text(
                text = (curPercentage.value * number).toInt().toString(),
                fontSize = fontSize,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
            )
        }
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


