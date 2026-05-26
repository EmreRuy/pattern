package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.pattern.domain.model.HabitType
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.utils.CalendarMathProvider
import java.time.LocalDate

@Immutable
data class HabitCardList(val items: List<HabitCardModel>)

/**
 * A pager that displays habit cards for different days.
 * Optimized for clean transitions and minimal recomposition.
 */
@Composable
fun HabitCardsPager(
    pagerState: PagerState,
    habitsByDate: Map<LocalDate, List<HabitCardModel>>,
    hasAnyHabits: Boolean,
    paddingValues: PaddingValues,
    onTaskCompleted: (Int, LocalDate, Boolean) -> Unit,
    onTaskIncrement: (Int, LocalDate) -> Unit,
    onTimerFinished: (HabitCardModel, LocalDate) -> Unit,
    onUnfinishTimer: (Int, LocalDate) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCardModel, LocalDate) -> Unit,
    onPauseTimer: (HabitCardModel, LocalDate) -> Unit,
    onResumeTimer: (HabitCardModel, LocalDate) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    
    // Haptic feedback when the user settles on a new page
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { it },
        beyondViewportPageCount = 1
    ) { pageIndex ->
        val date = remember(pageIndex, today) {
            CalendarMathProvider.getDateFromDayIndex(today, pageIndex)
        }
        val habits = habitsByDate[date] ?: emptyList()
        val isToday = date == today

        HabitListContent(
            habits = HabitCardList(habits),
            hasAnyHabits = hasAnyHabits,
            date = date,
            paddingValues = paddingValues,
            isToday = isToday,
            onTaskCompleted = onTaskCompleted,
            onTaskIncrement = onTaskIncrement,
            onTimerFinished = onTimerFinished,
            onUnfinishTimer = onUnfinishTimer,
            onHabitCardClick = onHabitCardClick,
            onStartTimer = onStartTimer,
            onPauseTimer = onPauseTimer,
            onResumeTimer = onResumeTimer
        )
    }
}

@Composable
private fun HabitListContent(
    habits: HabitCardList,
    hasAnyHabits: Boolean,
    date: LocalDate,
    paddingValues: PaddingValues,
    isToday: Boolean,
    onTaskCompleted: (Int, LocalDate, Boolean) -> Unit,
    onTaskIncrement: (Int, LocalDate) -> Unit,
    onTimerFinished: (HabitCardModel, LocalDate) -> Unit,
    onUnfinishTimer: (Int, LocalDate) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCardModel, LocalDate) -> Unit,
    onPauseTimer: (HabitCardModel, LocalDate) -> Unit,
    onResumeTimer: (HabitCardModel, LocalDate) -> Unit,
) {
    if (habits.items.isEmpty()) {
        val message = remember(hasAnyHabits) {
            if (hasAnyHabits) "No habits scheduled for this day!"
            else "Start by adding your first habit!"
        }
        EmptyStateMessage(paddingValues, message)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = habits.items,
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
                        onTaskIncrement = { id -> onTaskIncrement(id, date) },
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
