package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StreakBadge(
    streak: Int,
    isStreakActive: Boolean = true
) {
    if (streak <= 0) return

    Icon(
        imageVector = Icons.Rounded.Whatshot,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = if (isStreakActive) Color(0xFFFF9800) else Color.Gray.copy(alpha = 0.5f)
    )
}
