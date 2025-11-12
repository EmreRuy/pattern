package com.example.pattern.ui.screens.homeScreen.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.data.model.HabitCard
import androidx.core.graphics.toColorInt
import com.example.pattern.ui.screens.addHabitScreen.components.blendColors

@Composable
fun HabitBuildCard(
    habit: HabitCard,
    onHabitTimeChecked: () -> Unit,
    onCardClick: (Int) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surface = MaterialTheme.colorScheme.surface
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainer
    val accentColor = remember(habit.accentColorHex, isDark) {
        val base = try {
            Color(habit.accentColorHex.toColorInt())
        } catch (_: Exception) {
            fallbackColor
        }
        if (isDark) {
            // soften and blend with surface for dark theme
            blendColors(base, surface, 0.4f)
        } else {
            base
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCardClick(habit.id) }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(22.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = habit.iconEmoji ?: "",
                    fontSize = 24.sp,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (habit.isTimeChecked.value) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow,
                contentDescription = null,
                tint = if (habit.isTimeChecked.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(36.dp)
                    .clickable {
                        habit.isTimeChecked.value = !habit.isTimeChecked.value
                        if (habit.isTimeChecked.value) {
                            onHabitTimeChecked()
                        }
                    }
            )
        }
    }
}