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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.R

/**
 * Senior-level Streak Branding Tokens.
 * These centralize the design language for the special Streak experience.
 */
private object StreakTokens {
    val MilestoneGold = Color(0xFFFFD600)
    val InactiveGray = Color(0xFFDDE1E6)
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF9E9E9E)
    
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
    val isMilestoneActive = remember(currentStreak) { currentStreak >= 30 }
    val primaryColor = if (isMilestoneActive) StreakTokens.MilestoneGold else accentColor

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(top = 56.dp, bottom = 48.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StreakIconCenterpiece(
                streakCount = currentStreak,
                accentColor = primaryColor
            )

            Spacer(Modifier.height(16.dp))

            StreakCounter(
                currentStreak = currentStreak,
                textColor = StreakTokens.TextPrimary
            )

            StreakMotivationBadge(
                currentStreak = currentStreak,
                accentColor = primaryColor
            )

            Spacer(Modifier.height(56.dp))

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
    val glowAlpha by animateFloatAsState(
        targetValue = if (streakCount > 0) 1f else 0f,
        animationSpec = StreakTokens.DefaultFade,
        label = "GlowAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(160.dp)
            .drawWithCache {
                val brush = Brush.radialGradient(
                    0.0f to accentColor.copy(alpha = 0.15f * glowAlpha),
                    0.6f to accentColor.copy(alpha = 0.04f * glowAlpha),
                    1.0f to Color.Transparent,
                )
                onDrawBehind {
                    drawCircle(
                        brush = brush,
                        radius = size.maxDimension / 1.2f
                    )
                }
            }
    ) {
        Icon(
            imageVector = if (isElite) Icons.Rounded.EmojiEvents else Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    // Optional: Add scale or rotation effects for high-level streaks
                },
            tint = accentColor
        )
    }
}

@Composable
private fun StreakCounter(
    currentStreak: Int,
    textColor: Color
) {
    val streakText = stringResource(R.string.detail_day_streak).lowercase()
    
    AnimatedContent(
        targetState = currentStreak,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
        label = "StreakCounter",
        modifier = Modifier.semantics {
            contentDescription = "$currentStreak $streakText"
        }
    ) { targetStreak ->
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                    append(targetStreak.toString())
                }
                append(" ")
                append(streakText)
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                letterSpacing = (-0.8).sp,
                fontSize = 34.sp
            )
        )
    }
}

@Composable
private fun StreakMotivationBadge(
    currentStreak: Int,
    accentColor: Color
) {
    val motivationResId = remember(currentStreak) { getStreakMotivationResId(currentStreak) }
    val isSignificant = remember(currentStreak) { isMilestoneDay(currentStreak) || currentStreak >= 7 }

    AnimatedContent(
        targetState = motivationResId,
        transitionSpec = {
            fadeIn(animationSpec = StreakTokens.DefaultFade) togetherWith
                    fadeOut(animationSpec = StreakTokens.DefaultFade)
        },
        label = "MotivationText"
    ) { targetResId ->
        Text(
            text = stringResource(targetResId),
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSignificant) accentColor else StreakTokens.TextSecondary.copy(alpha = 0.6f),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
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
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, state ->
            TimelineNode(
                state = state,
                accentColor = accentColor
            )

            if (index < days.size - 1) {
                TimelineConnector(
                    isActive = state.isAchieved,
                    activeColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun TimelineConnector(
    isActive: Boolean,
    activeColor: Color
) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(4.dp)
            .background(
                color = if (isActive) activeColor else StreakTokens.InactiveGray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(2.dp)
            )
    )
}

@Composable
private fun TimelineNode(
    state: TimelineDayState,
    accentColor: Color
) {
    val nodeColor = if (state.isAchieved) accentColor else StreakTokens.InactiveGray.copy(alpha = 0.5f)
    val textColor = if (state.isAchieved) accentColor else StreakTokens.TextSecondary.copy(alpha = 0.4f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = nodeColor,
                shadowElevation = if (state.isAchieved) 8.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state.isMilestone) Icons.Rounded.EmojiEvents else Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
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
                fontSize = 12.sp
            )
        )
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
        StreakCard(currentStreak = 30, accentColor = Color(0xFFFF5722))
    }
}
