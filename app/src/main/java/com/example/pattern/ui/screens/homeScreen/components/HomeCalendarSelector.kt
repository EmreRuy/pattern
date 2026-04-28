package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.ui.screens.settings.MossGreen
import com.example.pattern.utils.CalendarDayModel
import java.time.DayOfWeek

@Composable
fun HomeCalendarSelector(
    pagerState: PagerState,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    dayList: List<CalendarDayModel>
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top,
        pageSpacing = 8.dp
    ) { weekIndex ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val startDayIndex = weekIndex * 7
            for (i in 0 until 7) {
                val dayIndex = startDayIndex + i
                if (dayIndex >= dayList.size) break
                
                val day = dayList[dayIndex]
                val isWeekend = day.date.dayOfWeek == DayOfWeek.SATURDAY || 
                               day.date.dayOfWeek == DayOfWeek.SUNDAY

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDaySelected(dayIndex) },
                    contentAlignment = Alignment.Center
                ) {
                    CalendarItem(
                        isSelected = selectedDayIndex == dayIndex,
                        dayLetter = day.dayLetter,
                        dayNumber = day.dayNumber,
                        isWeekend = isWeekend
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarItem(
    isSelected: Boolean,
    dayLetter: String,
    dayNumber: String,
    isWeekend: Boolean
) {
    val selectionProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selection_fade"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.graphicsLayer {
            translationY = -4.dp.toPx() * selectionProgress
        }
    ) {
        Text(
            text = dayLetter.uppercase(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            ),
            color = if (isWeekend) 
                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        // Main Container
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(
                    MossGreen.copy(alpha = 0.15f * selectionProgress)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { 
                            alpha = 0.3f + (0.7f * selectionProgress)
                            scaleX = 0.8f + (0.2f * selectionProgress)
                            scaleY = 0.8f + (0.2f * selectionProgress)
                        },
                    tint = if (isSelected) MossGreen else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                //circle container
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            scaleX = 0.85f + (0.15f * selectionProgress)
                            scaleY = 0.85f + (0.15f * selectionProgress)
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
                    Text(
                        text = dayNumber,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        ),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
