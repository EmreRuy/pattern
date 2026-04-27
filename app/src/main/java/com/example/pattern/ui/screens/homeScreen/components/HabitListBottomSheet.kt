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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.ui.screens.addHabitScreen.components.CardHeader
import com.example.pattern.ui.screens.settings.MossGreen

@Composable
fun HabitListSheetContent(
    habits: List<Habit>,
    dailyStates: List<HabitDailyState>,
    onHabitClick: (Int) -> Unit
) {
    HabitListBottomSheet(
        habits = habits,
        dailyStates = dailyStates,
        onHabitClick = onHabitClick
    )
}

@Composable
fun HabitListBottomSheet(
    habits: List<Habit>,
    dailyStates: List<HabitDailyState>,
    onHabitClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardHeader("Your Habits")
        Spacer(modifier = Modifier.height(16.dp))
        if (habits.isEmpty()) {
            EmptyHabitMessage()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.7f), // Limit height for better sheet behavior
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = habits,
                    key = { it.id }
                ) { habit ->
                    val dailyState = dailyStates.find { it.habitId == habit.id }
                    HabitListItem(
                        habit = habit,
                        dailyState = dailyState,
                        onHabitClick = onHabitClick
                    )
                }
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
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(containerColor)
            .clickable { onHabitClick(habit.id) }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Accent Indicator
        Box(
            modifier = Modifier
                .size(width = 6.dp, height = 42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.6f))
                    )
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (habit.type) {
                    HabitType.BUILD -> "${habit.durationInMinutes ?: 0} min session"
                    HabitType.TASK -> "Daily task"
                    HabitType.QUIT -> "Drop habit"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Habit XP Score
        val xpScore = remember(habit, dailyState) {
            ExperienceUtils.calculateHabitXP(habit, dailyState ?: HabitDailyState(habitId = habit.id, date = ""))
        }
        
        // Let's also show the max XP if it's 0, so they know what it's worth.
        val maxXP = remember(habit) {
             val dummyState = when (habit.type) {
                HabitType.BUILD -> HabitDailyState(habitId = habit.id, date = "", isCompleted = true)
                HabitType.TASK, HabitType.QUIT -> HabitDailyState(habitId = habit.id, date = "", isTaskCompleted = true)
            }
            ExperienceUtils.calculateHabitXP(habit, dummyState)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (xpScore > 0) "$xpScore" else "$maxXP",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (xpScore > 0) MossGreen else Color.Gray.copy(alpha = 0.5f)
                )
            )
            Text(
                text = "XP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
