package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmojiSelector(selectedEmoji: String, onEmojiChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddReaction,
                    contentDescription = "Icon Selection",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Choose Icon",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Emoji Grid
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FlowRow(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val emojis = listOf(
                        "🔥", "🏃", "📚", "💧", "🌿", "😴", "🧘", "🍎", "💪", "📝",
                        "🎨", "🎧", "🚴", "🏊", "🏋️‍♂️", "🥗", "🛌", "🎯", "📖", "🧩",
                        "💡", "🎹", "🎤", "🖌️", "🎬", "🎲", "🕹️", "🌞", "🌙", "🕊️"
                    )
                    emojis.forEach { icon ->
                        val interactionSource = remember { MutableInteractionSource() }
                        Surface(
                            shape = CircleShape,
                            color = if (selectedEmoji == icon)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface,
                            tonalElevation = if (selectedEmoji == icon) 4.dp else 0.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onEmojiChange(icon) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(icon, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
