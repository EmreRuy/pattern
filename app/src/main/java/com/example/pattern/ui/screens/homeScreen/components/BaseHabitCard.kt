package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.example.pattern.ui.components.PatternIcon
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.ui.theme.AppTheme

@Composable
fun BaseHabitCard(
    habit: HabitCardModel,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: @Composable () -> Unit,
    action: @Composable (Color) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surface = MaterialTheme.colorScheme.surface
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val onSurface = MaterialTheme.colorScheme.onSurface

    val accentColor = remember(habit.accentColorHex, isDark, surface, fallbackColor) {
        runCatching { Color(habit.accentColorHex.toColorInt()) }
            .getOrDefault(fallbackColor)
            .let {
                if (isDark) Color(
                    ColorUtils.blendARGB(
                        it.toArgb(),
                        surface.toArgb(),
                        0.30f
                    )
                ) else it
            }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCardClick(habit.id) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.extendedColors.habitCardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Section
            Box(
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = accentColor.copy(alpha = if (isDark) 0.20f else 0.12f),
                            shape = CircleShape
                        )
                ) {
                    PatternIcon(
                        iconCode = habit.iconEmoji.orEmpty(),
                        size = 24.sp,
                        color = accentColor
                    )
                }

                if (habit.currentStreak > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        StreakBadge(
                            streak = habit.currentStreak,
                            isStreakActive = habit.isCompleted || habit.isTaskChecked
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Text Section
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = habit.name.trim().replaceFirstChar { it.uppercase() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = onSurface.copy(alpha = 0.85f),
                       // fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                        lineHeight = 24.sp
                    )
                )
                subtitle()
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Action Section
            action(accentColor)
        }
    }
}
