package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.domain.model.XPDistribution

@Composable
fun XPDistributionCard(
    modifier: Modifier = Modifier,
    distribution: XPDistribution,
    title: String = "XP DISTRIBUTION"
) {
    val buildColor = Color(0xFF22C55E)
    val quitColor = Color(0xFFFB7185)
    val taskColor = Color(0xFF6366F1)

    // Sequential animation states for high-fidelity entry
    val buildProgress = remember { Animatable(0f) }
    val quitProgress = remember { Animatable(0f) }
    val taskProgress = remember { Animatable(0f) }

    // Tap interaction for premium micro-interaction
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(distribution) {
        buildProgress.snapTo(0f)
        quitProgress.snapTo(0f)
        taskProgress.snapTo(0f)
        
        buildProgress.animateTo(
            distribution.buildPercentage, 
            tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        quitProgress.animateTo(
            distribution.quitPercentage, 
            tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        taskProgress.animateTo(
            distribution.taskPercentage, 
            tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        onClick = { /* Action on tap */ },
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Icon(
                    imageVector = Icons.Rounded.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Optimized Ring Chart using drawWithCache
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .drawWithCache {
                            val strokeWidth = 14.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            onDrawBehind {
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.03f),
                                    radius = (size.minDimension - strokeWidth) / 2,
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )

                                var currentStartAngle = -90f
                                
                                val buildSweep = 360f * buildProgress.value
                                if (buildSweep > 0) {
                                    drawArc(
                                        color = buildColor,
                                        startAngle = currentStartAngle,
                                        sweepAngle = buildSweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                        size = arcSize,
                                        topLeft = topLeft
                                    )
                                }
                                currentStartAngle += 360f * distribution.buildPercentage

                                val quitSweep = 360f * quitProgress.value
                                if (quitSweep > 0) {
                                    drawArc(
                                        color = quitColor,
                                        startAngle = currentStartAngle,
                                        sweepAngle = quitSweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                        size = arcSize,
                                        topLeft = topLeft
                                    )
                                }
                                currentStartAngle += 360f * distribution.quitPercentage

                                val taskSweep = 360f * taskProgress.value
                                if (taskSweep > 0) {
                                    drawArc(
                                        color = taskColor,
                                        startAngle = currentStartAngle,
                                        sweepAngle = taskSweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                        size = arcSize,
                                        topLeft = topLeft
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RollingNumberTicker(
                            value = distribution.totalXP,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = "XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    LegendItem(color = buildColor, label = "Growth", percentage = distribution.buildPercentage)
                    LegendItem(color = quitColor, label = "Quitting", percentage = distribution.quitPercentage)
                    LegendItem(color = taskColor, label = "Daily Tasks", percentage = distribution.taskPercentage)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            InsightBanner(distribution)
        }
    }
}

@Composable
private fun RollingNumberTicker(value: Int, style: TextStyle) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut())
            } else {
                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> height } + fadeOut())
            }.using(SizeTransform(clip = false))
        },
        label = "XP_Ticker"
    ) { targetValue ->
        Text(
            text = targetValue.toString(),
            style = style,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InsightBanner(distribution: XPDistribution) {
    val insight = remember(distribution) {
        val topCategory = when {
            distribution.buildPercentage >= distribution.quitPercentage &&
                    distribution.buildPercentage >= distribution.taskPercentage -> "Growth"

            distribution.quitPercentage >= distribution.buildPercentage &&
                    distribution.quitPercentage >= distribution.taskPercentage -> "Quitting"

            else -> "Daily Tasks"
        }

        val topPercent = (maxOf(
            distribution.buildPercentage,
            distribution.quitPercentage,
            distribution.taskPercentage
        ) * 100).toInt()
        
        "$topPercent% of your XP comes from $topCategory habits. Your journey is leaning towards systemic improvement."
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = insight,
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
