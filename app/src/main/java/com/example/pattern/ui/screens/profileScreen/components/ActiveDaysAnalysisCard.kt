package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.domain.model.ActiveDaysAnalysis
import com.example.pattern.domain.model.DayCompletionRate

/**
 * Staff-engineered Active Days Analysis Card.
 * High-performance UI that visualizes behavioral patterns through a minimalist bar chart.
 */
@Composable
fun ActiveDaysAnalysisCard(
    modifier: Modifier = Modifier,
    analysis: ActiveDaysAnalysis,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE DAYS ANALYSIS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bar Chart
            ActiveDaysBarChart(
                dailyRates = analysis.dailyRates,
                worstDay = analysis.worstDay
            )

            // Insight Section
            analysis.insightMessage?.let { message ->
                Spacer(modifier = Modifier.height(32.dp))
                BehavioralInsightBlock(message = message)
            }
        }
    }
}

@Composable
private fun ActiveDaysBarChart(
    dailyRates: List<DayCompletionRate>,
    worstDay: Int?
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        dailyRates.forEachIndexed { index, data ->
            val dayLabel = dayLabels.getOrNull(index) ?: ""
            val isWorst = data.dayOfWeek == worstDay
            
            BarItem(
                label = dayLabel,
                rate = data.rate,
                isWorst = isWorst,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BarItem(
    label: String,
    rate: Float,
    isWorst: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = rate,
        animationSpec = tween(durationMillis = 1000),
        label = "BarHeight"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            )
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight(animatedProgress.coerceIn(0.05f, 1f))
                    .clip(CircleShape)
                    .background(
                        if (isWorst && rate < 0.8f) 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isWorst) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = if (isWorst) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun BehavioralInsightBlock(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
