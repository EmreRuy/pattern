package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeCalendarSelector(
    listState: LazyListState,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    dayList: List<Pair<String, String>>
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = listState
    ) {
        itemsIndexed(dayList) { index, (dayLetter, dayNumber) ->
            Box(
                modifier = Modifier
                    .fillParentMaxWidth(1f / 7f)
                    .height(96.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDaySelected(index) },
                contentAlignment = Alignment.Center
            ) {
                CalendarItem(
                    isSelected = selectedDay == index,
                    dayLetter = dayLetter,
                    dayNumber = dayNumber
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
    val backgroundColor by animateColorAsState(
        if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else
            Color.Transparent,
        label = "calendar_bg"
    )

    Box(
        modifier = Modifier
            .width(46.dp)
            .heightIn(min = 72.dp)
            .background(backgroundColor, RoundedCornerShape(26.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayLetter,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        if (isSelected) Color.White
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.Black
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}



