package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.model.HabitCardModel
import com.example.pattern.utils.CalendarDayModel
import java.time.LocalDate
import kotlin.math.absoluteValue

/**
 * Premium Daily Habit Pager.
 *
 * Senior Level Optimizations:
 * 1. Hardware-accelerated transitions via GraphicsLayer (zero-recomposition animations).
 * 2. Strict clipping via clipToBounds to prevent parallax bleed (ghosting fix).
 * 3. Haptic feedback synchronized with Pager snapshots.
 * 4. Fling and Snap behavior optimized for fluid scrolling.
 */
@Composable
fun HabitCardsPager(
    pagerState: PagerState,
    dayList: List<CalendarDayModel>,
    habitsByDate: Map<LocalDate, List<HabitCardModel>>,
    hasAnyHabits: Boolean,
    paddingValues: PaddingValues,
    onTaskCompleted: (Int, LocalDate, Boolean) -> Unit,
    onTimerFinished: (HabitCardModel, LocalDate) -> Unit,
    onUnfinishTimer: (Int, LocalDate) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCardModel, LocalDate) -> Unit,
    onPauseTimer: (HabitCardModel, LocalDate) -> Unit,
    onResumeTimer: (HabitCardModel, LocalDate) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    val flingBehavior = PagerDefaults.flingBehavior(state = pagerState)

    // Tactile feedback on page change
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
        flingBehavior = flingBehavior,
        key = { index -> dayList.getOrNull(index)?.date?.toString() ?: index.toString() }
    ) { pageIndex ->
        val date = dayList[pageIndex].date
        val habits = habitsByDate[date] ?: emptyList()
        val isToday = date == today

        // Main Page Container: Handles depth and alpha transitions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                    val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)

                    // Depth Effect: Subtle scale-down as pages move away
                    val scale = lerp(1f, 0.97f, absOffset)
                    scaleX = scale
                    scaleY = scale

                    // Fade Effect: Smooth alpha transition
                    alpha = lerp(1f, 0.4f, absOffset)
                }
                .clipToBounds() // Prevents parallax content from bleeding into adjacent pages
        ) {
            // Parallax Layer: Moves at a slower rate than the swipe for a 3D feel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                        translationX = pageOffset * size.width * 0.15f
                    }
            ) {
                HabitList(
                    habits = habits,
                    hasAnyHabits = hasAnyHabits,
                    date = date,
                    paddingValues = paddingValues,
                    isToday = isToday,
                    onTaskCompleted = onTaskCompleted,
                    onTimerFinished = onTimerFinished,
                    onUnfinishTimer = onUnfinishTimer,
                    onHabitCardClick = onHabitCardClick,
                    onStartTimer = onStartTimer,
                    onPauseTimer = onPauseTimer,
                    onResumeTimer = onResumeTimer
                )
            }
        }
    }
}

@Composable
private fun HabitList(
    habits: List<HabitCardModel>,
    hasAnyHabits: Boolean,
    date: LocalDate,
    paddingValues: PaddingValues,
    isToday: Boolean,
    onTaskCompleted: (Int, LocalDate, Boolean) -> Unit,
    onTimerFinished: (HabitCardModel, LocalDate) -> Unit,
    onUnfinishTimer: (Int, LocalDate) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCardModel, LocalDate) -> Unit,
    onPauseTimer: (HabitCardModel, LocalDate) -> Unit,
    onResumeTimer: (HabitCardModel, LocalDate) -> Unit,
) {
    if (habits.isEmpty()) {
        val message = remember(hasAnyHabits) {
            if (hasAnyHabits) "No habits scheduled for this day!"
            else "Start by adding your first habit!"
        }
        EmptyStateMessage(paddingValues, message)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(
                items = habits,
                key = { it.id },
                contentType = { it.type }
            ) { habit ->
                when (habit.type) {
                    HabitType.BUILD -> HabitBuildCard(
                        habit = habit,
                        isToday = isToday,
                        onTimerFinished = { onTimerFinished(it, date) },
                        onUnfinishTimer = { onUnfinishTimer(it, date) },
                        onCardClick = onHabitCardClick,
                        onStartTimer = { onStartTimer(it, date) },
                        onPauseTimer = { onPauseTimer(it, date) },
                        onResumeTimer = { onResumeTimer(it, date) }
                    )

                    HabitType.TASK -> HabitTaskCard(
                        habit = habit,
                        isToday = isToday,
                        onTaskCompleted = { id, completed -> onTaskCompleted(id, date, completed) },
                        onCardClick = onHabitCardClick
                    )

                    HabitType.QUIT -> HabitQuitCard(
                        habit = habit,
                        isToday = isToday,
                        onTaskCompleted = { id, completed -> onTaskCompleted(id, date, completed) },
                        onCardClick = onHabitCardClick
                    )
                }
            }
        }
    }
}
