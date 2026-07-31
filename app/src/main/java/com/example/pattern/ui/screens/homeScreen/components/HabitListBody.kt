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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.pattern.domain.usecase.HabitStatusModel
import com.example.pattern.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun HabitListBody(
    habits: ImmutableList<HabitStatusModel>,
    isLoading: Boolean,
    onHabitClick: (Int) -> Unit
) {
    if (habits.isEmpty() && !isLoading) {
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
                key = { "${it.habit.id}_${it.habit.type}_${it.habit.durationInMinutes ?: 0}" },
                contentType = { "habit_item" }
            ) { model ->
                HabitListItem(
                    model = model,
                    onHabitClick = onHabitClick,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun HabitListItem(
    model: HabitStatusModel,
    onHabitClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = model.habit
    val accentColor = remember(habit.accentColorHex) {
        if (habit.accentColorHex.isBlank()) {
            Color(0xFF6366F1)
        } else {
            try {
                Color(habit.accentColorHex.toColorInt())
            } catch (_: Exception) {
                Color(0xFF6366F1)
            }
        }
    }
    
    val containerColor = AppTheme.extendedColors.habitCardBackground
    
    val verticalIndicatorBrush = remember(accentColor) {
        Brush.verticalGradient(
            colors = listOf(accentColor, accentColor.copy(alpha = 0.5f))
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .clickable { onHabitClick(habit.id) }
            .drawBehind {
                // High-performance vertical indicator using Canvas directly with cached brush
                drawRoundRect(
                    brush = verticalIndicatorBrush,
                    topLeft = Offset(16.dp.toPx(), center.y - 18.dp.toPx()),
                    size = Size(4.dp.toPx(), 36.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
            .padding(start = 36.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (model.isCompleted) "+${model.currentXP}" else "${model.maxXP}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (model.isCompleted) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
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
