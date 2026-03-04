package com.example.pattern.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.data.mapper.toCardModel
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.ui.navigation.Screens
import com.example.pattern.ui.screens.homeScreen.components.EmptyStateMessage
import com.example.pattern.ui.screens.homeScreen.components.HabitCards
import com.example.pattern.ui.screens.homeScreen.components.HomeCalendarSelector
import com.example.pattern.ui.screens.homeScreen.components.HomeTopBar
import com.example.pattern.utils.generateNext365Days
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel = hiltViewModel(),
    navController: NavHostController,
    onOpenMenuSheet: () -> Unit,
    onOpenSettingsSheet: () -> Unit
) {
    val uiState by habitViewModel.homeUiState.collectAsStateWithLifecycle()
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val listState = rememberLazyListState()
    val selectedDay = remember { mutableIntStateOf(180) }
    val dayList = remember { generateNext365Days() }
    // Confetti State
    var explodeConfetti by remember { mutableStateOf(false) }
    var triggerConfetti by remember { mutableStateOf(false) }
    // Scroll to current day on first composition
    LaunchedEffect(Unit) {
        listState.scrollToItem(selectedDay.intValue)
    }
    // Selected date logic
    val selectedDate = remember(selectedDay.intValue) {
        today.minusDays(180).plusDays(selectedDay.intValue.toLong())
    }
    val selectedDbIndex = selectedDate.dayOfWeek.value - 1
    val selectedDateKey = selectedDate.toString()
    // Daily states for this day
    val dailyStates by habitViewModel
        .getDailyStatesForDate(selectedDateKey)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    // Build mapped habit list
    val habits = remember(selectedDateKey, uiState.habitList, dailyStates) {
        uiState.habitList
            .filter { it.selectedDays.getOrNull(selectedDbIndex) == true }
            .map { habit ->
                val daily = dailyStates.firstOrNull { it.habitId == habit.id }
                habit.toCardModel(daily)
            }
    }
    // daily states exist for mapped habits
    LaunchedEffect(habits, selectedDateKey, dailyStates) {
        habits.forEach { habit ->
            val exists = dailyStates.any { it.habitId == habit.id }
            if (!exists) {
                habitViewModel.ensureDailyStateExists(habit.id, selectedDateKey)
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
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                ) {
                    HomeTopBar(onMenuClick = onOpenMenuSheet,
                        onSettingsClick = onOpenSettingsSheet
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HomeCalendarSelector(
                        listState = listState,
                        selectedDay = selectedDay.intValue,
                        onDaySelected = { selectedDay.intValue = it },
                        dayList = dayList
                    )
                }
            }
        ) { paddingValues ->
            HabitCards(
                habits = habits,
                paddingValues = paddingValues,
                isToday = selectedDate == today,

                onTimerFinished = { habitCard ->
                    habitViewModel.finishTimer(habitCard.id, selectedDateKey)
                    triggerConfetti = true
                },
                onStartTimer = { habitViewModel.startTimer(it.id, selectedDateKey) },
                onPauseTimer = { habitViewModel.pauseTimer(it.id, selectedDateKey) },
                onResumeTimer = { habitViewModel.resumeTimer(it.id, selectedDateKey) },
                onTaskCompleted = { habitId, completed ->
                    habitViewModel.setTaskCompleted(
                        habitId = habitId,
                        date = selectedDateKey,
                        completed = completed
                    )
                },
                onHabitCardClick = { id ->
                    navController.navigate(Screens.HabitDetail.createRoute(id))
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



