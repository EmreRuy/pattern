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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.screens.homeScreen.components.HabitCardsPager
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.utils.CalendarMathProvider
import java.time.LocalDate
import androidx.compose.ui.unit.dp

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween

import com.example.pattern.ui.util.TimerTickerProvider

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenMenuScreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Lead Expert Fix: Maintain a "last success state" to prevent blinking to loading 
    // when returning to the screen or during fast refreshes.
    var lastSuccessState by remember { mutableStateOf<HomeUiState.Success?>(null) }
    
    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.Success) {
            lastSuccessState = uiState as HomeUiState.Success
        }
    }

    // Optimization: If we are "Loading" but have old data, show the old data 
    // to keep the UI stable while the fresh data is being processed.
    val displayState = remember(uiState, lastSuccessState) {
        if (uiState is HomeUiState.Loading && lastSuccessState != null) {
            lastSuccessState!!
        } else {
            uiState
        }
    }

    // Staff Fix: Use a discriminator for Crossfade so it only animates between 
    // TOP-LEVEL states (Loading/Success/Error), not between different Success instances.
    val stateKey = remember(displayState) {
        when (displayState) {
            is HomeUiState.Loading -> "loading"
            is HomeUiState.Success -> "success"
            is HomeUiState.Error -> "error"
        }
    }

    Crossfade(
        targetState = stateKey,
        label = "HomeScreenStateTransition",
        animationSpec = tween(durationMillis = 400)
    ) { key ->
        when (key) {
            "loading" -> LoadingScreen()
            "success" -> {
                // Cast is safe here because of the stateKey logic
                val successState = displayState as HomeUiState.Success
                TimerTickerProvider {
                    HomeContent(
                        state = successState,
                        onEvent = viewModel::onEvent,
                        onOpenMenuScreen = onOpenMenuScreen,
                        onSettingsClick = onSettingsClick,
                        onHabitClick = onHabitClick,
                        onPremiumClick = onPremiumClick
                    )
                }
            }
            "error" -> {
                val errorState = displayState as HomeUiState.Error
                ErrorScreen(
                    message = errorState.message,
                    onRetry = { viewModel.onEvent(HomeUiEvent.OnRetry) }
                )
            }
        }
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

    // 1. External Sync: ViewModel -> Pager (Only when NOT scrolling)
    LaunchedEffect(state.selectedDate) {
        val targetDayPage = CalendarMathProvider.getDayPageIndex(today, state.selectedDate)
        if (habitPagerState.currentPage != targetDayPage && !habitPagerState.isScrollInProgress) {
            val diff = kotlin.math.abs(habitPagerState.currentPage - targetDayPage)
            if (diff > 7) {
                habitPagerState.scrollToPage(targetDayPage)
            } else {
                habitPagerState.animateScrollToPage(targetDayPage)
            }
        }
    }

    // 2. Internal Sync: Habit Pager -> Calendar Pager (Instant visual sync)
    LaunchedEffect(habitPagerState.currentPage) {
        val targetWeekPage = habitPagerState.currentPage / 7
        if (calendarPagerState.currentPage != targetWeekPage) {
            // Calendar is small, scroll is fast
            calendarPagerState.scrollToPage(targetWeekPage)
        }
    }

    // 3. ViewModel Sync: Habit Pager -> ViewModel (Only after SETTLING)
    // This is the "Zero-Blink" secret: don't disturb the ViewModel while the finger is on the screen.
    LaunchedEffect(habitPagerState.isScrollInProgress, habitPagerState.currentPage) {
        if (!habitPagerState.isScrollInProgress) {
            val dateAtPage = CalendarMathProvider.getDateFromDayIndex(today, habitPagerState.currentPage)
            // Use an epsilon-check to avoid redundant events
            if (dateAtPage.toEpochDay() != state.selectedDate.toEpochDay()) {
                onEvent(HomeUiEvent.OnDateSelected(dateAtPage))
            }
        }
    }

    // Optimization: Memoize callbacks to prevent HabitCardsPager from recomposing 
    // just because HomeContent recomposed due to a date selection change.
    val onTimerFinished = remember(onEvent) { { habit: com.example.pattern.ui.model.HabitCardModel, date: LocalDate -> onEvent(HomeUiEvent.OnTimerFinish(habit.id, date)) } }
    val onUnfinishTimer = remember(onEvent) { { id: Int, date: LocalDate -> onEvent(HomeUiEvent.OnTimerUnfinish(id, date)) } }
    val onStartTimer = remember(onEvent) { { habit: com.example.pattern.ui.model.HabitCardModel, date: LocalDate -> onEvent(HomeUiEvent.OnTimerStart(habit.id, date)) } }
    val onPauseTimer = remember(onEvent) { { habit: com.example.pattern.ui.model.HabitCardModel, date: LocalDate -> onEvent(HomeUiEvent.OnTimerPause(habit.id, date)) } }
    val onResumeTimer = remember(onEvent) { { habit: com.example.pattern.ui.model.HabitCardModel, date: LocalDate -> onEvent(HomeUiEvent.OnTimerResume(habit.id, date)) } }
    val onTaskCompleted = remember(onEvent) { { id: Int, date: LocalDate, completed: Boolean -> onEvent(HomeUiEvent.OnTaskToggle(id, date, completed)) } }
    val onTaskIncrement = remember(onEvent) { { id: Int, date: LocalDate -> onEvent(HomeUiEvent.OnTaskIncrement(id, date)) } }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
            ) {
                HomeTopBar(
                    onMenuClick = onOpenMenuScreen,
                    onSettingsClick = onSettingsClick,
                    onPremiumClick = onPremiumClick
                )
                HomeCalendarSelector(
                    pagerState = calendarPagerState,
                    // Use visuallySelectedDate for the Calendar UI so it moves instantly with the swipe
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
            onTimerFinished = onTimerFinished,
            onUnfinishTimer = onUnfinishTimer,
            onStartTimer = onStartTimer,
            onPauseTimer = onPauseTimer,
            onResumeTimer = onResumeTimer,
            onTaskCompleted = onTaskCompleted,
            onTaskIncrement = onTaskIncrement,
            onHabitCardClick = onHabitClick
        )
    }
}

@Composable
private fun LoadingScreen() {
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}
