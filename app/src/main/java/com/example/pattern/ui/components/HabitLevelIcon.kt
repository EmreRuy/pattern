package com.example.pattern.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A set of Material icons representing the leveling system.
 * Novice: "star_border" with gray color
 * Beginner: "star_border" with accentColor
 * Learner: half filled star with accent color
 * Advanced: fully filled star with accent color
 * Master: "workspace_premium" with accent color
 * Grandmaster: diamond with accent color
 */
@Composable
fun HabitLevelIcon(
    levelTitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (levelTitle) {
        "Novice" -> Icons.Rounded.StarBorder to Color.Gray
        "Beginner", "Amateur" -> Icons.Rounded.StarBorder to accentColor
        "Learner", "Practitioner", "Consistent" -> Icons.AutoMirrored.Rounded.StarHalf to accentColor
        "Skilled", "Advanced", "Expert" -> Icons.Rounded.Star to accentColor
        "Elite", "Master" -> Icons.Rounded.WorkspacePremium to accentColor
        "Grandmaster" -> Icons.Rounded.Diamond to accentColor
        else -> Icons.Rounded.Star to accentColor
    }

    Icon(
        imageVector = icon,
        contentDescription = "Level: $levelTitle",
        tint = color,
        modifier = modifier.size(24.dp)
    )
}
