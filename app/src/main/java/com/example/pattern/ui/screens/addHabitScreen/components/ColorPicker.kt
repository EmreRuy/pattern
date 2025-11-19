package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt


@Composable
fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#264653", // Deep teal
        "#2A9D8F", // Jade green
        "#E9C46A", // Soft gold
        "#F4A261", // Warm amber
        "#E76F51", // Terracotta red
        "#6D597A", // Muted plum
        "#4A4E69", // Slate indigo
        "#1D3557" , // Deep navy

        "#5A7D9A", // Slate Blue-Gray
        "#6A8E7F", // Muted Teal
        "#A67F5B", // Warm Brown-Gold
        "#8C5E7A", // Dusty Purple
        "#4F6F52", // Deep Forest Green
        "#8A8F9E", // Elegant Gray-Blue
        "#B06C5B", // Terracotta
        "#7C8C4C"  // Olive Green
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 18.dp)
    ) {
        items(colors) { colorHex ->
            val color = Color(colorHex.toColorInt())
            val isSelected = colorHex == selectedColor

            val animatedSize by animateDpAsState(
                targetValue = if (isSelected) 56.dp else 44.dp,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ), label = "colorSize"
            )
            val interactionSource = remember { MutableInteractionSource() }
            // 👇 Fixed outer box to keep layout stable
            Box(
                modifier = Modifier
                    .size(56.dp) // max size reserved
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(animatedSize)
                        .clip(CircleShape)
                        .background(color)
                        .shadow(elevation = 0.dp, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(visible = isSelected) {
                       // val checkmarkColor = if (color.luminance() > 0.4f) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.scrim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
