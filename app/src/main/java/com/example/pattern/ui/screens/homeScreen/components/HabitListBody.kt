package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.domain.model.HabitDailyState

@Composable
fun HabitListBody(
    habits: List<Habit>,
    dailyStates: Map<Int, HabitDailyState>,
    onHabitClick: (Int) -> Unit
) {
    if (habits.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            EmptyHabitMessage()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = habits,
                key = { it.id }
            ) { habit ->
                val dailyState = dailyStates[habit.id]
                HabitListItem(
                    habit = habit,
                    dailyState = dailyState,
                    onHabitClick = onHabitClick
                )
            }
        }
    }
}

@Composable
fun HabitListItem(
    habit: Habit,
    dailyState: HabitDailyState?,
    onHabitClick: (Int) -> Unit
) {
    val accentColor = remember(habit.accentColorHex) {
        Color(habit.accentColorHex.toColorInt())
    }
    
    val containerColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .clickable { onHabitClick(habit.id) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Accent Indicator
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 36.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.5f))
                    )
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name.trim(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when (habit.type) {
                    HabitType.BUILD -> "${habit.durationInMinutes ?: 0} min session"
                    HabitType.TASK -> "Daily task"
                    HabitType.QUIT -> "Drop habit"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        val xpScore = remember(habit, dailyState) {
            ExperienceUtils.calculateHabitXP(habit, dailyState ?: HabitDailyState(habitId = habit.id, date = ""))
        }
        
        val maxXP = remember(habit) {
             val dummyState = when (habit.type) {
                HabitType.BUILD -> HabitDailyState(habitId = habit.id, date = "", isCompleted = true)
                HabitType.TASK, HabitType.QUIT -> HabitDailyState(habitId = habit.id, date = "", isTaskCompleted = true)
            }
            ExperienceUtils.calculateHabitXP(habit, dummyState)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (xpScore > 0) "+$xpScore" else "$maxXP",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (xpScore > 0) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
            Text(
                text = "XP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
