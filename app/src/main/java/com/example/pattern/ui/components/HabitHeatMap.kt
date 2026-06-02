package com.example.pattern.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableSet
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HabitHeatMap(
    completedDates: ImmutableSet<String>, // "YYYY-MM-DD" - Stable collection for better performance
    accentColor: Color,
    createdAt: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val weeksToShow = 53
    
    val startDate = remember(today) {
        today.minusWeeks((weeksToShow - 1).toLong())
            .minusDays((today.dayOfWeek.value - 1).toLong())
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    // High-performance Grid Data calculation to prevent frame drops during screen entry
    val gridData = remember(startDate, today, completedDates, createdAt, accentColor) {
        val futureColor = Color.LightGray.copy(alpha = 0.1f) // Simplified to avoid complex theme lookups in loop
        val baseColor = Color.LightGray.copy(alpha = 0.4f)
        
        List(weeksToShow) { w ->
            val weekStartDate = startDate.plusWeeks(w.toLong())
            List(7) { d ->
                val date = weekStartDate.plusDays(d.toLong())
                val dateString = date.toString()
                when {
                    date.isAfter(today) -> futureColor
                    date.isBefore(createdAt) -> baseColor
                    completedDates.contains(dateString) -> accentColor
                    else -> baseColor
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp) // Symmetric padding ensures space on both sides of the card
    ) {
        Column(
            modifier = Modifier
                .padding(top = 26.dp)
                .height(108.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("M", "W", "F", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            // Container for month labels with internal padding to match grid scroll
            Box(
                modifier = Modifier.padding(end = 24.dp)
            ) {
                val monthLabels = remember(startDate) {
                    (0 until weeksToShow).mapNotNull { w ->
                        val weekStartDate = startDate.plusWeeks(w.toLong())
                        if (weekStartDate.dayOfMonth <= 7) {
                            w to weekStartDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                        } else null
                    }
                }
                
                monthLabels.forEach { (w, label) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = (w * 16).dp) 
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.padding(end = 12.dp), // Extra trailing space for "Today"
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                gridData.forEach { weekColors ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.width(12.dp)
                    ) {
                        weekColors.forEach { cellColor ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
