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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.utils.CalendarDayModel
import com.example.pattern.utils.CalendarMathProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * PRODUCTION-GRADE OPTIMIZED CALENDAR SELECTOR
 * Optimized to eliminate object allocation churn (String, Integer, LayoutNode) and minimize recomposition.
 */

// 1. Static caches to prevent high-frequency string and formatter allocations.
private object CalendarCache {
    private val dayNumberStrings = (1..31).associateWith { it.toString() }
    private val dayLetterCache = mutableMapOf<Pair<DayOfWeek, Locale>, String>()

    fun getDayNumber(day: Int): String = dayNumberStrings[day] ?: day.toString()

    fun getDayLetter(dayOfWeek: DayOfWeek, locale: Locale): String {
        return dayLetterCache.getOrPut(dayOfWeek to locale) {
            dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
        }
    }
}

private val monthFormatterCache = mutableMapOf<Locale, DateTimeFormatter>()
private fun getMonthFormatter(locale: Locale): DateTimeFormatter {
    return monthFormatterCache.getOrPut(locale) {
        DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    }
}

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

    val locale = LocalConfiguration.current.locales[0]
    val monthFormatter = remember(locale) { getMonthFormatter(locale) }

    // Optimization: Defer title reading to CalendarHeader via lambda to avoid main container recomposition.
    val currentMonthTitleState = remember(pagerState, pivotDate, monthFormatter) {
        derivedStateOf {
            val weekOffset = pagerState.currentPage - CalendarMathProvider.WEEK_PAGER_PIVOT
            val dateInWeek = pivotDate.plusWeeks(weekOffset.toLong())
            dateInWeek.format(monthFormatter)
        }
    }

    // Optimization: Capture changing inputs in stable states to preserve HorizontalPager's content lambda stability.
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)
    val currentSelectedDate by rememberUpdatedState(selectedDate)

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(titleProvider = { currentMonthTitleState.value })
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false,
            beyondViewportPageCount = 1,
            key = { it }
        ) { weekIndex ->
            val weekOffset = weekIndex - CalendarMathProvider.WEEK_PAGER_PIVOT
            
            // Optimization: Memoize the week data calculation. Use cached strings to prevent allocation churn.
            val weekDays = remember(weekOffset, pivotDate, locale) {
                val weekStartDate = pivotDate.plusWeeks(weekOffset.toLong())
                List(7) { i ->
                    val date = weekStartDate.plusDays(i.toLong())
                    CalendarDayModel(
                        date = date,
                        dayLetter = CalendarCache.getDayLetter(date.dayOfWeek, locale),
                        dayNumber = CalendarCache.getDayNumber(date.dayOfMonth),
                        fullDateString = "" // Unused in UI, avoid toString() allocation
                    )
                }.toImmutableList()
            }
            
            // Optimization: Isolated scope for week content.
            WeekRow(
                days = weekDays,
                selectedDate = currentSelectedDate,
                today = today,
                onDateSelected = currentOnDateSelected,
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
    Row(
        modifier = CalendarSelectorDefaults.WeekRowModifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            val date = day.date
            val isSelected = selectedDate == date
            val isToday = date == today
            
            // Optimization: Stable lambda reference per date prevents item invalidation.
            val onClick = remember(date, haptic) {
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDateSelected(date)
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

    // Optimization: Defer selection state read via lambda to avoid recomposing the whole Column.
    val progressProvider = remember(selectionProgressState) { { selectionProgressState.value } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, 
                onClick = onClick
            )
            .graphicsLayer {
                // Optimization: Read state inside lambda to avoid recomposing during animation.
                translationY = -CalendarSelectorDefaults.SelectionLift.toPx() * progressProvider()
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
            selectionProgress = progressProvider
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
