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
import com.example.pattern.ui.screens.addHabitScreen.components.ColorSelector
import com.example.pattern.ui.screens.addHabitScreen.components.EmojiSelector
import com.example.pattern.ui.screens.addHabitScreen.components.HabitNameCard
import com.example.pattern.ui.screens.addHabitScreen.components.HabitTypeSelectorCard
import com.example.pattern.ui.screens.addHabitScreen.components.HandleColorSheet
import com.example.pattern.ui.screens.addHabitScreen.components.HandleEmojiSheet
import com.example.pattern.ui.screens.addHabitScreen.components.HandleHabitNameSheet
import com.example.pattern.ui.screens.addHabitScreen.components.HandleHabitTypeSheet
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
    var emoji by remember { mutableStateOf("🔥") }
    var buildHabitDays by remember { mutableStateOf(listOf<DayOfWeek>()) }
    val focusManager = LocalFocusManager.current
    //For the missing hours and minutes yet
    var durationHours by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var selectedColor by remember { mutableStateOf("#77DD77") }
    var showEmojiSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showNameSheet by remember { mutableStateOf(false) }

    var showHabitTypeSheet by remember { mutableStateOf(false) }
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
                HabitNameCard(
                    habitName = habitName,
                    onOpen = { showNameSheet = true }
                )
                HandleHabitNameSheet(
                    showSheet = showNameSheet,
                    habitName = habitName,
                    onNameChange = { habitName = it },
                    onDismiss = { showNameSheet = false }
                )
                ColorSelector(
                    selectedColor = selectedColor,
                    onOpen = { showColorSheet = true }
                )
                HandleColorSheet(
                    showColorSheet = showColorSheet,
                    selectedColor = selectedColor,
                    onColorChange = { selectedColor = it },
                    onDismiss = { showColorSheet = false }
                )
                HabitTypeSelectorCard(
                    selectedType = habitType,
                    onOpen = { showHabitTypeSheet = true }
                )

                HandleHabitTypeSheet(
                    showSheet = showHabitTypeSheet,
                    selectedType = habitType,
                    selectedDays = buildHabitDays,
                    durationHours = durationHours,
                    durationMinutes = durationMinutes,
                    onTypeChange = { habitType = it },
                    onDaysChange = { buildHabitDays = it },
                    onDurationChange = { h, m ->
                        durationHours = h
                        durationMinutes = m
                    },
                    onDismiss = { showHabitTypeSheet = false }
                )
                //  ReminderCard(reminderEnabled, reminderTime, onToggle = { reminderEnabled = it })
                EmojiSelector(
                    selectedEmoji = emoji,
                    onOpen = { showEmojiSheet = true }
                )
                HandleEmojiSheet(
                    showEmojiSheet = showEmojiSheet,
                    selectedEmoji = emoji,
                    onEmojiChange = { emoji = it },
                    onDismiss = { showEmojiSheet = false }
                )
                SaveHabitButton(
                    habitName = habitName,
                    habitType = habitType,
                    buildHabitDays = buildHabitDays,
                    durationHours = durationHours,
                    durationMinutes = durationMinutes,
                    emoji = emoji,
                    habitViewModel = habitViewModel,
                    onSaveSuccess = onSaveSuccess,
                    accentColorHex = selectedColor
                )
            }
        }
    }
}



