package com.example.pattern.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.screens.homeScreen.HomeViewModel
import com.example.pattern.data.mapper.toCardModel
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.ui.navigation.Screens
import com.example.pattern.ui.screens.homeScreen.components.EmptyStateMessage
import com.example.pattern.ui.screens.homeScreen.components.HabitCards
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.utils.generateNext365Days
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit


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
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val dayList = remember { generateNext365Days() }

    // Find the index of "today" in the generated list
    val todayIndex = remember(dayList, today) {
        val index = dayList.indexOfFirst { it.date == today }
        if (index != -1) index else dayList.size / 2
    }

    // Selected day index (global index in dayList)
    var selectedDayIndex by remember { mutableIntStateOf(todayIndex) }

    // Pager state based on weeks
    val initialPage = todayIndex / 7
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { dayList.size / 7 }
    )

    // Confetti State
    var explodeConfetti by remember { mutableStateOf(false) }
    var triggerConfetti by remember { mutableStateOf(false) }

    // Selected date logic
    val selectedDate = remember(selectedDayIndex, dayList) {
        dayList[selectedDayIndex].date
    }
    val selectedDbIndex = selectedDate.dayOfWeek.value - 1
    val selectedDateKey = remember(selectedDate) { selectedDate.toString() }

    // Use derived state from uiState for today, or fetch for other days
    val isToday = selectedDate == today
    val dailyStatesForSelectedDate by if (isToday) {
        remember(uiState.todayStates) { mutableStateOf(uiState.todayStates.values.toList()) }
    } else {
        viewModel.getDailyStatesForDate(selectedDateKey).collectAsStateWithLifecycle(initialValue = emptyList())
    }

    // Build mapped habit list with senior-level performance optimization
    val habits = remember(selectedDateKey, uiState.habitList, dailyStatesForSelectedDate, uiState.streaks) {
        val selectedLocalDate = LocalDate.parse(selectedDateKey)
        
        uiState.habitList
            .filter { habit ->
                // Filter 1: Check if the habit was already created by this date
                val creationDate = Instant.ofEpochMilli(habit.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                val wasCreated = !selectedLocalDate.isBefore(creationDate)
                
                // Filter 2: Check if it's scheduled for this day of the week
                val isScheduled = habit.selectedDays.getOrNull(selectedDbIndex) == true
                
                wasCreated && isScheduled
            }
            .map { habit ->
                val daily = dailyStatesForSelectedDate.firstOrNull { it.habitId == habit.id }
                val streak = uiState.streaks[habit.id] ?: 0
                habit.toCardModel(daily, streak)
            }
    }
    // daily states exist for mapped habits
    LaunchedEffect(habits, selectedDateKey, dailyStatesForSelectedDate) {
        habits.forEach { habit ->
            val exists = dailyStatesForSelectedDate.any { it.habitId == habit.id }
            if (!exists) {
                viewModel.ensureDailyStateExists(habit.id, selectedDateKey)
            }
        }
    }
    // Confetti Triggering
    LaunchedEffect(triggerConfetti) {
        if (triggerConfetti) {
            delay(300)
            explodeConfetti = true
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
                    HomeTopBar(onMenuClick = onOpenMenuScreen,
                        onSettingsClick = onSettingsClick,
                        onPremiumClick = onPremiumClick
                    )
                    HomeCalendarSelector(
                        pagerState = pagerState,
                        selectedDayIndex = selectedDayIndex,
                        onDaySelected = { index ->
                            selectedDayIndex = index
                        },
                        dayList = dayList
                    )
                }
            },
        ) { paddingValues ->
            HabitCards(
                habits = habits,
                paddingValues = paddingValues,
                isToday = selectedDate == today,

                onTimerFinished = { habitCard ->
                    viewModel.finishTimer(habitCard.id, selectedDateKey)
                    triggerConfetti = true
                },
                onUnfinishTimer = { habitId ->
                    viewModel.unfinishTimer(habitId, selectedDateKey)
                },
                onStartTimer = { viewModel.startTimer(it.id, selectedDateKey) },
                onPauseTimer = { viewModel.pauseTimer(it.id, selectedDateKey) },
                onResumeTimer = { viewModel.resumeTimer(it.id, selectedDateKey) },
                onTaskCompleted = { habitId, completed ->
                    viewModel.setTaskCompleted(
                        habitId = habitId,
                        date = selectedDateKey,
                        completed = completed
                    )
                },
                onHabitCardClick = { id ->
                    onHabitClick(id)
                }
            )
            // Empty states
            if (!uiState.isLoading) {
                when {
                    habits.isEmpty() && uiState.habitList.isNotEmpty() ->
                        EmptyStateMessage(
                            paddingValues,
                            "No habits scheduled for this day!"
                        )

                    uiState.habitList.isEmpty() ->
                        EmptyStateMessage(
                            paddingValues,
                            "Start by adding your first habit!"
                        )
                }
            }
        }
    }
}
