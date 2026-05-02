package com.example.pattern.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.ui.screens.homeScreen.components.EmptyStateMessage
import com.example.pattern.ui.screens.homeScreen.components.HabitCards
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.utils.generateNext365Days
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenMenuScreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dayList = remember { generateNext365Days() }

    // Initial index for "today"
    val todayIndex = remember(dayList) {
        val today = LocalDate.now()
        val index = dayList.indexOfFirst { it.date == today }
        if (index != -1) index else dayList.size / 2
    }

    // Pager state for the calendar weeks
    val pagerState = rememberPagerState(
        initialPage = todayIndex / 7,
        pageCount = { dayList.size / 7 }
    )

    // Synchronize local selection index with ViewModel state
    val selectedDayIndex = remember(uiState.selectedDate, dayList) {
        dayList.indexOfFirst { it.date == uiState.selectedDate }.coerceAtLeast(0)
    }

    // Confetti animation control
    var explodeConfetti by remember { mutableStateOf(false) }
    var triggerConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(triggerConfetti) {
        if (triggerConfetti) {
            delay(300)
            explodeConfetti = true
            delay(3000)
            explodeConfetti = false
            triggerConfetti = false
        }
    }

    ConfettiView(explodeConfetti = explodeConfetti) {
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
                        pagerState = pagerState,
                        selectedDayIndex = selectedDayIndex,
                        onDaySelected = { index ->
                            viewModel.onDateSelected(dayList[index].date)
                        },
                        dayList = dayList
                    )
                }
            },
        ) { paddingValues ->
            if (!uiState.isLoading) {
                HabitCards(
                    habits = uiState.habits,
                    paddingValues = paddingValues,
                    isToday = uiState.isSelectedDateToday,
                    onTimerFinished = { habitCard ->
                        viewModel.finishTimer(habitCard.id)
                        triggerConfetti = true
                    },
                    onUnfinishTimer = { habitId ->
                        viewModel.unfinishTimer(habitId)
                    },
                    onStartTimer = { viewModel.startTimer(it.id) },
                    onPauseTimer = { viewModel.pauseTimer(it.id) },
                    onResumeTimer = { viewModel.resumeTimer(it.id) },
                    onTaskCompleted = { habitId, completed ->
                        viewModel.setTaskCompleted(habitId, completed)
                        if (completed) triggerConfetti = true
                    },
                    onHabitCardClick = onHabitClick
                )

                // Refined empty state handling
                if (uiState.habits.isEmpty()) {
                    val message = if (uiState.hasAnyHabits) {
                        "No habits scheduled for this day!"
                    } else {
                        "Start by adding your first habit!"
                    }
                    EmptyStateMessage(paddingValues, message)
                }
            }
        }
    }
}
