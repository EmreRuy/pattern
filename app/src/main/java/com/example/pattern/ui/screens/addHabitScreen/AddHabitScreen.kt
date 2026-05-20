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

    // Lead Expert Implementation: Auto-sync switch state when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (isAllowed && uiState.showPermissionDialog) {
                    viewModel.onReminderEnabledChange(true)
                    viewModel.onShowPermissionDialogChange(false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AddHabitScreenContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onTypeChange = viewModel::onTypeChange,
        onEmojiChange = viewModel::onEmojiChange,
        onMotivationChange = viewModel::onMotivationChange,
        onDaysChange = viewModel::onDaysChange,
        onDurationChange = viewModel::onDurationChange,
        onColorChange = viewModel::onColorChange,
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
    onShowPermissionDialogChange: (Boolean) -> Unit,
    onOpenTimePickerRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Optimization: Use derivedStateOf to stabilize state and prevent unnecessary TopAppBar recompositions
    val currentUiState by rememberUpdatedState(uiState)
    val screenTitle by remember { derivedStateOf { currentUiState.screenTitle } }
    val isSaveEnabled by remember { derivedStateOf { currentUiState.isValid } }
    val isNameValid by remember { derivedStateOf { currentUiState.isNameValid } }
    val isColorValid by remember { derivedStateOf { currentUiState.isColorValid } }
    val isDaysValid by remember { derivedStateOf { currentUiState.isDaysValid } }
    val currentStep by remember { derivedStateOf { currentUiState.currentStep } }

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
                        IconButton(onClick = {
                            if (!isNameValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a name for your pattern") }
                            } else if (!isColorValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please select a color") }
                            } else if (!isDaysValid) {
                                scope.launch { snackbarHostState.showSnackbar("Please select at least one day") }
                            } else {
                                onSave()
                            }
                        }) {
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
                                name = uiState.habitName,
                                onNameChange = onNameChange
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    EmojiSelector(
                                        selectedEmoji = uiState.emoji,
                                        onOpen = { onStepChange(AddHabitStep.Emoji) }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ColorSelector(
                                        selectedColor = uiState.selectedColor,
                                        onOpen = { onStepChange(AddHabitStep.Color) }
                                    )
                                }
                            }

                            SectionHeader("Structure")
                            
                            HabitTypeSelectorCard(
                                selectedType = uiState.habitType,
                                onOpen = { onStepChange(AddHabitStep.Category) }
                            )
                            
                            HabitReminderCard(
                                isEnabled = uiState.reminderEnabled,
                                reminderTime = uiState.reminderTime,
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

            // Notification Permission Dialog at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                NotificationPermissionDialog(
                    isVisible = uiState.showPermissionDialog,
                    onOpenSettings = onOpenSettings,
                    onDismiss = { onShowPermissionDialogChange(false) }
                )
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
