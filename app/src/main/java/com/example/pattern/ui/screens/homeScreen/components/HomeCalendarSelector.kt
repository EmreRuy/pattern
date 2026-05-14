package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.utils.CalendarDayModel
import com.example.pattern.utils.toCalendarDayModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Premium, minimalist Calendar Selector for the Home Screen.
 * Engineered for maximum performance and a refined user experience.
 */
@Composable
fun HomeCalendarSelector(
    pagerState: PagerState,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    // The central pivot is "This Week" (the week containing today).
    // pageIndex 25,000 corresponds to the week of LocalDate.now().
    val pivotDate = remember { 
        LocalDate.now().minusDays((LocalDate.now().dayOfWeek.value - 1).toLong()) 
    }

    // Derived state for the month title to avoid unnecessary recompositions of the header
    val currentMonthTitle by remember {
        derivedStateOf {
            val weekOffset = pagerState.currentPage - 25000
            val dateInWeek = pivotDate.plusWeeks(weekOffset.toLong())
            dateInWeek.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(title = { currentMonthTitle })
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            pageSpacing = 0.dp,
            beyondViewportPageCount = 1,
            key = { it } // Use page index as key for stability
        ) { weekIndex ->
            val weekOffset = weekIndex - 25000
            val weekStartDate = remember(weekOffset) { pivotDate.plusWeeks(weekOffset.toLong()) }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CalendarSelectorDefaults.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { i ->
                    val date = remember(weekStartDate) { weekStartDate.plusDays(i.toLong()) }
                    val dayModel = remember(date) { date.toCalendarDayModel() }
                    val isSelected = selectedDate == date
                    val isToday = remember(date) { date == LocalDate.now() }
                    
                    CalendarItem(
                        isSelected = isSelected,
                        isToday = isToday,
                        day = dayModel
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(title: () -> String) {
    AnimatedContent(
        targetState = title(),
        transitionSpec = {
            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
             slideInVertically { it / 2 })
                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)))
        },
        label = "month_header_transition"
    ) { targetTitle ->
        Text(
            text = targetTitle,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 24.dp, bottom = 4.dp)
        )
    }
}

@Composable
private fun CalendarItem(
    isSelected: Boolean,
    isToday: Boolean,
    day: CalendarDayModel,
    modifier: Modifier = Modifier
) {
    val isWeekend = remember(day.date) {
        day.date.dayOfWeek == DayOfWeek.SATURDAY || day.date.dayOfWeek == DayOfWeek.SUNDAY
    }

    val selectionProgressState = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "selection_progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                // Subtle lift animation on selection using state value in lambda to avoid recomposition
                translationY = -CalendarSelectorDefaults.SelectionLift.toPx() * selectionProgressState.value
            }
    ) {
        DayLetterHeader(
            letter = day.dayLetter,
            isWeekend = isWeekend,
            isSelected = isSelected,
            isToday = isToday
        )

        Spacer(Modifier.height(CalendarSelectorDefaults.HeaderSpacing))

        DayNumberCircle(
            dayNumber = day.dayNumber,
            isSelected = isSelected,
            isToday = isToday,
            selectionProgress = { selectionProgressState.value }
        )
    }
}

@Composable
private fun DayLetterHeader(
    letter: String,
    isWeekend: Boolean,
    isSelected: Boolean,
    isToday: Boolean
) {
    val color = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Text(
        text = letter.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp
        ),
        color = color
    )
}

@Composable
private fun DayNumberCircle(
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean,
    selectionProgress: () -> Float
) {
    val selectionColor = MaterialTheme.colorScheme.primary
    val todayColor = MaterialTheme.colorScheme.secondary
    val surfaceContainerLowest = MaterialTheme.colorScheme.surfaceContainerLowest

    val contentColor = when {
        isSelected -> selectionColor
        isToday -> todayColor
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .size(CalendarSelectorDefaults.NumberCircleSize)
            .graphicsLayer {
                // Natural scaling effect using lambda for performance
                val progress = selectionProgress()
                val scale = 0.96f + (0.04f * progress)
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                val progress = selectionProgress()
                // background color updated to surfaceContainerLowest with smooth alpha transition
                drawCircle(
                    color = surfaceContainerLowest.copy(alpha = progress),
                )
                
                // Subtle thin border for a refined, minimalistic selection
                if (progress > 0f) {
                    drawCircle(
                        color = Color.Black.copy(alpha = progress * 0.08f),
                        style = Stroke(width = CalendarSelectorDefaults.SelectionBorderWidth.toPx())
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 15.sp
                ),
                color = contentColor
            )
            
            if (isToday) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(3.dp)
                        .graphicsLayer {
                            // Fade out dot as selection fills up for a cleaner transition
                            alpha = 1f - (selectionProgress() * 0.5f)
                        }
                        .background(todayColor, CircleShape)
                )
            }
        }
    }
}

private object CalendarSelectorDefaults {
    val NumberCircleSize = 40.dp
    val SelectionLift = 2.dp
    val HeaderSpacing = 8.dp
    val VerticalPadding = 12.dp
    val SelectionBorderWidth = 0.5.dp
}
