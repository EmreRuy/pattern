package com.example.pattern.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.R
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.data.local.toUiModel
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.ui.screens.homeScreen.components.HabitCards
import com.example.pattern.utils.generateNext365Days
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId

@Preview(showBackground = true)
@Composable
fun PreviewOfHomeScreen() {
    HomeScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel = hiltViewModel()
) {
    // 1. Collect the real-time state from the ViewModel
    val uiState by habitViewModel.homeUiState.collectAsStateWithLifecycle()

    var explodeConfetti by remember { mutableStateOf(false) }
    var triggerConfetti by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val selectedDay = remember { mutableIntStateOf(180) }
    // NOTE: Ensure generateNext365Days is accessible here or defined at file level
    val dayList = remember { generateNext365Days() }

    LaunchedEffect(Unit) {
        // Scroll to 'today' (index 180) on initial load
        listState.scrollToItem(selectedDay.intValue)
    }
    LaunchedEffect(triggerConfetti) {
        if (triggerConfetti) {
            delay(300)
            explodeConfetti = true
            triggerConfetti = false
        }
    }

    // --- START OF FIXED DATE/INDEX LOGIC ---

    // 1. Determine the exact LocalDate for the selected index (mirroring generateNext365Days)
    val today = LocalDate.now(ZoneId.systemDefault())

    val selectedDate = remember(selectedDay.intValue) {
        today
            .minusDays(180)             // Start from 180 days ago
            .plusDays(selectedDay.intValue.toLong()) // Add the current index (0-364)
    }

    // 2. Calculate the required database index (0=Mon, 6=Sun)
    // DayOfWeek.value is 1 (Mon) to 7 (Sun). We convert it to 0-6.
    val selectedDbIndex = selectedDate.dayOfWeek.value - 1

    // 3. Filter the live list of habits based on the correct database index
    val habits = uiState.habitList
        .filter { habit ->
            // Check if the habit is marked for the calculated day index
            habit.selectedDays.getOrNull(selectedDbIndex) == true
        }
        .map { it.toUiModel() } // Assuming 'toUiModel()' is defined elsewhere and accessible

    // --- END OF FIXED DATE/INDEX LOGIC ---


    // Assuming ConfettiView is defined elsewhere
    ConfettiView(
        explodeConfetti = explodeConfetti,
        explodeConfettiCallback = { explodeConfetti = false }
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
                    // Top Row with Menu, App Name, Settings (unchanged)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // These require R.drawable.menu and R.drawable.settings to be available
                        Icon(
                            painter = painterResource(id = R.drawable.menu),
                            contentDescription = "Menu Icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                        // 2. App Name Text
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 22.sp,
                            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // 3. Settings Icon
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "Settings Icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Calendar/Date Selector Row (unchanged)
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
                                        .clickable { selectedDay.intValue = index }
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

            // 4. Pass the filtered list to your display component
            HabitCards(
                habits = habits,
                paddingValues = paddingValues,
                onHabitChecked = {
                    triggerConfetti = true
                },
                onHabitTimeChecked = {
                    triggerConfetti = true
                }
            )
            // Optional: Show a message if there are no habits for the selected day
            if (habits.isEmpty() && uiState.habitList.isNotEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No habits scheduled for this day!",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else if (uiState.habitList.isEmpty() && !uiState.isLoading) {
                // Initial empty state when no habits exist in the database
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
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


