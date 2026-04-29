package com.example.pattern.ui.screens.addHabitScreen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pattern.ui.screens.addHabitScreen.AddHabitViewModel
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.ui.screens.addHabitScreen.components.*
import com.example.pattern.ui.screens.settings.PatternTimePickerDialog
import java.time.DayOfWeek
import kotlinx.coroutines.launch

enum class AddHabitStep {
    Main, Category, Color, Emoji, Motivation
}

@Composable
fun AddHabitContent(
    onClose: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    AddHabitScreen(
        onSaveSuccess = onClose,
        onBack = onClose,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(AddHabitStep.Main) }
    
    var habitName by remember { mutableStateOf("") }
    var habitType by remember { mutableStateOf("Grow") }
    var emoji by remember { mutableStateOf("🔥") }
    var motivation by remember { mutableStateOf("") }
    var buildHabitDays by remember { mutableStateOf(DayOfWeek.entries.toList()) }

    var durationHours by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var selectedColor by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val screenTitle = when (currentStep) {
        AddHabitStep.Main -> "NEW PATTERN"
        AddHabitStep.Category -> "CATEGORY"
        AddHabitStep.Color -> "SELECT COLOR"
        AddHabitStep.Emoji -> "CHOOSE ICON"
        AddHabitStep.Motivation -> "MOTIVATION"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        screenTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == AddHabitStep.Main) onBack()
                        else currentStep = AddHabitStep.Main
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (currentStep == AddHabitStep.Main) {
                        val isNameValid = habitName.isNotBlank()
                        val isColorValid = selectedColor.isNotBlank()
                        val isDaysValid = buildHabitDays.isNotEmpty()
                        val isValid = isNameValid && isColorValid && isDaysValid && emoji.isNotBlank()

                        IconButton(onClick = {
                            if (!isNameValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a name for your pattern") }
                            } else if (!isColorValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please select a color") }
                            } else if (!isDaysValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please select at least one day") }
                            } else {
                                viewModel.saveNewHabit(
                                    name = habitName.trim(),
                                    type = when (habitType) {
                                        "Grow" -> HabitType.BUILD
                                        "Drop" -> HabitType.QUIT
                                        else -> HabitType.TASK
                                    },
                                    durationHours = durationHours,
                                    durationMinutes = durationMinutes,
                                    selectedDays = DayOfWeek.entries.map { buildHabitDays.contains(it) },
                                    iconCode = emoji,
                                    accentColorHex = selectedColor,
                                    reminderTime = if (reminderEnabled) reminderTime else null,
                                    motivation = if (motivation.isBlank()) null else motivation.trim()
                                )
                                onSaveSuccess()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Save",
                                tint = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (currentStep == AddHabitStep.Category || currentStep == AddHabitStep.Motivation) {
                        IconButton(onClick = { currentStep = AddHabitStep.Main }) {
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
            targetState = currentStep,
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
                        
                        // Inline Habit Name Input
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = habitName.trim().take(1).uppercase().ifBlank { "?" }
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Habit Name",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    )
                                    BasicTextField(
                                        value = habitName,
                                        onValueChange = { habitName = it.take(20) },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        decorationBox = { innerTextField ->
                                            if (habitName.isEmpty()) {
                                                Text(
                                                    text = "What's the goal?",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                EmojiSelector(
                                    selectedEmoji = emoji,
                                    onOpen = { currentStep = AddHabitStep.Emoji }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ColorSelector(
                                    selectedColor = selectedColor,
                                    onOpen = { currentStep = AddHabitStep.Color }
                                )
                            }
                        }

                        SectionHeader("Structure")
                        
                        val (categoryIcon, categoryIconColor) = when (habitType) {
                            "Grow" -> Icons.Default.AutoGraph to Color(0xFF22C55E)
                            "Drop" -> Icons.Default.RemoveCircleOutline to Color(0xFFFB7185)
                            else -> Icons.Default.ChangeCircle to Color(0xFF6366F1)
                        }

                        HabitSelectionCard(
                            label = "Category",
                            value = habitType,
                            onClick = { currentStep = AddHabitStep.Category },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(categoryIconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = null,
                                        tint = categoryIconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        )
                        
                        HabitReminderCard(
                            isEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            onEnabledChange = { reminderEnabled = it },
                            onOpenTimePicker = { showTimePicker = true }
                        )

                        SectionHeader("Mindset")

                        MotivationCard(
                            motivation = motivation,
                            onOpen = { currentStep = AddHabitStep.Motivation }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                
                AddHabitStep.Category -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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
                    }
                }
                
                AddHabitStep.Color -> {
                    ColorPickerView(
                        selectedColor = selectedColor,
                        onColorSelected = { 
                            selectedColor = it
                            currentStep = AddHabitStep.Main
                        }
                    )
                }
                
                AddHabitStep.Emoji -> {
                    EmojiPickerView(
                        selectedEmoji = emoji,
                        onEmojiSelected = { 
                            emoji = it
                            currentStep = AddHabitStep.Main
                        }
                    )
                }
                
                AddHabitStep.Motivation -> {
                    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                        BasicTextField(
                            value = motivation,
                            onValueChange = { motivation = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (motivation.isEmpty()) {
                                    Text(
                                        text = "Add a reason or reminder...",
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

    if (showTimePicker) {
        PatternTimePickerDialog(
            initialTime = reminderTime,
            onTimeSelected = {
                reminderTime = it
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
