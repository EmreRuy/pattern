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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.utils.CalendarDayModel
import com.example.pattern.utils.CalendarMathProvider
import com.example.pattern.utils.TimePeriod
import com.example.pattern.utils.TimeUtils
import com.example.pattern.utils.toCalendarDayModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
@Composable
fun HomeCalendarSelector(
    pagerState: PagerState,
    selectedDate: LocalDate,
    timePeriod: TimePeriod,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val isTodaySelected = remember(selectedDate, today) { selectedDate == today }
    val anchorText = remember(selectedDate, today) {
        TimeUtils.getRelativeDateString(selectedDate, today)
    }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }

    val currentMonthTitle by remember {
        derivedStateOf {
            val dateInWeek = CalendarMathProvider.getMondayOfWeek(pagerState.currentPage)
            dateInWeek.format(monthFormatter)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(
            title = { currentMonthTitle },
            timePeriod = timePeriod,
            isToday = isTodaySelected,
            anchorText = anchorText,
            onTodayClick = onTodayClick
        )
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false,
            verticalAlignment = Alignment.CenterVertically,
            pageSpacing = 0.dp,
            beyondViewportPageCount = 1,
            key = { it }
        ) { weekIndex ->
            val weekStartDate = remember(weekIndex) { 
                CalendarMathProvider.getMondayOfWeek(weekIndex) 
            }
            
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
                    val isToday = remember(date, today) { date == today }
                    
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
private fun CalendarHeader(
    title: () -> String,
    timePeriod: TimePeriod,
    isToday: Boolean,
    anchorText: String,
    onTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month / Year Capsule (Left)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp), // Add space between pills
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .widthIn(min = 100.dp) // Consistent min width
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
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
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // DateAnchor (Center) - Weighted Box ensures perfect screen centering
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp), // Add space between pills
            contentAlignment = Alignment.Center
        ) {
            DateAnchor(
                text = anchorText,
                isToday = isToday,
                onClick = onTodayClick
            )
        }

        // TimePeriodBadge (Right)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp), // Add space between pills
            contentAlignment = Alignment.CenterEnd
        ) {
            TimePeriodBadge(timePeriod = timePeriod)
        }
    }
}

@Composable
private fun DateAnchor(
    text: String,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .then(
                if (!isToday) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .widthIn(min = 100.dp) // Consistent min width
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = text,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
                        .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)))
                },
                label = "anchor_text_transition"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = color.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun TimePeriodBadge(timePeriod: TimePeriod) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .widthIn(min = 100.dp) // Consistent min width
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = timePeriod,
            transitionSpec = {
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)))
            },
            label = "time_period_transition"
        ) { targetPeriod ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = targetPeriod.icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = targetPeriod.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            }
        }
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
    val selectionColor = Color.Black
    val todayColor = MaterialTheme.colorScheme.secondary

    val contentColor = when {
        isSelected -> Color.White
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
                // Selection circle background implemented as solid black per design
                drawCircle(
                    color = selectionColor.copy(alpha = progress),
                )
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
        }
    }
}

private object CalendarSelectorDefaults {
    val NumberCircleSize = 40.dp
    val HeaderSpacing = 8.dp
    val VerticalPadding = 12.dp
}
