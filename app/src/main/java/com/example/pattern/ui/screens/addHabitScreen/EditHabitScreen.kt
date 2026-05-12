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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.notifications.ReminderManager
import com.example.pattern.ui.components.SectionHeader
import com.example.pattern.ui.screens.addHabitScreen.components.*
import com.example.pattern.ui.screens.settings.PatternTimePickerDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])
    
    private val _habit = MutableStateFlow<Habit?>(null)
    val habit: StateFlow<Habit?> = _habit.asStateFlow()

    init {
        viewModelScope.launch {
            _habit.value = repository.getHabitOnce(habitId)
        }
    }

    fun updateHabit(
        name: String,
        type: String,
        durationHours: Int,
        durationMinutes: Int,
        selectedDays: List<DayOfWeek>,
        emoji: String,
        colorHex: String,
        reminderEnabled: Boolean,
        reminderTime: String?,
        motivation: String?
    ) {
        val currentHabit = _habit.value ?: return
        
        val daysList = DayOfWeek.entries.map { selectedDays.contains(it) }
        
        val updatedHabit = currentHabit.copy(
            name = name.trim(),
            type = when(type) {
                "Grow" -> HabitType.BUILD
                "Drop" -> HabitType.QUIT
                else -> HabitType.TASK
            },
            durationInMinutes = if (type == "Grow") (durationHours * 60) + durationMinutes else null,
            selectedDays = daysList,
            iconCode = emoji,
            accentColorHex = colorHex,
            reminderTime = if (reminderEnabled) reminderTime else null,
            motivation = if (motivation.isNullOrBlank()) null else motivation.trim()
        )
        viewModelScope.launch {
            repository.updateHabit(updatedHabit)
            if (updatedHabit.reminderTime != null) {
                reminderManager.scheduleReminder(updatedHabit)
            } else {
                reminderManager.cancelReminder(updatedHabit.id)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditHabitViewModel = hiltViewModel()
) {
    val habit by viewModel.habit.collectAsState()

    if (habit == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val initialHabit = habit!!
    
    // Initialize states with existing habit data
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
            DayOfWeek.entries.filterIndexed { index, _ -> initialHabit.selectedDays.getOrElse(index) { false } }
        ) 
    }

    var durationHours by remember { mutableIntStateOf((initialHabit.durationInMinutes ?: 0) / 60) }
    var durationMinutes by remember { mutableIntStateOf((initialHabit.durationInMinutes ?: 0) % 60) }
    var selectedColor by remember { mutableStateOf(initialHabit.accentColorHex) }
    var reminderEnabled by remember { mutableStateOf(initialHabit.reminderTime != null) }
    var reminderTime by remember { mutableStateOf(initialHabit.reminderTime ?: "09:00") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showTimePicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val screenTitle = when (currentStep) {
        AddHabitStep.Main -> "EDIT PATTERN"
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
                        IconButton(onClick = {
                            if (habitName.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a habit name") }
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
                                    motivation = motivation
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
                        
                        // We could also reuse the inline editor from AddHabitScreen or keep it simple
                        // For consistency, let's use a similar layout
                        
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
                            onEnabledChange = { reminderEnabled = it },
                            onOpenTimePicker = { showTimePicker = true }
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
