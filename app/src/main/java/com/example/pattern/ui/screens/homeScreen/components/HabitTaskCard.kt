package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.toColorInt
import com.example.pattern.R
import com.example.pattern.ui.model.HabitCardModel

@Composable
fun HabitTaskCard(
    habit: HabitCardModel,
    isToday: Boolean,
    onCardClick: (Int) -> Unit,
    onTaskCompleted: (habitId: Int, completed: Boolean) -> Unit,
    onTaskIncrement: (habitId: Int) -> Unit = {}
) {
    val taskCount = habit.taskCount ?: 1
    val isMultiStep = taskCount > 1

    BaseHabitCard(
        habit = habit,
        onCardClick = onCardClick,
        subtitle = {
            Text(
                text = if (isMultiStep) "$taskCount times goal" else stringResource(R.string.habit_type_task),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        action = {
            TaskRing(
                checked = habit.isTaskChecked,
                taskCount = taskCount,
                completedCount = habit.completedCount,
                onToggle = {
                    if (isToday) {
                        if (isMultiStep && !habit.isTaskChecked) {
                            onTaskIncrement(habit.id)
                        } else {
                            onTaskCompleted(habit.id, !habit.isTaskChecked)
                        }
                    }
                }
            )
        }
    )
}
