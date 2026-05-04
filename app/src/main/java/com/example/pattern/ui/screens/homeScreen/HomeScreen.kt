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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.ui.screens.homeScreen.components.HabitCardsPager
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
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
    val mondayOfThisWeek = remember(today) { 
        today.minusDays((today.dayOfWeek.value - 1).toLong()) 
    }
    
    // Constants for the infinite pager
    val weekPivotPage = 25000
    val dayPivotIndex = weekPivotPage * 7

    val calendarPagerState = rememberPagerState(
        initialPage = weekPivotPage,
        pageCount = { 50000 }
    )

    val initialHabitPage = remember(today, dayPivotIndex) {
        dayPivotIndex + (today.dayOfWeek.value - 1)
    }
    val habitPagerState = rememberPagerState(
        initialPage = initialHabitPage,
        pageCount = { 50000 * 7 }
    )

    // Sync Pager -> ViewModel
    LaunchedEffect(habitPagerState.currentPage) {
        val offset = habitPagerState.currentPage - dayPivotIndex
        val selectedDate = mondayOfThisWeek.plusDays(offset.toLong())
        
        if (selectedDate != state.selectedDate) {
            onEvent(HomeUiEvent.OnDateSelected(selectedDate))
            
            val targetWeekPage = habitPagerState.currentPage / 7
            if (calendarPagerState.currentPage != targetWeekPage) {
                calendarPagerState.animateScrollToPage(targetWeekPage)
            }
        }
    }

    // Sync ViewModel -> Pager
    LaunchedEffect(state.selectedDate) {
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(mondayOfThisWeek, state.selectedDate)
        val targetHabitPage = dayPivotIndex + daysBetween.toInt()
        
        if (habitPagerState.currentPage != targetHabitPage) {
            habitPagerState.animateScrollToPage(targetHabitPage)
        }
    }

    // Sync Calendar Pager -> Habit Pager (when user swipes calendar)
    LaunchedEffect(calendarPagerState.currentPage) {
        val targetHabitWeekStart = calendarPagerState.currentPage * 7
        val currentHabitWeekStart = (habitPagerState.currentPage / 7) * 7
        if (targetHabitWeekStart != currentHabitWeekStart) {
            // Maintain the same day of week when swiping weeks if possible
            val dayOfWeekOffset = habitPagerState.currentPage % 7
            habitPagerState.animateScrollToPage(targetHabitWeekStart + dayOfWeekOffset)
        }
    }

    ConfettiView(explodeConfetti = state.explodeConfetti) {
        if (state.explodeConfetti) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                onEvent(HomeUiEvent.OnConfettiAnimationShown)
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
                        selectedDate = state.selectedDate,
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
                onHabitCardClick = onHabitClick
            )
        }
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
