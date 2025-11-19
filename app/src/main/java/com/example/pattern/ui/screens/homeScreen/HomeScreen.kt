package com.example.pattern.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.pattern.R
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.data.local.toCardModel
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.ui.navigation.Screens
import com.example.pattern.ui.screens.homeScreen.components.HabitCards
import com.example.pattern.utils.generateNext365Days
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    //Base Habit List
    val uiState by habitViewModel.homeUiState.collectAsStateWithLifecycle()

    // For Confetti
    var explodeConfetti by remember { mutableStateOf(false) }
    var triggerConfetti by remember { mutableStateOf(false) }

    // Date Selector Logic
    val listState = rememberLazyListState()
    val selectedDay = remember { mutableIntStateOf(180) }
    val dayList = remember { generateNext365Days() }

    LaunchedEffect(Unit) {
        listState.scrollToItem(selectedDay.intValue)
    }

    LaunchedEffect(triggerConfetti) {
        if (triggerConfetti) {
            delay(300)
            explodeConfetti = true
        }
    }

    val today = LocalDate.now(ZoneId.systemDefault())
    val selectedDate = remember(selectedDay.intValue) {
        today.minusDays(180).plusDays(selectedDay.intValue.toLong())
    }

    val selectedDbIndex = selectedDate.dayOfWeek.value - 1
    val selectedDateKey = selectedDate.toString()


    // Daily States for Selected Date
    val dailyStates by habitViewModel
        .getDailyStatesForDate(selectedDateKey)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Combine Habit + DailyState
    val habits = uiState.habitList
        .filter { habit ->
            habit.selectedDays.getOrNull(selectedDbIndex) == true
        }
        .map { habit ->
            val daily = dailyStates.firstOrNull { it.habitId == habit.id }
            habit.toCardModel(daily)
        }

    // Confetti
    ConfettiView(
        explodeConfetti = explodeConfetti
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.menu),
                            contentDescription = "Menu Icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 22.sp,
                            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "Settings Icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //Calendar section
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(end = 8.dp),
                        state = listState
                    ) {
                        items(365) { index ->
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { selectedDay.intValue = index }
                                        .background(
                                            if (selectedDay.intValue == index)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayList[index],
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            val isToday = selectedDate == today
            //Habit List on Homescreen
            HabitCards(
                habits = habits,
                paddingValues = paddingValues,
                onHabitChecked = {},
                isToday = isToday,

                // passing date to viewModel
                onTimerFinished = { habitCard ->
                    habitViewModel.finishTimer(
                        habitId = habitCard.id,
                        date = selectedDateKey
                    )
                    triggerConfetti = true
                },
                onStartTimer = { habitCard ->
                    habitViewModel.startTimer(
                        habitId = habitCard.id,
                        date = selectedDateKey
                    )
                },
                onPauseTimer = { habitCard ->
                    habitViewModel.pauseTimer(
                        habitId = habitCard.id,
                        date = selectedDateKey
                    )
                },
                onResumeTimer = { habitCard ->
                    habitViewModel.resumeTimer(
                        habitId = habitCard.id,
                        date = selectedDateKey
                    )
                },
                onHabitCardClick = { habitId ->
                    navController.navigate(Screens.HabitDetail.createRoute(habitId))
                }
            )

            // Empty States
            if (habits.isEmpty() && uiState.habitList.isNotEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No habits scheduled for this day!",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else if (uiState.habitList.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Start by adding your first habit!",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}



