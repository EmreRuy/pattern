package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

@Composable
fun HabitSelectionCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    leadingContent: @Composable () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "pressScale")
    
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        shadowElevation = 0.dp,
        border = null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            leadingContent()
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun HabitNameCard(
    habitName: String,
    onOpen: () -> Unit
) {
    val initial = habitName.trim().take(1).uppercase().ifBlank { "?" }
    HabitSelectionCard(
        label = "Habit Name",
        value = habitName.ifBlank { "What's the goal?" },
        onClick = onOpen,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    )
}

@Composable
fun ColorSelector(
    selectedColor: String,
    onOpen: () -> Unit
) {
    val color = remember(selectedColor) {
        try { Color(selectedColor.toColorInt()) } catch (_: Exception) { Color(0xFF77DD77) }
    }
    HabitSelectionCard(
        label = "Color",
        value = "Set the vibe",
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    )
}

@Composable
fun EmojiSelector(
    selectedEmoji: String,
    onOpen: () -> Unit
) {
    HabitSelectionCard(
        label = "Icon",
        value = "Add a touch",
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = selectedEmoji, fontSize = 24.sp)
            }
        }
    )
}

@Composable
fun HabitTypeSelectorCard(
    selectedType: String,
    onOpen: () -> Unit
) {
    val (icon, iconColor) = when (selectedType) {
        "Grow" -> Icons.Default.AutoGraph to Color(0xFF22C55E)
        "Drop" -> Icons.Default.RemoveCircleOutline to Color(0xFFFB7185)
        else -> Icons.Default.ChangeCircle to Color(0xFF6366F1)
    }
    
    HabitSelectionCard(
        label = "Category",
        value = selectedType,
        onClick = onOpen,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

@Composable
fun MotivationCard(
    motivation: String,
    onOpen: () -> Unit
) {
    HabitSelectionCard(
        label = "Motivation",
        value = motivation.ifBlank { "Why this habit?" },
        onClick = onOpen,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎯",
                    fontSize = 20.sp
                )
            }
        }
    )
}
