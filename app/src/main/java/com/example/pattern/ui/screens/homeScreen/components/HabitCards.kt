package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.example.pattern.domain.model.HabitType
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.utils.CalendarMathProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

/**
 * A pager that displays habit cards for different days.
 * Optimized for clean transitions and minimal recomposition.
 */
@Composable
fun HabitCardsPager(
    pagerState: PagerState,
    habitsByDate: ImmutableMap<LocalDate, ImmutableList<HabitCardModel>>,
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
        // Staff Fix: Use the DATE as the key, not the index.
        // This makes the pager item identity stable across state changes.
        key = { index -> 
            CalendarMathProvider.getDateFromDayIndex(today, index).toString() 
        },
        // Pre-load 7 pages in each direction (full week buffer) for absolute zero-latency swiping.
        // Combined with the ViewModel mapping cache, this makes swipes feel local and native.
        beyondViewportPageCount = 7
    ) { pageIndex ->
        val date = remember(pageIndex, today) {
            CalendarMathProvider.getDateFromDayIndex(today, pageIndex)
        }
        
        // Staff Optimization: Pass ImmutableList directly to children 
        // to ensure reliable Compose skipping.
        val habits = habitsByDate[date] ?: persistentListOf()
        val isToday = date == today

        HabitListContent(
            habits = habits,
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
    habits: ImmutableList<HabitCardModel>,
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
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = habits,
                key = { it.id },
                contentType = { it.type }
            ) { habit ->
                // Principal Fix: To keep HabitBuildCard/HabitTaskCard skippable, 
                // we must NOT pass newly created lambdas that capture changing values 
                // (like 'date') on every recomposition. We remember them per item key.
                
                when (habit.type) {
                    HabitType.BUILD -> {
                        val onFinished = remember(date) { { h: HabitCardModel -> onTimerFinished(h, date) } }
                        val onUnfinish = remember(date) { { id: Int -> onUnfinishTimer(id, date) } }
                        val onStart = remember(date) { { h: HabitCardModel -> onStartTimer(h, date) } }
                        val onPause = remember(date) { { h: HabitCardModel -> onPauseTimer(h, date) } }
                        val onResume = remember(date) { { h: HabitCardModel -> onResumeTimer(h, date) } }

                        HabitBuildCard(
                            habit = habit,
                            isToday = isToday,
                            onTimerFinished = onFinished,
                            onUnfinishTimer = onUnfinish,
                            onCardClick = onHabitCardClick,
                            onStartTimer = onStart,
                            onPauseTimer = onPause,
                            onResumeTimer = onResume
                        )
                    }

                    HabitType.TASK -> {
                        val onCompleted = remember(date) { { id: Int, completed: Boolean -> onTaskCompleted(id, date, completed) } }
                        val onIncrement = remember(date) { { id: Int -> onTaskIncrement(id, date) } }
                        
                        HabitTaskCard(
                            habit = habit,
                            isToday = isToday,
                            onTaskCompleted = onCompleted,
                            onTaskIncrement = onIncrement,
                            onCardClick = onHabitCardClick
                        )
                    }

                    HabitType.QUIT -> {
                        val onCompleted = remember(date) { { id: Int, completed: Boolean -> onTaskCompleted(id, date, completed) } }
                        
                        HabitQuitCard(
                            habit = habit,
                            isToday = isToday,
                            onTaskCompleted = onCompleted,
                            onCardClick = onHabitCardClick
                        )
                    }
                }
            }
        }
    }
}
