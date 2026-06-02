package com.example.pattern.ui.screens.addHabitScreen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.ui.components.PatternTimePickerDialog
import com.example.pattern.ui.components.SectionHeader
import com.example.pattern.ui.screens.addHabitScreen.components.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditHabitViewModel = hiltViewModel()
) {
    val habit by viewModel.habit.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    if (habit == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val initialHabit = habit!!
    
    var currentStep by remember { mutableStateOf(AddHabitStep.Main) }
    var habitName by remember { mutableStateOf(initialHabit.name) }
    var habitType by remember { 
        mutableStateOf(
            when(initialHabit.type) {
                HabitType.BUILD -> "Grow"
                HabitType.QUIT -> "Drop"
                HabitType.TASK -> "Task"
            }
        ) 
    }
    var emoji by remember { mutableStateOf(initialHabit.iconCode) }
    var motivation by remember { mutableStateOf(initialHabit.motivation ?: "") }
    var buildHabitDays by remember { 
        mutableStateOf(
            DayOfWeek.entries.filterIndexed { index, _ -> initialHabit.selectedDays.getOrElse(index) { false } }.toImmutableList()
        ) 
    }

    var durationHours by remember { mutableIntStateOf((initialHabit.durationInMinutes ?: 0) / 60) }
    var durationMinutes by remember { mutableIntStateOf((initialHabit.durationInMinutes ?: 0) % 60) }
    var taskCount by remember { mutableIntStateOf(initialHabit.taskCount ?: 1) }
    var selectedColor by remember { mutableStateOf(initialHabit.accentColorHex) }
    var reminderEnabled by remember { mutableStateOf(initialHabit.reminderTime != null) }
    var reminderTime by remember { mutableStateOf(initialHabit.reminderTime ?: "09:00") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Sync switch state when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (isAllowed && showPermissionDialog) {
                    reminderEnabled = true
                    showPermissionDialog = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val screenTitle = remember(currentStep) {
        when (currentStep) {
            AddHabitStep.Main -> "EDIT PATTERN"
            AddHabitStep.Category -> "CATEGORY"
            AddHabitStep.Color -> "SELECT COLOR"
            AddHabitStep.Emoji -> "CHOOSE ICON"
            AddHabitStep.Motivation -> "MOTIVATION"
        }
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
                        IconButton(onClick = {
                            if (habitName.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a habit name") }
                            } else if (buildHabitDays.isEmpty()) {
                                scope.launch { snackbarHostState.showSnackbar("Please select at least one day") }
                            } else {
                                viewModel.updateHabit(
                                    name = habitName,
                                    type = habitType,
                                    durationHours = durationHours,
                                    durationMinutes = durationMinutes,
                                    selectedDays = buildHabitDays,
                                    emoji = emoji,
                                    colorHex = selectedColor,
                                    reminderEnabled = reminderEnabled,
                                    reminderTime = reminderTime,
                                    motivation = motivation,
                                    taskCount = taskCount
                                )
                                onSaveSuccess()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary,
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                            
                            HabitNameCard(
                                name = habitName,
                                onNameChange = { habitName = it }
                            )
                            
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
                            
                            HabitTypeSelectorCard(
                                selectedType = habitType,
                                onOpen = { currentStep = AddHabitStep.Category }
                            )
                            
                            HabitReminderCard(
                                isEnabled = reminderEnabled,
                                reminderTime = reminderTime,
                                onEnabledChange = { enabled ->
                                    if (enabled) {
                                        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                                            reminderEnabled = true
                                        } else {
                                            showPermissionDialog = true
                                        }
                                    } else {
                                        reminderEnabled = false
                                    }
                                },
                                onOpenTimePicker = {
                                    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                                        showTimePicker = true
                                    } else {
                                        showPermissionDialog = true
                                    }
                                }
                            )

                            SectionHeader("Mindset")

                            MotivationCard(
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
                                onDaysChange = { buildHabitDays = it.toImmutableList() },
                                durationHours = durationHours,
                                durationMinutes = durationMinutes,
                                onDurationChange = { h, m ->
                                    durationHours = h
                                    durationMinutes = m
                                },
                                taskCount = taskCount,
                                onTaskCountChange = { taskCount = it }
                            )
                        }
                    }
                    
                    AddHabitStep.Color -> {
                        ColorPickerView(
                            selectedColor = selectedColor,
                            isPremium = isPremium,
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

            // Notification Permission Dialog at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                NotificationPermissionDialog(
                    isVisible = showPermissionDialog,
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    onDismiss = { showPermissionDialog = false }
                )
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
