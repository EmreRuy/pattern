package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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

@Composable
fun HomeCalendarSelector(
    listState: LazyListState,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    dayList: List<CalendarDayModel>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        state = listState,
        flingBehavior = ScrollableDefaults.flingBehavior()
    ) {
        itemsIndexed(
            items = dayList,
            key = { _, day -> day.fullDateString }
        ) { index, day ->
            Box(
                modifier = Modifier
                    .fillParentMaxWidth(1f / 7f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDaySelected(index) },
                contentAlignment = Alignment.Center
            ) {
                CalendarItem(
                    isSelected = selectedDay == index,
                    dayLetter = day.dayLetter,
                    dayNumber = day.dayNumber
                )
            }
        }
    }
}

@Composable
fun CalendarItem(
    isSelected: Boolean,
    dayLetter: String,
    dayNumber: String
) {
    val selectionProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "selection_fade"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = dayLetter.uppercase(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        // Main Container
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    MossGreen.copy(alpha = 0.12f * selectionProgress)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { alpha = if (isSelected) 1f else 0.3f },
                    tint = if (isSelected) MossGreen else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                //circle container
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            //pop animation
                            scaleX = 0.8f + (0.2f * selectionProgress)
                            scaleY = 0.8f + (0.2f * selectionProgress)
                        }
                        .background(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.surfaceContainerLowest
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
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}



