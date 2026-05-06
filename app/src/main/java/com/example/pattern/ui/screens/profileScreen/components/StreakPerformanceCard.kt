package com.example.pattern.ui.screens.profileScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.domain.model.StreakStat
import androidx.core.graphics.toColorInt

/**
 * Top-tier Product Design Implementation.
 * DNA: Minimalist, Bold Hierarchy, Behavioral Focus.
 * Inspired by high-end performance dashboards (Apple Health, Strava).
 */
@Composable
fun StreakPerformanceCard(
    modifier: Modifier = Modifier,
    bestStreaks: List<StreakStat>,
    insight: InsightData? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Section Header: Refined Typography
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STREAK PERFORMANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF5722).copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))

            if (bestStreaks.isEmpty()) {
                EmptyStreakPlaceholder()
            } else {
                // Main Streak List
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    bestStreaks.forEach { streak ->
                        StreakItem(streak = streak)
                    }
                }
            }
            
            // Integrated Insight: The "Reflection" Block
            insight?.let {
                Spacer(modifier = Modifier.height(32.dp))
                StreakReflectionBlock(it)
            }
        }
    }
}

@Composable
private fun StreakItem(streak: StreakStat) {
    val color = remember(streak.colorHex) {
        try { Color(streak.colorHex.toColorInt()) } catch (_: Exception) { Color(0xFF588157) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High-Contrast Icon Container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = streak.iconCode,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Center Info: Bold Labeling
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = streak.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "PERSONAL BEST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    fontSize = 9.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        // Numeric Hero: Iconic representation
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = streak.streakCount.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                ),
                color = color
            )
            Text(
                text = "DAYS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun StreakReflectionBlock(insight: InsightData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = insight.emoji, fontSize = 18.sp)
            }
            Text(
                text = insight.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = insight.message,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.1.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Behavioral Anchor Callout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(3.dp, 28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )
            Text(
                text = insight.action,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyStreakPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Consistency creates the rhythm of life.\nStart your first streak today.",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
