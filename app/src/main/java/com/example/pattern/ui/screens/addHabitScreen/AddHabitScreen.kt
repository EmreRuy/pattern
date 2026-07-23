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
import com.example.pattern.ui.components.PatternTimePickerDialog
import com.example.pattern.ui.components.SectionHeader
import com.example.pattern.ui.screens.addHabitScreen.components.*
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentShowPermissionDialog by rememberUpdatedState(uiState.showPermissionDialog)

    // Lead Expert Implementation: Auto-sync switch state when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (isAllowed && currentShowPermissionDialog) {
                    viewModel.onReminderEnabledChange(true)
                    viewModel.onShowPermissionDialogChange(false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Derived states for optimization: Calculate these outside the main content to minimize its recomposition if possible
    val screenTitle = remember(uiState.currentStep) { uiState.screenTitle }
    val isSaveEnabled = remember(uiState.isValid) { uiState.isValid }

    AddHabitScreenContent(
        currentStep = uiState.currentStep,
        habitName = uiState.habitName,
        habitType = uiState.habitType,
        emoji = uiState.emoji,
        selectedColor = uiState.selectedColor,
        motivation = uiState.motivation,
        reminderEnabled = uiState.reminderEnabled,
        reminderTime = uiState.reminderTime,
        buildHabitDays = uiState.buildHabitDays,
        durationHours = uiState.durationHours,
        durationMinutes = uiState.durationMinutes,
        taskCount = uiState.taskCount,
        emojiSearchQuery = uiState.emojiSearchQuery,
        selectedEmojiCategory = uiState.selectedEmojiCategory,
        availableEmojiCategories = uiState.availableEmojiCategories,
        filteredEmojis = uiState.filteredEmojis,
        showPermissionDialog = uiState.showPermissionDialog,
        showTimePicker = uiState.showTimePicker,
        screenTitle = screenTitle,
        isSaveEnabled = isSaveEnabled,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::onNameChange,
        onTypeChange = viewModel::onTypeChange,
        onEmojiChange = viewModel::onEmojiChange,
        onMotivationChange = viewModel::onMotivationChange,
        onDaysChange = viewModel::onDaysChange,
        onDurationChange = viewModel::onDurationChange,
        onTaskCountChange = viewModel::onTaskCountChange,
        onColorChange = viewModel::onColorChange,
        onEmojiSearchQueryChange = viewModel::onEmojiSearchQueryChange,
        onEmojiCategoryChange = viewModel::onEmojiCategoryChange,
        onReminderEnabledChange = { enabled ->
            if (enabled) {
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    viewModel.onReminderEnabledChange(true)
                } else {
                    viewModel.onShowPermissionDialogChange(true)
                }
            } else {
                viewModel.onReminderEnabledChange(false)
            }
        },
        onReminderTimeChange = viewModel::onReminderTimeChange,
        onStepChange = viewModel::onStepChange,
        onShowTimePickerChange = viewModel::onShowTimePickerChange,
        onShowPermissionDialogChange = viewModel::onShowPermissionDialogChange,
        onOpenTimePickerRequest = {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                viewModel.onShowTimePickerChange(true)
            } else {
                viewModel.onShowPermissionDialogChange(true)
            }
        },
        onOpenSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onSaveAttempt = {
            if (!uiState.isNameValid) {
                scope.launch { snackbarHostState.showSnackbar("Please enter a name for your pattern") }
            } else if (!uiState.isColorValid) {
                scope.launch { snackbarHostState.showSnackbar("Please select a color") }
            } else if (!uiState.isDaysValid) {
                scope.launch { snackbarHostState.showSnackbar("Please select at least one day") }
            } else {
                viewModel.saveNewHabit(onSaveSuccess)
            }
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreenContent(
    currentStep: AddHabitStep,
    habitName: String,
    habitType: String,
    emoji: String,
    selectedColor: String,
    motivation: String,
    reminderEnabled: Boolean,
    reminderTime: String,
    buildHabitDays: kotlinx.collections.immutable.ImmutableList<java.time.DayOfWeek>,
    durationHours: Int,
    durationMinutes: Int,
    taskCount: Int,
    emojiSearchQuery: String,
    selectedEmojiCategory: String,
    availableEmojiCategories: kotlinx.collections.immutable.ImmutableList<String>,
    filteredEmojis: kotlinx.collections.immutable.ImmutableList<com.example.pattern.domain.model.HabitEmoji>,
    showPermissionDialog: Boolean,
    showTimePicker: Boolean,
    screenTitle: String,
    isSaveEnabled: Boolean,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onMotivationChange: (String) -> Unit,
    onDaysChange: (List<java.time.DayOfWeek>) -> Unit,
    onDurationChange: (Int, Int) -> Unit,
    onTaskCountChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onEmojiSearchQueryChange: (String) -> Unit,
    onEmojiCategoryChange: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    onStepChange: (AddHabitStep) -> Unit,
    onShowTimePickerChange: (Boolean) -> Unit,
    onShowPermissionDialogChange: (Boolean) -> Unit,
    onOpenTimePickerRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaveAttempt: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            AddHabitTopBar(
                screenTitle = screenTitle,
                currentStep = currentStep,
                isSaveEnabled = isSaveEnabled,
                onBack = onBack,
                onStepChange = onStepChange,
                onSaveAttempt = onSaveAttempt
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
                        AddHabitMainStep(
                            habitName = habitName,
                            emoji = emoji,
                            selectedColor = selectedColor,
                            habitType = habitType,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            onNameChange = onNameChange,
                            onStepChange = onStepChange,
                            onReminderEnabledChange = onReminderEnabledChange,
                            onOpenTimePickerRequest = onOpenTimePickerRequest,
                            focusManager = focusManager
                        )
                    }
                    
                    AddHabitStep.Category -> {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            HabitTypeSelectorModern(
                                selectedType = habitType,
                                onTypeChange = onTypeChange,
                                selectedDays = buildHabitDays,
                                onDaysChange = onDaysChange,
                                durationHours = durationHours,
                                durationMinutes = durationMinutes,
                                onDurationChange = onDurationChange,
                                taskCount = taskCount,
                                onTaskCountChange = onTaskCountChange
                            )
                        }
                    }
                    
                    AddHabitStep.Color -> {
                        ColorPickerView(
                            selectedColor = selectedColor,
                            onColorSelected = { 
                                onColorChange(it)
                                onStepChange(AddHabitStep.Main)
                            }
                        )
                    }
                    
                    AddHabitStep.Emoji -> {
                        EmojiPickerView(
                            selectedEmoji = emoji,
                            onEmojiSelected = { 
                                onEmojiChange(it)
                                onStepChange(AddHabitStep.Main)
                            },
                            searchQuery = emojiSearchQuery,
                            onSearchQueryChange = onEmojiSearchQueryChange,
                            selectedCategory = selectedEmojiCategory,
                            onCategorySelected = onEmojiCategoryChange,
                            categories = availableEmojiCategories,
                            emojis = filteredEmojis
                        )
                    }
                    
                    AddHabitStep.Motivation -> {
                        AddHabitMotivationStep(
                            motivation = motivation,
                            onMotivationChange = onMotivationChange
                        )
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
                    onOpenSettings = onOpenSettings,
                    onDismiss = { onShowPermissionDialogChange(false) }
                )
            }
        }
    }

    if (showTimePicker) {
        PatternTimePickerDialog(
            initialTime = reminderTime,
            onTimeSelected = {
                onReminderTimeChange(it)
                onShowTimePickerChange(false)
            },
            onDismiss = { onShowTimePickerChange(false) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHabitTopBar(
    screenTitle: String,
    currentStep: AddHabitStep,
    isSaveEnabled: Boolean,
    onBack: () -> Unit,
    onStepChange: (AddHabitStep) -> Unit,
    onSaveAttempt: () -> Unit
) {
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
            if (currentStep == AddHabitStep.Main) {
                IconButton(onClick = onSaveAttempt) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Save",
                        tint = if (isSaveEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (currentStep == AddHabitStep.Category || currentStep == AddHabitStep.Motivation) {
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
}

@Composable
private fun AddHabitMainStep(
    habitName: String,
    emoji: String,
    selectedColor: String,
    habitType: String,
    reminderEnabled: Boolean,
    reminderTime: String,
    onNameChange: (String) -> Unit,
    onStepChange: (AddHabitStep) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onOpenTimePickerRequest: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
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
            onNameChange = onNameChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                EmojiSelector(
                    selectedEmoji = emoji,
                    onOpen = { onStepChange(AddHabitStep.Emoji) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ColorSelector(
                    selectedColor = selectedColor,
                    onOpen = { onStepChange(AddHabitStep.Color) }
                )
            }
        }

        SectionHeader("Structure")
        
        HabitTypeSelectorCard(
            selectedType = habitType,
            onOpen = { onStepChange(AddHabitStep.Category) }
        )
        
        HabitReminderCard(
            isEnabled = reminderEnabled,
            reminderTime = reminderTime,
            onEnabledChange = onReminderEnabledChange,
            onOpenTimePicker = onOpenTimePickerRequest
        )

        SectionHeader("Mindset")

        MotivationCard(
            onOpen = { onStepChange(AddHabitStep.Motivation) }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun AddHabitMotivationStep(
    motivation: String,
    onMotivationChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        BasicTextField(
            value = motivation,
            onValueChange = onMotivationChange,
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
