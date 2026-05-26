package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.utils.CalendarDayModel
import com.example.pattern.utils.CalendarMathProvider
import com.example.pattern.utils.toCalendarDayModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Optimized, premium minimalist Calendar Selector for the Home Screen.
 * Refactored for zero-churn runtime performance and high-frequency interaction.
 */
@Composable
fun HomeCalendarSelector(
    pagerState: PagerState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    
    // Optimization: Pivot date is stable for the session.
    val pivotDate = remember(today) { 
        today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }

    // Optimization: Defer title reading to CalendarHeader to avoid recomposing the main container on scroll.
    val currentMonthTitleState = remember(pagerState, pivotDate) {
        derivedStateOf {
            val weekOffset = pagerState.currentPage - CalendarMathProvider.WEEK_PAGER_PIVOT
            val dateInWeek = pivotDate.plusWeeks(weekOffset.toLong())
            dateInWeek.format(monthFormatter)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(titleProvider = { currentMonthTitleState.value })
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false, // Staff Decision: Unified navigation source
            beyondViewportPageCount = 1,
            key = { it }
        ) { weekIndex ->
            val weekOffset = weekIndex - CalendarMathProvider.WEEK_PAGER_PIVOT
            
            // Optimization: Pre-calculate week data into a stable list to minimize remember slots.
            val weekDays = remember(weekOffset, pivotDate) {
                val weekStartDate = pivotDate.plusWeeks(weekOffset.toLong())
                List(7) { i ->
                    weekStartDate.plusDays(i.toLong()).toCalendarDayModel()
                }.toImmutableList()
            }
            
            // Optimization: Isolated scope for week content to prevent parent recomposition.
            WeekRow(
                days = weekDays,
                selectedDate = selectedDate,
                today = today,
                onDateSelected = onDateSelected,
                haptic = haptic
            )
        }
    }
}

@Composable
private fun WeekRow(
    days: ImmutableList<CalendarDayModel>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)
    
    Row(
        modifier = CalendarSelectorDefaults.WeekRowModifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            val date = day.date
            val isSelected = selectedDate == date
            val isToday = date == today
            
            // Optimization: Stable lambda reference per date prevents CalendarItem from invalidating.
            val onClick = remember(date) {
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnDateSelected(date)
                }
            }

            CalendarItem(
                isSelected = isSelected,
                isToday = isToday,
                day = day,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun CalendarHeader(titleProvider: () -> String) {
    // Reading the state inside AnimatedContent's scope limits invalidation.
    AnimatedContent(
        targetState = titleProvider(),
        transitionSpec = {
            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
             slideInVertically { it / 2 })
                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)))
        },
        label = "month_header_transition"
    ) { targetTitle ->
        Text(
            text = targetTitle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWeekend = remember(day.date) {
        day.date.dayOfWeek == DayOfWeek.SATURDAY || day.date.dayOfWeek == DayOfWeek.SUNDAY
    }

    val selectionProgressState = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = CalendarSelectorDefaults.SelectionAnimation,
        label = "selection_progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple for high performance & minimalist aesthetics
                onClick = onClick
            )
            .graphicsLayer {
                // Optimization: Read state inside lambda to avoid recomposing Column during animation.
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
    val colorScheme = MaterialTheme.colorScheme
    val color = remember(isSelected, isToday, isWeekend, colorScheme) {
        when {
            isSelected -> colorScheme.primary
            isToday -> colorScheme.secondary
            isWeekend -> colorScheme.error.copy(alpha = 0.5f)
            else -> colorScheme.onSurface.copy(alpha = 0.35f)
        }
    }

    Text(
        text = letter.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
        letterSpacing = 0.5.sp,
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
    val colorScheme = MaterialTheme.colorScheme
    val surfaceContainerLowest = colorScheme.surfaceContainerLowest
    val onSurface = colorScheme.onSurface
    val primary = colorScheme.primary
    val secondary = colorScheme.secondary

    val contentColor = remember(isSelected, isToday, primary, secondary, onSurface) {
        when {
            isSelected -> primary
            isToday -> secondary
            else -> onSurface.copy(alpha = 0.7f)
        }
    }

    Box(
        modifier = Modifier
            .size(CalendarSelectorDefaults.NumberCircleSize)
            .graphicsLayer {
                // Optimization: Lambda-based property updates avoid recomposition.
                val progress = selectionProgress()
                val scale = 0.96f + (0.04f * progress)
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                val progress = selectionProgress()
                drawCircle(
                    color = surfaceContainerLowest.copy(alpha = progress),
                )
                
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
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected || isToday) FontWeight.Medium else FontWeight.Normal,
                fontSize = 15.sp,
                color = contentColor
            )
            
            if (isToday) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(3.dp)
                        .graphicsLayer {
                            // Smoothly fade out today-dot as selection circle fills.
                            alpha = 1f - (selectionProgress() * 0.5f)
                        }
                        .background(secondary, CircleShape)
                )
            }
        }
    }
}

private object CalendarSelectorDefaults {
    val NumberCircleSize = 40.dp
    val SelectionLift = 2.dp
    val HeaderSpacing = 8.dp
    val SelectionBorderWidth = 0.5.dp
    
    val SelectionAnimation = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val WeekRowModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
}
