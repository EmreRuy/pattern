package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.pattern.data.model.HabitCardModel
import com.example.pattern.ui.screens.addHabitScreen.components.blendColors

@Composable
fun HabitTaskCard(
    habit: HabitCardModel,
    onCardClick: (Int) -> Unit,
    onTaskCompleted: (habitId: Int, completed: Boolean) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val surface = MaterialTheme.colorScheme.surface
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val accentColor = remember(habit.accentColorHex, isDark) {
        runCatching { Color(habit.accentColorHex.toColorInt()) }
            .getOrDefault(fallbackColor)
            .let { if (isDark) blendColors(it, surface, 0.35f) else it }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCardClick(habit.id) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.iconEmoji.orEmpty(),
                    fontSize = 30.sp,
                    modifier = Modifier.padding(end = 14.dp)
                )
                Column {
                    Text(
                        text = habit.name.replaceFirstChar { it.uppercase() },
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 19.sp,
                            letterSpacing = (-0.3).sp
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Task",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
            TaskRing(
                checked = habit.isTaskChecked.value,
                accentColor = accentColor,
                onToggle = {
                    val newValue = !habit.isTaskChecked.value
                    habit.isTaskChecked.value = newValue
                    onTaskCompleted(habit.id, newValue)
                }
            )
        }
    }
}