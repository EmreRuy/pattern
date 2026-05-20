package com.example.pattern.ui.screens.profileScreen.components.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileExtraCard(
    uiState: SuccessDashboardUiState,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> SuccessScoreOverview(
                        title = uiState.title,
                        percentage = uiState.successRate,
                        statusText = uiState.statusText,
                        doneCount = uiState.doneCount,
                        missedCount = uiState.missedCount,
                        xpPoints = uiState.xpPoints
                    )

                    1 -> TopHabitsList(
                        title = "TOP 3 DONE",
                        habits = uiState.topDoneHabits,
                        emptyMessage = "Keep going! No habits done yet."
                    )

                    2 -> TopHabitsList(
                        title = "TOP 3 MISSED",
                        habits = uiState.topMissedHabits,
                        emptyMessage = "Perfect! No missed habits."
                    )
                }
            }

            // Pager Indicator
            Row(
                Modifier
                    .padding(top = 4.dp)
                    .height(4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 18f else 6f,
                        animationSpec = tween(durationMillis = 300),
                        label = "width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            .size(width = width.dp, height = 4.dp)
                    )
                }
            }
        }
    }
}
