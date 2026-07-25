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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.pattern.ui.navigation.LocalNavActions
import com.example.pattern.ui.screens.homeScreen.components.HabitCardsPager
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.utils.CalendarMathProvider
import java.time.LocalDate

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenMenuScreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onPremiumClick: () -> Unit,
    onHomeReady: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is HomeUiState.Success && !state.isLoading) {
            onHomeReady()
        }
    }

    when (val state = uiState) {
        is HomeUiState.Success -> HomeContent(
            navController = navController,
            state = state,
            onEvent = viewModel::onEvent,
            onOpenMenuScreen = onOpenMenuScreen,
            onSettingsClick = onSettingsClick,
            onHabitClick = onHabitClick,
            onPremiumClick = onPremiumClick
        )
        is HomeUiState.Error -> ErrorScreen(state.message)
        else -> {} // Should not happen with exhaustive sealed interface
    }
}

@Composable
private fun HomeContent(
    navController: NavHostController,
    state: HomeUiState.Success,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenMenuScreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    val actions = LocalNavActions.current
    val calendarPagerState = rememberPagerState(
        initialPage = CalendarMathProvider.getWeekPageIndex(state.selectedDate),
        pageCount = { 50000 }
    )

    val habitPagerState = rememberPagerState(
        initialPage = CalendarMathProvider.getDayPageIndex(state.selectedDate),
        pageCount = { 50000 * 7 }
    )

    // Visual Source of Truth: The date currently centered in the pager.
    val visuallySelectedDate by remember {
        derivedStateOf {
            CalendarMathProvider.getDateFromDayIndex(habitPagerState.currentPage)
        }
    }

    // 1. Sync ViewModel changes TO UI (e.g. initial load or external reset)
    LaunchedEffect(state.selectedDate) {
        val targetDayPage = CalendarMathProvider.getDayPageIndex(state.selectedDate)
        if (habitPagerState.currentPage != targetDayPage && !habitPagerState.isScrollInProgress) {
            // Use scrollToPage (instant) when returning from navigation 
            // to prevent the "ghost swipe" effect.
            habitPagerState.scrollToPage(targetDayPage)
        }
    }

    // 2. Sync Habit Pager -> Calendar Pager & ViewModel
    LaunchedEffect(habitPagerState) {
        snapshotFlow { 
            Pair(habitPagerState.currentPage, habitPagerState.isScrollInProgress) 
        }.collect { (currentPage, isScrolling) ->
            // ONLY update the ViewModel when the pager has settled.
            if (!isScrolling) {
                val dateAtPage = CalendarMathProvider.getDateFromDayIndex(currentPage)
                if (dateAtPage != state.selectedDate) {
                    onEvent(HomeUiEvent.OnDateSelected(dateAtPage))
                }
            }

            // Immediate Visual Sync (Week Pager follows Habit Pager)
            val targetWeekPage = currentPage / 7
            if (calendarPagerState.currentPage != targetWeekPage) {
                calendarPagerState.scrollToPage(targetWeekPage)
            }
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
                    timePeriod = state.timePeriod
                )
            }
        },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                onItemClick = { item -> actions.navigateToBottomBarRoute(item.route) }
            )
        }
    ) { paddingValues ->
        HabitCardsPager(
            pagerState = habitPagerState,
            habitsByDate = state.habitsByDate,
            hasAnyHabits = state.hasAnyHabits,
            isLoading = state.isLoading,
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
private fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}
