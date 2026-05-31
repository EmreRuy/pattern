package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.pattern.ui.components.SectionHeader

@Composable
fun ColorPickerView(
    selectedColor: String,
    isPremium: Boolean = false,
    onColorSelected: (String) -> Unit
) {
    val freeColors = listOf(
        "#264653", "#2A9D8F", "#E9C46A", "#F4A261",
        "#E76F51", "#6D597A", "#4A4E69", "#1D3557"
    )
    val premiumColors = listOf(
        "#5A7D9A", "#6A8E7F", "#A67F5B", "#8C5E7A",
        "#4F6F52", "#8A8F9E", "#B06C5B", "#7C8C4C", "#6366F1"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("FREE COLORS")
        ColorGrid(
            colors = freeColors,
            selectedColor = selectedColor,
            isLocked = false,
            onColorSelected = onColorSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("PREMIUM COLORS")
        ColorGrid(
            colors = premiumColors,
            selectedColor = selectedColor,
            isLocked = !isPremium,
            onColorSelected = if (isPremium) onColorSelected else { _ -> /* Do nothing or show subtle feedback */ }
        )
    }
}

@Composable
private fun ColorGrid(
    colors: List<String>,
    selectedColor: String,
    isLocked: Boolean,
    onColorSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(colors) { colorHex ->
            val color = Color(colorHex.toColorInt())
            val isSelected = selectedColor == colorHex
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(color)
                    .testTag("color_$colorHex")
                    .clickable {
                        onColorSelected(colorHex)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
