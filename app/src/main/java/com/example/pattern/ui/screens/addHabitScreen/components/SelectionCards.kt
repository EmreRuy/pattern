package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

@Composable
fun HabitSelectionCard(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Optimization: Use graphicsLayer for animations to avoid recomposition
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "pressScale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            content()
        }
    }
}

@Composable
fun ColorSelector(
    selectedColor: String,
    onOpen: () -> Unit
) {
    val color = remember(selectedColor) {
        try { Color(selectedColor.toColorInt()) } catch (_: Exception) { Color(0xFF6366F1)
        }
    }
    HabitSelectionCard(
        label = "Color",
        onClick = onOpen
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun EmojiSelector(
    selectedEmoji: String,
    onOpen: () -> Unit
) {
    HabitSelectionCard(
        label = "Icon",
        onClick = onOpen
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = selectedEmoji, fontSize = 20.sp)
        }
    }
}

@Composable
fun MotivationCard(
    onOpen: () -> Unit
) {
    HabitSelectionCard(
        label = "Motivation",
        onClick = onOpen
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎯",
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun HabitNameCard(
    name: String,
    onNameChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (name.isEmpty()) {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun HabitTypeSelectorCard(
    selectedType: String,
    onOpen: () -> Unit
) {
    // Optimization: Memoize the configuration to avoid re-calculation during recomposition
    val config = remember(selectedType) {
        when (selectedType) {
            "Grow" -> Icons.Default.AutoGraph to Color(0xFF22C55E)
            "Drop" -> Icons.Default.RemoveCircleOutline to Color(0xFFFB7185)
            else -> Icons.Default.ChangeCircle to Color(0xFF6366F1)
        }
    }
    val (categoryIcon, categoryIconColor) = config

    HabitSelectionCard(
        label = "Category",
        onClick = onOpen
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(categoryIconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = null,
                tint = categoryIconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
