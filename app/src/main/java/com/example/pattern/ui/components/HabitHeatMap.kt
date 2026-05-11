package com.example.pattern.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HabitHeatMap(
    completedDates: Set<String>, // "YYYY-MM-DD"
    accentColor: Color,
    createdAt: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val weeksToShow = 53
    
    // Calculate the start date (aligned to Monday)
    val startDate = remember(today) {
        today.minusWeeks((weeksToShow - 1).toLong())
            .minusDays((today.dayOfWeek.value - 1).toLong())
    }

    val scrollState = rememberScrollState()

    // Auto-scroll to the end to show recent progress
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        // 1. Day Labels (Fixed on the left)
        Column(
            modifier = Modifier
                .padding(top = 24.dp) // Space for month labels
                .height(110.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("M", "W", "F", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // 2. Scrollable Area (Months + Grid)
        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            // Month labels
            Box(modifier = Modifier.fillMaxWidth()) {
                for (w in 0 until weeksToShow) {
                    val weekStartDate = startDate.plusWeeks(w.toLong())
                    // If this is the first week of a month, or the very first week shown
                    if (weekStartDate.dayOfMonth <= 7) {
                        Text(
                            text = weekStartDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(start = (w * 16).dp) // 12dp width + 4dp spacing
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // The HeatMap Grid
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (w in 0 until weeksToShow) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.width(12.dp)
                    ) {
                        for (d in 0 until 7) {
                            val date = startDate.plusWeeks(w.toLong()).plusDays(d.toLong())
                            val dateString = date.toString()
                            val isCompleted = completedDates.contains(dateString)
                            val isFuture = date.isAfter(today)
                            val isBeforeCreation = date.isBefore(createdAt)
                            val isToday = date == today

                            val targetColor = when {
                                isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                isBeforeCreation -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                isCompleted -> accentColor
                                isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }

                            val animatedColor by animateColorAsState(
                                targetValue = targetColor,
                                animationSpec = tween(durationMillis = 500),
                                label = "color"
                            )

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(animatedColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
