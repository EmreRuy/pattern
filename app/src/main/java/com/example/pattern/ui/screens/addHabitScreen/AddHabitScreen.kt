package com.example.pattern.ui.screens.addHabitScreen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pattern.data.local.HabitViewModel
import com.example.pattern.ui.screens.addHabitScreen.components.EmojiSelector
import com.example.pattern.ui.screens.addHabitScreen.components.HabitNameCard
import com.example.pattern.ui.screens.addHabitScreen.components.HabitTypeSelectorModern
import com.example.pattern.ui.screens.addHabitScreen.components.SaveHabitButton
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onSaveSuccess: () -> Unit,
    habitViewModel: HabitViewModel = hiltViewModel()
) {
    var habitName by remember { mutableStateOf("") }
    var habitType by remember { mutableStateOf("Grow") }
    var reminderEnabled by remember { mutableStateOf(false) }
    // var reminderTime by remember { mutableStateOf(LocalTime.now()) }
    var emoji by remember { mutableStateOf("🔥") }
    var buildHabitDays by remember { mutableStateOf(listOf<DayOfWeek>()) }
    val focusManager = LocalFocusManager.current
    //For the missing hours and minutes yet
    var durationHours by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(30) }
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HabitNameCard(habitName) { habitName = it }
                HabitTypeSelectorModern(
                    selectedType = habitType,
                    onTypeChange = { habitType = it },
                    selectedDays = buildHabitDays,
                    onDaysChange = { buildHabitDays = it },
                    durationHours = durationHours,
                    durationMinutes = durationMinutes,
                    onDurationChange = { h, m ->
                        durationHours = h
                        durationMinutes = m
                    }
                )
                //  ReminderCard(reminderEnabled, reminderTime, onToggle = { reminderEnabled = it })
                EmojiSelector(emoji) { emoji = it }
                SaveHabitButton(
                    habitName = habitName,
                    habitType = habitType,
                    buildHabitDays = buildHabitDays,
                    durationHours = durationHours,
                    durationMinutes = durationMinutes,
                    reminderEnabled = reminderEnabled,
                    emoji = emoji,
                    habitViewModel = habitViewModel,
                    onSaveSuccess = onSaveSuccess
                )
            }
        }
    }
}



