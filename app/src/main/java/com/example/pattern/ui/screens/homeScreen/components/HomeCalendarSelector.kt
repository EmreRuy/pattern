package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
 * Premium Calendar Selector for the Home Screen.
 * Optimized for performance, smooth natural motion, and refined tactile feedback.
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
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
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
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
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
                translationY = -CalendarSelectorDefaults.SelectionOffset.toPx() * selectionProgress
            }
    ) {
        DayLetterHeader(
            letter = day.dayLetter,
            isWeekend = isWeekend,
            isSelected = isSelected,
            isToday = isToday
        )

        Spacer(Modifier.height(CalendarSelectorDefaults.HeaderSpacing))

        DaySelectionCapsule(
            isSelected = isSelected,
            isToday = isToday,
            dayNumber = day.dayNumber,
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
        isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Text(
        text = letter.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isToday || isSelected) FontWeight.Black else FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        ),
        color = color
    )
}

@Composable
private fun DaySelectionCapsule(
    isSelected: Boolean,
    isToday: Boolean,
    dayNumber: String,
    selectionProgress: Float
) {
    val containerColor = MaterialTheme.colorScheme.primary.copy(
        alpha = CalendarSelectorDefaults.SelectedBgAlpha * selectionProgress
    )

    Box(
        modifier = Modifier
            .size(
                width = CalendarSelectorDefaults.CapsuleWidth,
                height = CalendarSelectorDefaults.CapsuleHeight
            )
            .clip(RoundedCornerShape(CalendarSelectorDefaults.CapsuleCornerRadius))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                modifier = Modifier
                    .size(CalendarSelectorDefaults.IconSize)
                    .graphicsLayer {
                        alpha = 0.3f + (0.7f * selectionProgress)
                        val scale = 0.8f + (0.2f * selectionProgress)
                        scaleX = scale
                        scaleY = scale
                    },
                tint = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                }
            )

            Spacer(Modifier.height(CalendarSelectorDefaults.IconToNumberSpacing))

            DayNumberCircle(
                dayNumber = dayNumber,
                isSelected = isSelected,
                isToday = isToday,
                selectionProgress = selectionProgress
            )
        }
    }
}

@Composable
private fun DayNumberCircle(
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean,
    selectionProgress: Float
) {
    val numberColor = when {
        isSelected -> MaterialTheme.colorScheme.onSurface
        isToday -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .size(CalendarSelectorDefaults.NumberCircleSize)
            .graphicsLayer {
                val scale = 0.85f + (0.15f * selectionProgress)
                scaleX = scale
                scaleY = scale
            }
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.surface
                else
                    Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = numberColor
            )
            if (isToday && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }
    }
}

private object CalendarSelectorDefaults {
    val CapsuleWidth = 46.dp
    val CapsuleHeight = 84.dp
    val CapsuleCornerRadius = 23.dp
    val ItemCornerRadius = 24.dp
    val NumberCircleSize = 34.dp
    val IconSize = 18.dp
    
    val SelectionOffset = 4.dp
    val HeaderSpacing = 4.dp
    val IconToNumberSpacing = 8.dp
    val VerticalPadding = 8.dp
    
    const val SelectedBgAlpha = 0.15f
}
