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
import com.example.pattern.utils.generateNext365Days
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
    val dayList = remember { generateNext365Days() }
    
    val todayIndex = remember(dayList) {
        val today = LocalDate.now()
        dayList.indexOfFirst { it.date == today }.let { if (it != -1) it else dayList.size / 2 }
    }

    val calendarPagerState = rememberPagerState(
        initialPage = todayIndex / 7,
        pageCount = { dayList.size / 7 }
    )

    val habitPagerState = rememberPagerState(
        initialPage = todayIndex,
        pageCount = { dayList.size }
    )

    val selectedDayIndex = remember(state.selectedDate, dayList) {
        dayList.indexOfFirst { it.date == state.selectedDate }.coerceAtLeast(0)
    }

    // Sync Pager -> ViewModel
    LaunchedEffect(habitPagerState.currentPage) {
        val selectedDate = dayList[habitPagerState.currentPage].date
        if (selectedDate != state.selectedDate) {
            onEvent(HomeUiEvent.OnDateSelected(selectedDate))
            
            val targetWeekPage = habitPagerState.currentPage / 7
            if (calendarPagerState.currentPage != targetWeekPage) {
                calendarPagerState.animateScrollToPage(targetWeekPage)
            }
        }
    }

    // Sync ViewModel -> Pager
    LaunchedEffect(selectedDayIndex) {
        if (habitPagerState.currentPage != selectedDayIndex) {
            habitPagerState.animateScrollToPage(selectedDayIndex)
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
                        selectedDayIndex = selectedDayIndex,
                        onDaySelected = { index ->
                            onEvent(HomeUiEvent.OnDateSelected(dayList[index].date))
                        },
                        dayList = dayList
                    )
                }
            },
        ) { paddingValues ->
            HabitCardsPager(
                pagerState = habitPagerState,
                dayList = dayList,
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
