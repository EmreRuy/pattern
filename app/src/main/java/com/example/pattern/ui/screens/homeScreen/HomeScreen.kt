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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.pattern.ui.navigation.LocalNavActions
import com.example.pattern.ui.screens.homeScreen.components.HabitCardsPager
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.utils.CalendarMathProvider
import kotlinx.coroutines.launch
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
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(HomeUiEvent.OnResume)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

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
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val calendarPagerState = rememberPagerState(
        initialPage = CalendarMathProvider.getWeekPageIndex(state.selectedDate),
        pageCount = { 50000 }
    )

    val habitPagerState = rememberPagerState(
        initialPage = CalendarMathProvider.getDayPageIndex(state.selectedDate),
        pageCount = { 50000 * 7 }
    )

    // A flag to prevent pager settling events from triggering ViewModel updates 
    // during navigation transitions or initial setup.
    var isSyncLocked by remember { mutableStateOf(true) }

    // Visual Source of Truth: The date currently centered in the pager.
    val visuallySelectedDate by remember {
        derivedStateOf {
            CalendarMathProvider.getDateFromDayIndex(habitPagerState.currentPage)
        }
    }

    // 1. ViewModel -> Pager Sync (Source of Truth established on Re-entry)
    LaunchedEffect(state.selectedDate) {
        val targetDayPage = CalendarMathProvider.getDayPageIndex(state.selectedDate)
        if (habitPagerState.currentPage != targetDayPage) {
            // Instant snap to correct date to avoid intermediate state emissions
            habitPagerState.scrollToPage(targetDayPage)
        }
        // Unlock sync once the pager is aligned with the ViewModel
        isSyncLocked = false
    }

    // 2. Lifecycle Observer to lock sync during navigation
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isSyncLocked = true
            } else if (event == Lifecycle.Event.ON_RESUME) {
                // Keep locked until the ViewModel -> Pager sync above finishes
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 3. Habit Pager -> Calendar Pager & ViewModel
    LaunchedEffect(habitPagerState, isSyncLocked) {
        snapshotFlow { 
            Pair(habitPagerState.currentPage, habitPagerState.isScrollInProgress) 
        }.collect { (currentPage, isScrolling) ->
            if (!isScrolling && !isSyncLocked) {
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
                    timePeriod = state.timePeriod,
                    onTodayClick = {
                        scope.launch {
                            val targetPage = CalendarMathProvider.getDayPageIndex(LocalDate.now())
                            habitPagerState.animateScrollToPage(targetPage)
                        }
                    }
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
