package com.example.pattern.ui.screens.addHabitScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun AddHabitSheetContent(
    onClose: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    AddHabitScreen(
        onSaveSuccess = onClose,
        habitViewModel = viewModel
    )
}
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

    var durationHours by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var selectedColor by remember { mutableStateOf("#77DD77") }
    var showEmojiSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showNameSheet by remember { mutableStateOf(false) }
    var showHabitTypeSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CardHeader("Create your habit")
                SectionHeader("Info")
                HabitNameCard(
                    habitName = habitName,
                    onOpen = { showNameSheet = true }
                )
                SectionHeader("Appearance")
                ColorSelector(
                    selectedColor = selectedColor,
                    onOpen = { showColorSheet = true }
                )
                EmojiSelector(
                    selectedEmoji = emoji,
                    onOpen = { showEmojiSheet = true }
                )
                SectionHeader("Pattern")
                HabitTypeSelectorCard(
                    selectedType = habitType,
                    onOpen = { showHabitTypeSheet = true }
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

                Spacer(modifier = Modifier.height(16.dp))
            }
            HandleHabitNameSheet(
                showSheet = showNameSheet,
                habitName = habitName,
                onNameChange = { habitName = it },
                onDismiss = { showNameSheet = false }
            )
            HandleColorSheet(
                showColorSheet = showColorSheet,
                selectedColor = selectedColor,
                onColorChange = { selectedColor = it },
                onDismiss = { showColorSheet = false }
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
            HandleEmojiSheet(
                showEmojiSheet = showEmojiSheet,
                selectedEmoji = emoji,
                onEmojiChange = { emoji = it },
                onDismiss = { showEmojiSheet = false }
            )
        }
    }
}
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}
@Composable
fun CardHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        )
    }
}



