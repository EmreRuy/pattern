package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.pattern.R
import com.example.pattern.data.model.HabitCardModel

@Composable
fun HabitQuitCard(
    habit: HabitCardModel,
    isToday: Boolean,
    onCardClick: (Int) -> Unit,
    onTaskCompleted: (habitId: Int, completed: Boolean) -> Unit,
) {
    BaseHabitCard(
        habit = habit,
        onCardClick = onCardClick,
        subtitle = {
            Text(
                text = stringResource(R.string.habit_type_quit),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        action = { accentColor ->
            TaskRing(
                checked = habit.isTaskChecked,
                accentColor = accentColor,
                onToggle = {
                    if (isToday) {
                        onTaskCompleted(habit.id, !habit.isTaskChecked)
                    }
                }
            )
        }
    )
}
