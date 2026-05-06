package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val growColor = Color(0xFF22C55E)
    val quitColor = Color(0xFFFB7185)
    val taskColor = Color(0xFF6366F1)

    val totalXP = distribution.totalXP
    
    val buildPercentage = distribution.buildPercentage
    val quitPercentage = distribution.quitPercentage
    val taskPercentage = distribution.taskPercentage

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(distribution) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = Color.LightGray
            )
            
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ring Chart
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 20.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radius = diameter / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // Background track
                        drawCircle(
                            color = Color(0xFFF8F9FA),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )

                        var currentAngle = -90f
                        
                        // Build Segment
                        val buildSweep = 360f * buildPercentage * animatedProgress.value
                        if (buildSweep > 0) {
                            drawArc(
                                color = growColor,
                                startAngle = currentAngle,
                                sweepAngle = buildSweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            currentAngle += 360f * buildPercentage
                        }

                        // Quit Segment
                        val quitSweep = 360f * quitPercentage * animatedProgress.value
                        if (quitSweep > 0) {
                             drawArc(
                                color = quitColor,
                                startAngle = currentAngle,
                                sweepAngle = quitSweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            currentAngle += 360f * quitPercentage
                        }

                        // Task Segment
                        val taskSweep = 360f * taskPercentage * animatedProgress.value
                        if (taskSweep > 0) {
                            drawArc(
                                color = taskColor,
                                startAngle = currentAngle,
                                sweepAngle = taskSweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalXP.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = "TOTAL XP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(36.dp))

                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    LegendItem(color = growColor, label = "Grow", percentage = buildPercentage)
                    LegendItem(color = quitColor, label = "Quit", percentage = quitPercentage)
                    LegendItem(color = taskColor, label = "Task", percentage = taskPercentage)
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Insight message
            val topCategory = when {
                buildPercentage >= quitPercentage && buildPercentage >= taskPercentage -> "Growth"
                quitPercentage >= buildPercentage && quitPercentage >= taskPercentage -> "Quitting"
                else -> "Daily Tasks"
            }
            
            val topPercent = (maxOf(buildPercentage, quitPercentage, taskPercentage) * 100).toInt()

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$topPercent% of your XP comes from $topCategory habits. Your journey is leaning towards systemic improvement.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
