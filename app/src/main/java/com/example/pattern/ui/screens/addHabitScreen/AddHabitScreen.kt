package com.example.pattern.ui.screens.addHabitScreen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pattern.ui.components.SectionHeader
import com.example.pattern.ui.screens.addHabitScreen.components.*
import com.example.pattern.ui.screens.settings.PatternTimePickerDialog
import kotlinx.coroutines.launch

enum class AddHabitStep {
    Main, Category, Color, Emoji, Motivation
}

@Composable
fun AddHabitScreen(
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AddHabitScreenContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onTypeChange = viewModel::onTypeChange,
        onEmojiChange = viewModel::onEmojiChange,
        onMotivationChange = viewModel::onMotivationChange,
        onDaysChange = viewModel::onDaysChange,
        onDurationChange = viewModel::onDurationChange,
        onColorChange = viewModel::onColorChange,
        onReminderEnabledChange = viewModel::onReminderEnabledChange,
        onReminderTimeChange = viewModel::onReminderTimeChange,
        onStepChange = viewModel::onStepChange,
        onShowTimePickerChange = viewModel::onShowTimePickerChange,
        onSave = { viewModel.saveNewHabit(onSaveSuccess) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreenContent(
    uiState: AddHabitUiState,
    onNameChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onMotivationChange: (String) -> Unit,
    onDaysChange: (List<java.time.DayOfWeek>) -> Unit,
    onDurationChange: (Int, Int) -> Unit,
    onColorChange: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    onStepChange: (AddHabitStep) -> Unit,
    onShowTimePickerChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.screenTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == AddHabitStep.Main) onBack()
                        else onStepChange(AddHabitStep.Main)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (uiState.currentStep == AddHabitStep.Main) {
                        IconButton(onClick = {
                            if (!uiState.isNameValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a name for your pattern") }
                            } else if (!uiState.isColorValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please select a color") }
                            } else if (!uiState.isDaysValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please select at least one day") }
                            } else {
                                onSave()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Save",
                                tint = if (uiState.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (uiState.currentStep == AddHabitStep.Category || uiState.currentStep == AddHabitStep.Motivation) {
                        IconButton(onClick = { onStepChange(AddHabitStep.Main) }) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Done",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.statusBars
    ) { padding ->
        AnimatedContent(
            targetState = uiState.currentStep,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding),
            label = "ScreenTransition"
        ) { step ->
            when (step) {
                AddHabitStep.Main -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { focusManager.clearFocus() }
                            },
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionHeader("The Basics")
                        
                        HabitNameCard(
                            name = uiState.habitName,
                            onNameChange = onNameChange
                        )

                        EmojiSelector(
                            selectedEmoji = uiState.emoji,
                            onOpen = { onStepChange(AddHabitStep.Emoji) }
                        )

                        ColorSelector(
                            selectedColor = uiState.selectedColor,
                            onOpen = { onStepChange(AddHabitStep.Color) }
                        )

                        SectionHeader("Structure")
                        
                        HabitTypeSelectorCard(
                            selectedType = uiState.habitType,
                            onOpen = { onStepChange(AddHabitStep.Category) }
                        )
                        
                        HabitReminderCard(
                            isEnabled = uiState.reminderEnabled,
                            reminderTime = uiState.reminderTime,
                            onEnabledChange = onReminderEnabledChange,
                            onOpenTimePicker = { onShowTimePickerChange(true) }
                        )

                        SectionHeader("Mindset")

                        MotivationCard(
                            onOpen = { onStepChange(AddHabitStep.Motivation) }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                
                AddHabitStep.Category -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        HabitTypeSelectorModern(
                            selectedType = uiState.habitType,
                            onTypeChange = onTypeChange,
                            selectedDays = uiState.buildHabitDays,
                            onDaysChange = onDaysChange,
                            durationHours = uiState.durationHours,
                            durationMinutes = uiState.durationMinutes,
                            onDurationChange = onDurationChange
                        )
                    }
                }
                
                AddHabitStep.Color -> {
                    ColorPickerView(
                        selectedColor = uiState.selectedColor,
                        onColorSelected = { 
                            onColorChange(it)
                            onStepChange(AddHabitStep.Main)
                        }
                    )
                }
                
                AddHabitStep.Emoji -> {
                    EmojiPickerView(
                        selectedEmoji = uiState.emoji,
                        onEmojiSelected = { 
                            onEmojiChange(it)
                            onStepChange(AddHabitStep.Main)
                        }
                    )
                }
                
                AddHabitStep.Motivation -> {
                    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                        BasicTextField(
                            value = uiState.motivation,
                            onValueChange = onMotivationChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (uiState.motivation.isEmpty()) {
                                    Text(
                                        text = "Add a reason or quote...",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showTimePicker) {
        PatternTimePickerDialog(
            initialTime = uiState.reminderTime,
            onTimeSelected = {
                onReminderTimeChange(it)
                onShowTimePickerChange(false)
            },
            onDismiss = { onShowTimePickerChange(false) }
        )
    }
}
