package com.example.pattern.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.pattern.R
import com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen.HabitDetailsUi
import com.example.pattern.utils.ExperienceUtils

@Composable
fun HabitProgressCard(habit: HabitDetailsUi, accentColor: Color) {
    val levelInfo = remember(habit.totalXP) { ExperienceUtils.getLevelInfo(habit.totalXP) }
    val animatedXP by animateIntAsState(
        targetValue = habit.totalXP,
        animationSpec = tween(durationMillis = 1500),
        label = "xpAnimation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.detail_mastery_progress),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        HabitLevelIcon(
                            levelTitle = levelInfo.title,
                            accentColor = accentColor
                        )
                        Text(
                            text = levelInfo.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HabitLevelIcon(
                            levelTitle = levelInfo.title,
                            accentColor = accentColor
                        )
                    }
                }

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { levelInfo.progress },
                        modifier = Modifier.size(48.dp),
                        color = accentColor,
                        strokeWidth = 4.dp,
                        trackColor = accentColor.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${(levelInfo.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        ),
                        color = accentColor
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = animatedXP.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " " + stringResource(R.string.detail_total_xp),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { levelInfo.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.detail_level_only, levelInfo.level),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(
                        R.string.detail_xp_for_level,
                        levelInfo.nextLevelXP,
                        levelInfo.level + 1
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
