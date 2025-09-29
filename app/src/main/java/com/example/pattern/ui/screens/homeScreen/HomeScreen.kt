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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
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
import com.example.pattern.R
import com.example.pattern.ui.components.ConfettiView
import com.example.pattern.data.model.Habit
import com.example.pattern.ui.screens.homeScreen.components.HabitCards
import com.example.pattern.utils.generateNext365Days
import kotlinx.coroutines.delay

@Preview(showBackground = true)
@Composable
fun PreviewOfHomeScreen() {
    HomeScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var explodeConfetti by remember { mutableStateOf(false) }
    var triggerConfetti by remember { mutableStateOf(false) }
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
            triggerConfetti = false
        }
    }
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

                val allHabits = remember {
                    listOf(
                        Habit("Read 10 pages", Icons.Default.CheckCircle),
                        Habit("Meditate", Icons.Default.Build),
                        Habit("Workout", Icons.Default.AccountBox),
                        Habit("Drink water", Icons.Default.AddCircle),
                        Habit("Listen Music", Icons.Default.Call),
                        Habit("Dance Practice", Icons.Default.DateRange),
                        Habit("Dance Practice", Icons.Default.DateRange),
                        Habit("Dance Practice", Icons.Default.DateRange),
                        Habit("Dance Practice", Icons.Default.DateRange),
                        Habit("Dance Practice", Icons.Default.DateRange),
                        Habit("Drawing course", Icons.Default.Email)
                    )
                }

                val habits = allHabits.filterIndexed { index, _ ->
                    (index + selectedDay.intValue) % 2 == 0
                }

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
            }
        }
}


