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
import com.example.pattern.ui.screens.addHabitScreen.CardHeader

@Composable
fun HabitListSheetContent(
    habits: List<Habit>,
    onHabitClick: (Int) -> Unit
) {
    HabitListBottomSheet(
        habits = habits,
        onHabitClick = onHabitClick
    )
}
@Composable
fun HabitListBottomSheet(
    habits: List<Habit>,
    onHabitClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

       CardHeader("Your Habits")

        if (habits.isEmpty()) {
            EmptyHabitMessage()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = habits,
                    key = { it.id }
                ) { habit ->
                    HabitListItem(
                        habit = habit,
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
    onHabitClick: (Int) -> Unit
) {
    val accentColor = remember(habit.accentColorHex) {
        Color(habit.accentColorHex.toColorInt())
    }
    val isDark = isSystemInDarkTheme()
    val containerColor =  if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest
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
        // XP Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "12,312",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "XP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color = Color.Gray
            )
        }
    }
}
