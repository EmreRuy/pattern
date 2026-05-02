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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.utils.CalendarDayModel
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
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    dayList: List<CalendarDayModel>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Derived state for the month title to avoid unnecessary recompositions of the header
    val currentMonthTitle by remember {
        derivedStateOf {
            val firstDayOfWeekIndex = pagerState.currentPage * 7
            if (firstDayOfWeekIndex in dayList.indices) {
                dayList[firstDayOfWeekIndex].date.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                )
            } else ""
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(title = currentMonthTitle)
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            pageSpacing = 0.dp,
            beyondViewportPageCount = 1,
            key = { it } // Use page index as key for stability
        ) { weekIndex ->
            val startDayIndex = remember(weekIndex) { weekIndex * 7 }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CalendarSelectorDefaults.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { i ->
                    val dayIndex = startDayIndex + i
                    if (dayIndex in dayList.indices) {
                        val day = dayList[dayIndex]
                        val isSelected = selectedDayIndex == dayIndex
                        val isToday = remember(day.date) { day.date == LocalDate.now() }
                        
                        CalendarItem(
                            isSelected = isSelected,
                            isToday = isToday,
                            day = day,
                            onDayClick = {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDaySelected(dayIndex)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(title: String) {
    AnimatedContent(
        targetState = title,
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
    onDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWeekend = remember(day.date) {
        day.date.dayOfWeek == DayOfWeek.SATURDAY || day.date.dayOfWeek == DayOfWeek.SUNDAY
    }

    val selectionProgress by animateFloatAsState(
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
            .semantics { role = Role.Button }
            .clip(RoundedCornerShape(CalendarSelectorDefaults.ItemCornerRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDayClick
            )
            .graphicsLayer {
                // Subtle lift animation on selection
                translationY = -CalendarSelectorDefaults.SelectionLift.toPx() * selectionProgress
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
            selectionProgress = selectionProgress
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
    selectionProgress: Float
) {
    val selectionColor = MaterialTheme.colorScheme.primary
    val todayColor = MaterialTheme.colorScheme.secondary
    
    // Background color updated to surfaceContainerLowest for a minimalist selection effect
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(
        alpha = selectionProgress
    )

    val contentColor = when {
        isSelected -> selectionColor
        isToday -> todayColor
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .size(CalendarSelectorDefaults.NumberCircleSize)
            .graphicsLayer {
                // Natural scaling effect
                val scale = 0.94f + (0.06f * selectionProgress)
                scaleX = scale
                scaleY = scale
            }
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 16.sp
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
                            alpha = 1f - (selectionProgress * 0.5f)
                        }
                        .background(todayColor, CircleShape)
                )
            }
        }
    }
}

private object CalendarSelectorDefaults {
    val ItemCornerRadius = 12.dp
    val NumberCircleSize = 42.dp
    val SelectionLift = 3.dp
    val HeaderSpacing = 10.dp
    val VerticalPadding = 14.dp
}
