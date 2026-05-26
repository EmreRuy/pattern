package com.example.pattern.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.screens.homeScreen.components.HabitCardsPager
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.utils.CalendarMathProvider
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenMenuScreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingScreen()
        is HomeUiState.Success -> HomeContent(
            state = state,
            onEvent = viewModel::onEvent,
            onOpenMenuScreen = onOpenMenuScreen,
            onSettingsClick = onSettingsClick,
            onHabitClick = onHabitClick,
            onPremiumClick = onPremiumClick
        )
        is HomeUiState.Error -> ErrorScreen(state.message)
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenMenuScreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    val today = remember { LocalDate.now() }
    
    val calendarPagerState = rememberPagerState(
        initialPage = CalendarMathProvider.getWeekPageIndex(today, state.selectedDate),
        pageCount = { 50000 }
    )

    val habitPagerState = rememberPagerState(
        initialPage = CalendarMathProvider.getDayPageIndex(today, state.selectedDate),
        pageCount = { 50000 * 7 }
    )

    // Visual Source of Truth: The date currently centered in the pager.
    // Driving the UI selection from this instead of the ViewModel state
    // provides instant visual feedback while swiping.
    val visuallySelectedDate by remember {
        derivedStateOf {
            CalendarMathProvider.getDateFromDayIndex(today, habitPagerState.currentPage)
        }
    }

    // 1. Sync Pager to ViewModel (External changes only)
    LaunchedEffect(state.selectedDate) {
        val targetDayPage = CalendarMathProvider.getDayPageIndex(today, state.selectedDate)
        // Only animate if the difference is caused by something other than the user swiping
        if (habitPagerState.currentPage != targetDayPage && !habitPagerState.isScrollInProgress) {
            habitPagerState.animateScrollToPage(targetDayPage)
        }
    }

    // 2. Immediate Habit Pager -> Calendar Pager sync
    val habitWeekPage by remember { derivedStateOf { habitPagerState.currentPage / 7 } }
    LaunchedEffect(habitWeekPage) {
        if (calendarPagerState.currentPage != habitWeekPage) {
            calendarPagerState.scrollToPage(habitWeekPage)
        }
    }

    // 3. Proactive ViewModel Update
    // As the page changes, we update the ViewModel's selectedDate.
    // Because we have a wide data window in the ViewModel, this won't cause lag.
    LaunchedEffect(habitPagerState.currentPage) {
        val dateAtPage = CalendarMathProvider.getDateFromDayIndex(today, habitPagerState.currentPage)
        if (dateAtPage != state.selectedDate) {
            onEvent(HomeUiEvent.OnDateSelected(dateAtPage))
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .fillMaxWidth()
            ) {
                HomeTopBar(
                    onMenuClick = onOpenMenuScreen,
                    onSettingsClick = onSettingsClick,
                    onPremiumClick = onPremiumClick
                )
                HomeCalendarSelector(
                    pagerState = calendarPagerState,
                    selectedDate = visuallySelectedDate,
                    onDateSelected = { date ->
                        onEvent(HomeUiEvent.OnDateSelected(date))
                    }
                )
            }
        },
    ) { paddingValues ->
        HabitCardsPager(
            pagerState = habitPagerState,
            habitsByDate = state.habitsByDate,
            hasAnyHabits = state.hasAnyHabits,
            paddingValues = paddingValues,
            onTimerFinished = { habit, date -> onEvent(HomeUiEvent.OnTimerFinish(habit.id, date)) },
            onUnfinishTimer = { id, date -> onEvent(HomeUiEvent.OnTimerUnfinish(id, date)) },
            onStartTimer = { habit, date -> onEvent(HomeUiEvent.OnTimerStart(habit.id, date)) },
            onPauseTimer = { habit, date -> onEvent(HomeUiEvent.OnTimerPause(habit.id, date)) },
            onResumeTimer = { habit, date -> onEvent(HomeUiEvent.OnTimerResume(habit.id, date)) },
            onTaskCompleted = { id, date, completed -> onEvent(HomeUiEvent.OnTaskToggle(id, date, completed)) },
            onTaskIncrement = { id, date -> onEvent(HomeUiEvent.OnTaskIncrement(id, date)) },
            onHabitCardClick = onHabitClick
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}
