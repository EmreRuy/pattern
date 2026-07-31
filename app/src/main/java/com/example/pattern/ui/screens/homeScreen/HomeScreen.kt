package com.example.pattern.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.pattern.ui.model.HabitCardModel
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
            onProject = viewModel::project,
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
    onProject: (LocalDate) -> List<HabitCardModel>,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenMenuScreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    val actions = LocalNavActions.current
    val scope = rememberCoroutineScope()

    // Safety: Capture latest event handler and state to avoid stale references in LaunchedEffects
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentSelectedDate by rememberUpdatedState(state.selectedDate)

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

    // 1. ViewModel -> Pager Sync (Only for explicit jumps like 'Today' or external resets)
    LaunchedEffect(state.selectedDate) {
        val targetDayPage = CalendarMathProvider.getDayPageIndex(state.selectedDate)
        if (habitPagerState.currentPage != targetDayPage && !habitPagerState.isScrollInProgress) {
            // Use scrollToPage for large jumps (like initial load or far dates) to avoid jank
            if (Math.abs(habitPagerState.currentPage - targetDayPage) > 7) {
                habitPagerState.scrollToPage(targetDayPage)
            } else {
                habitPagerState.animateScrollToPage(targetDayPage)
            }
        }
    }

    // 2. Habit Pager -> ViewModel & Calendar Pager Sync
    LaunchedEffect(habitPagerState, calendarPagerState) {
        snapshotFlow { habitPagerState.currentPage to habitPagerState.isScrollInProgress }
            .collect { (currentPage, isScrolling) ->
                // Keep Calendar Pager (Week) in sync with Habit Pager (Day) during scroll
                val targetWeekPage = currentPage / 7
                if (calendarPagerState.currentPage != targetWeekPage) {
                    calendarPagerState.scrollToPage(targetWeekPage)
                }

                if (!isScrolling) {
                    val dateAtPage = CalendarMathProvider.getDateFromDayIndex(currentPage)
                    if (dateAtPage != currentSelectedDate) {
                        currentOnEvent(HomeUiEvent.OnDateSelected(dateAtPage))
                    }
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
            onProject = onProject,
            projectionData = state.projectionData,
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
