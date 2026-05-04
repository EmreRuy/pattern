package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.pattern.domain.model.HabitType
import com.example.pattern.ui.model.HabitCardModel
import java.time.LocalDate

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
    onTimerFinished: (HabitCardModel, LocalDate) -> Unit,
    onUnfinishTimer: (Int, LocalDate) -> Unit,
    onHabitCardClick: (Int) -> Unit,
    onStartTimer: (HabitCardModel, LocalDate) -> Unit,
    onPauseTimer: (HabitCardModel, LocalDate) -> Unit,
    onResumeTimer: (HabitCardModel, LocalDate) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    
    // The central pivot for days is today at the middle index
    val pivotDate = remember { LocalDate.now() }
    val pivotIndex = 25000 * 7 // Aligned with the week pager pivot

    // Haptic feedback when the user settles on a new page
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { it }
    ) { pageIndex ->
        val date = remember(pageIndex) {
            val offset = pageIndex - (pivotIndex + (today.dayOfWeek.value - 1))
            today.plusDays(offset.toLong())
        }
        val habits = habitsByDate[date] ?: emptyList()
        val isToday = date == today

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
