package com.example.pattern.ui.screens.addHabitScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmojiPickerView(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val allEmojis = listOf(
        "🔥","🏃","📚","💧","🌿","😴","🧘","🍎","💪","📝",
        "🎨","🎧","🚴","🏊","🏋️‍♂️","🥗","🛌","🎯","📖","🧩",
        "💡","🎹","🎤","🖌️","🎬","🎲","🕹️","🌞","🌙"
    )
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) allEmojis
        else allEmojis.filter { it.contains(query) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EmojiSearchBar(
            query = query,
            onQueryChange = { query = it }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(filtered) { icon ->
                val isSelected = icon == selectedEmoji
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                        .clickable { onEmojiSelected(icon) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 26.sp
                    )
                }
            }
        }
    }
}
