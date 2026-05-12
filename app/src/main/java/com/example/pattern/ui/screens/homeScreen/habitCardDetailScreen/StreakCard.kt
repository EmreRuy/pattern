package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.R

/**
 * Senior-level Streak Branding Tokens.
 * These centralize the design language for the special Streak experience.
 */
private object StreakTokens {
    @Composable
    fun textPrimary() = MaterialTheme.colorScheme.onSurface

    @Composable
    fun textSecondary() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    @Composable
    fun inactiveColor() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    // Animation Specs
    val DefaultFade = tween<Float>(durationMillis = 400)
}

/**
 * A production-ready, highly optimized Streak Card.
 * Designed with a focus on zero-recomposition overhead, accessibility, and premium visual polish.
 */
@Composable
fun StreakCard(
    currentStreak: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 48.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StreakIconCenterpiece(
                streakCount = currentStreak,
                accentColor = accentColor
            )

            Spacer(Modifier.height(16.dp))

            StreakCounter(
                currentStreak = currentStreak,
                textColor = StreakTokens.textPrimary()
            )

            Box(
                modifier = Modifier.heightIn(min = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                StreakMotivationBadge(
                    currentStreak = currentStreak
                )
            }

            Spacer(Modifier.height(48.dp))

            StreakTimeline(
                currentStreak = currentStreak,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun StreakIconCenterpiece(
    streakCount: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isElite = streakCount >= 30
    // Removed animation to prevent "blinking" on entry
    val glowAlpha = if (streakCount > 0) 1f else 0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(140.dp)
            .drawWithCache {
                val brush = Brush.radialGradient(
                    0.0f to accentColor.copy(alpha = 0.2f * glowAlpha),
                    0.5f to accentColor.copy(alpha = 0.05f * glowAlpha),
                    1.0f to Color.Transparent,
                )
                onDrawBehind {
                    drawCircle(
                        brush = brush,
                        radius = size.maxDimension / 1.1f
                    )
                }
            }
    ) {
        Icon(
            imageVector = if (isElite) Icons.Rounded.EmojiEvents else Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp),
            tint = accentColor
        )
    }
}

@Composable
private fun StreakCounter(
    currentStreak: Int,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val streakText = stringResource(R.string.detail_day_streak).lowercase()

    // Staff-level fix: Using a single Text node with a unified font family (Poppins)
    // to prevent the "jump" caused by metric mismatches between Lato and Poppins.
    // LineHeight is locked to reserve vertical space before font swap.
    val annotatedString = remember(currentStreak, streakText) {
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                append(currentStreak.toString())
            }
            append(" ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                append(streakText)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = textColor,
                letterSpacing = (-0.8).sp,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.semantics {
                contentDescription = "$currentStreak $streakText"
            },
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun StreakMotivationBadge(
    currentStreak: Int
) {
    val motivationResId = getStreakMotivationResId(currentStreak)

    Text(
        text = stringResource(motivationResId),
        style = MaterialTheme.typography.labelMedium.copy(
            color = StreakTokens.textSecondary(),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
        ),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Immutable
private data class TimelineDayState(
    val day: Int,
    val isAchieved: Boolean,
    val isTarget: Boolean,
    val isMilestone: Boolean
)

@Composable
private fun StreakTimeline(
    currentStreak: Int,
    accentColor: Color
) {
    val days = remember(currentStreak) {
        val startDay = (currentStreak - 1).coerceAtLeast(1)
        List(4) { index ->
            val day = startDay + index
            TimelineDayState(
                day = day,
                isAchieved = day <= currentStreak,
                isTarget = day == currentStreak + 1,
                isMilestone = isMilestoneDay(day)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEachIndexed { index, state ->
            TimelineNode(
                state = state,
                accentColor = accentColor
            )

            if (index < days.size - 1) {
                TimelineConnector(
                    isActive = state.isAchieved && days[index + 1].isAchieved,
                    activeColor = accentColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineConnector(
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) activeColor else StreakTokens.inactiveColor()
    Box(
        modifier = modifier
            .height(2.dp)
            .background(color = color, shape = CircleShape)
    )
}

@Composable
private fun TimelineNode(
    state: TimelineDayState,
    accentColor: Color
) {
    val nodeColor = if (state.isAchieved) accentColor else StreakTokens.inactiveColor()
    val textColor = if (state.isAchieved) accentColor else StreakTokens.textSecondary().copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
            Surface(
                modifier = Modifier.size(if (state.isAchieved) 36.dp else 32.dp),
                shape = CircleShape,
                color = nodeColor,
                shadowElevation = if (state.isAchieved) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getTimelineIcon(state),
                        contentDescription = null,
                        modifier = Modifier.size(if (state.isMilestone) 20.dp else 18.dp),
                        tint = if (state.isAchieved) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "D${state.day}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}

private fun getTimelineIcon(state: TimelineDayState): ImageVector {
    return when {
        state.isMilestone -> Icons.Rounded.EmojiEvents
        else -> Icons.Rounded.LocalFireDepartment
    }
}

private fun isMilestoneDay(day: Int): Boolean {
    val keyMilestones = setOf(2, 7, 14, 30, 50, 100, 365)
    return day in keyMilestones || (day > 0 && day % 50 == 0)
}

@StringRes
private fun getStreakMotivationResId(streak: Int): Int {
    return when {
        streak <= 0 -> R.string.streak_motivation_0
        streak == 1 -> R.string.streak_motivation_1
        streak in 2..3 -> R.string.streak_motivation_2_3
        streak in 4..6 -> R.string.streak_motivation_4_6
        streak in 7..13 -> R.string.streak_motivation_7_13
        streak in 14..20 -> R.string.streak_motivation_14_20
        streak in 21..29 -> R.string.streak_motivation_21_29
        streak in 30..49 -> R.string.streak_motivation_30_49
        streak in 50..99 -> R.string.streak_motivation_50_99
        else -> R.string.streak_motivation_100_plus
    }
}

@Preview(showBackground = true, name = "New Journey")
@Composable
private fun StreakCardNewPreview() {
    MaterialTheme {
        StreakCard(currentStreak = 1, accentColor = Color(0xFF4CAF50))
    }
}

@Preview(showBackground = true, name = "Active Streak")
@Composable
private fun StreakCardActivePreview() {
    MaterialTheme {
        StreakCard(currentStreak = 7, accentColor = Color(0xFFFF9800))
    }
}

@Preview(showBackground = true, name = "Elite Milestone")
@Composable
private fun StreakCardElitePreview() {
    MaterialTheme {
        StreakCard(currentStreak = 365, accentColor = Color(0xFFFF5722))
    }
}
