package com.example.pattern.ui.screens.addHabitScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pattern.data.local.HabitType
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.ui.screens.addHabitScreen.components.EmojiSelector
import com.example.pattern.ui.screens.addHabitScreen.components.HabitDetailsCard
import com.example.pattern.ui.screens.addHabitScreen.components.HabitTypeSelectorModern
import com.example.pattern.ui.screens.addHabitScreen.components.ReminderCard
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onSaveSuccess: () -> Unit,
    habitViewModel: HabitViewModel = hiltViewModel()
) {
    var habitName by remember { mutableStateOf("") }
    var habitType by remember { mutableStateOf("Build") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf(LocalTime.now()) }
    var emoji by remember { mutableStateOf("🔥") }
    var buildHabitDays by remember { mutableStateOf(listOf<DayOfWeek>()) }

    // State for the missing duration
    var durationHours by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(30) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("New Habit") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus(force = true)
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HabitDetailsCard(habitName) { habitName = it }

                HabitTypeSelectorModern(
                    selectedType = habitType,
                    onTypeChange = { habitType = it },
                    selectedDays = buildHabitDays,
                    onDaysChange = { buildHabitDays = it },
                    // Pass duration state down
                    /*  durationHours = durationHours,
                      onDurationHoursChange = { durationHours = it },
                      durationMinutes = durationMinutes,
                      onDurationMinutesChange = { durationMinutes = it } */
                    // will be added later, remember this!!
                )

                ReminderCard(reminderEnabled, reminderTime, onToggle = { reminderEnabled = it })
                EmojiSelector(emoji) { emoji = it }

                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    Button(
                        onClick = {
                            // Converting the habit type string to the Enum
                            val habitTypeEnum = when (habitType) {
                                "Build" -> HabitType.BUILD
                                "Quit" -> HabitType.QUIT
                                "Task" -> HabitType.TASK
                                else -> HabitType.BUILD
                            }
                            // Convert the DayOfWeek list to the database's List<Boolean> format
                            val dayListBooleans = DayOfWeek.entries.map { it in buildHabitDays }

                            // this one Calls the ViewModel function to insert data
                            habitViewModel.saveNewHabit(
                                name = habitName,
                                type = habitTypeEnum,
                                durationHours = durationHours,
                                durationMinutes = durationMinutes,
                                selectedDays = dayListBooleans,
                                reminderEnabled = reminderEnabled,
                                iconCode = emoji
                            )

                            onSaveSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = habitName.isNotBlank()
                    ) {
                        Text("Save", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}



