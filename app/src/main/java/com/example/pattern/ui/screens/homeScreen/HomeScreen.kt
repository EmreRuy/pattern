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
import com.example.pattern.data.model.HabitCardModel
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.ui.screens.homeScreen.components.EmptyStateMessage
import com.example.pattern.ui.screens.homeScreen.components.HabitCardsPager
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.utils.generateNext365Days
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    // Initial index for "today"
    val todayIndex = remember(dayList) {
        val today = LocalDate.now()
        val index = dayList.indexOfFirst { it.date == today }
        if (index != -1) index else dayList.size / 2
    }

    // Pager state for the calendar weeks
    val calendarPagerState = rememberPagerState(
        initialPage = todayIndex / 7,
        pageCount = { dayList.size / 7 }
    )

    // Pager state for the habit cards (daily)
    val habitPagerState = rememberPagerState(
        initialPage = todayIndex,
        pageCount = { dayList.size }
    )

    // Synchronize local selection index with ViewModel state
    val selectedDayIndex = remember(uiState.selectedDate, dayList) {
        dayList.indexOfFirst { it.date == uiState.selectedDate }.coerceAtLeast(0)
    }

    // Update ViewModel and sync Calendar when Habit Pager changes
    LaunchedEffect(habitPagerState.currentPage) {
        val selectedDate = dayList[habitPagerState.currentPage].date
        if (selectedDate != uiState.selectedDate) {
            viewModel.onDateSelected(selectedDate)

            // Sync calendar week pager if needed
            val targetWeekPage = habitPagerState.currentPage / 7
            if (calendarPagerState.currentPage != targetWeekPage) {
                calendarPagerState.animateScrollToPage(targetWeekPage)
            }
        }
    }

    // Sync Habit Pager when date is selected from Calendar
    LaunchedEffect(selectedDayIndex) {
        if (habitPagerState.currentPage != selectedDayIndex) {
            habitPagerState.animateScrollToPage(selectedDayIndex)
        }
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
                        pagerState = calendarPagerState,
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
                val onTimerFinished = remember {
                    { habit: HabitCardModel, date: LocalDate ->
                        viewModel.finishTimer(habit.id, date)
                        triggerConfetti = true
                    }
                }
                val onUnfinishTimer = remember {
                    { id: Int, date: LocalDate -> viewModel.unfinishTimer(id, date) }
                }
                val onStartTimer = remember {
                    { habit: HabitCardModel, date: LocalDate -> viewModel.startTimer(habit.id, date) }
                }
                val onPauseTimer = remember {
                    { habit: HabitCardModel, date: LocalDate -> viewModel.pauseTimer(habit.id, date) }
                }
                val onResumeTimer = remember {
                    { habit: HabitCardModel, date: LocalDate -> viewModel.resumeTimer(habit.id, date) }
                }
                val onTaskCompleted = remember {
                    { id: Int, date: LocalDate, completed: Boolean ->
                        viewModel.setTaskCompleted(id, date, completed)
                        if (completed) triggerConfetti = true
                    }
                }
                val onHabitClickInternal = remember(onHabitClick) { { id: Int -> onHabitClick(id) } }

                HabitCardsPager(
                    pagerState = habitPagerState,
                    dayList = dayList,
                    habitsByDate = uiState.habitsByDate,
                    hasAnyHabits = uiState.hasAnyHabits,
                    paddingValues = paddingValues,
                    onTimerFinished = onTimerFinished,
                    onUnfinishTimer = onUnfinishTimer,
                    onStartTimer = onStartTimer,
                    onPauseTimer = onPauseTimer,
                    onResumeTimer = onResumeTimer,
                    onTaskCompleted = onTaskCompleted,
                    onHabitCardClick = onHabitClickInternal
                )
            }
        }
    }
    }
