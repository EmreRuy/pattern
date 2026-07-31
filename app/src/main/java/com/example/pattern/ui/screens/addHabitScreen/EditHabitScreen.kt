package com.example.pattern.ui.screens.addHabitScreen

import android.util.Log
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitEmoji
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.EmojiRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.notifications.ReminderManager
import com.example.pattern.ui.components.PatternEmojiInputDialog
import com.example.pattern.ui.components.PatternTimePickerDialog
import com.example.pattern.ui.components.SectionHeader
import com.example.pattern.ui.screens.addHabitScreen.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val emojiRepository: EmojiRepository,
    private val reminderManager: ReminderManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])
    
    private val _uiState = MutableStateFlow(EditHabitUiState())
    val uiState: StateFlow<EditHabitUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditHabitEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadHabit()
        observeEmojis()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            val habit = repository.getHabitOnce(habitId)
            if (habit != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        habitName = habit.name,
                        habitType = when (habit.type) {
                            HabitType.BUILD -> "Grow"
                            HabitType.QUIT -> "Drop"
                            HabitType.TASK -> "Task"
                        },
                        emoji = habit.iconCode,
                        motivation = habit.motivation ?: "",
                        buildHabitDays = DayOfWeek.entries.filterIndexed { index, _ -> 
                            habit.selectedDays.getOrElse(index) { false } 
                        }.toImmutableList(),
                        durationHours = (habit.durationInMinutes ?: 0) / 60,
                        durationMinutes = (habit.durationInMinutes ?: 0) % 60,
                        taskCount = habit.taskCount ?: 1,
                        selectedColor = habit.accentColorHex,
                        reminderEnabled = habit.reminderTime != null,
                        reminderTime = habit.reminderTime ?: "09:00"
                    )
                }
            }
        }
    }

    private fun observeEmojis() {
        combine(
            emojiRepository.getAllEmojis(),
            emojiRepository.getCategories(),
            _uiState.map { it.emojiSearchQuery }.distinctUntilChanged(),
            _uiState.map { it.selectedEmojiCategory }.distinctUntilChanged()
        ) { allEmojis, categories, query, category ->
            val filtered = allEmojis.filter { emoji ->
                val matchesCategory = (category == "All") || (emoji.category == category)
                val matchesSearch = query.isBlank() || 
                        emoji.value.contains(query, ignoreCase = true) || 
                        emoji.keywords.any { it.contains(query, ignoreCase = true) }
                matchesCategory && matchesSearch
            }.toImmutableList()
            
            _uiState.update { it.copy(
                filteredEmojis = filtered,
                availableEmojiCategories = categories.toImmutableList()
            ) }
        }.launchIn(viewModelScope)
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(habitName = name.take(20)) }
    }

    fun onTypeChange(type: String) {
        _uiState.update { state ->
            val updatedState = state.copy(habitType = type)
            // Principal Requirement: If switching to GROW and duration is 0, default to 1 min
            if (type == "Grow" && updatedState.durationHours == 0 && updatedState.durationMinutes == 0) {
                updatedState.copy(durationMinutes = 1)
            } else {
                updatedState
            }
        }
    }

    fun onDurationChange(hours: Int, minutes: Int) {
        _uiState.update { it.copy(durationHours = hours, durationMinutes = minutes) }
    }

    fun onEmojiSearchQueryChange(query: String) {
        _uiState.update { it.copy(emojiSearchQuery = query) }
    }

    fun onEmojiCategoryChange(category: String) {
        _uiState.update { it.copy(selectedEmojiCategory = category) }
    }

    fun onShowCustomEmojiDialogChange(show: Boolean) {
        _uiState.update { it.copy(showCustomEmojiDialog = show) }
    }
    
    fun onEmojiSelected(emoji: String) {
        _uiState.update { it.copy(emoji = emoji, currentStep = AddHabitStep.Main) }
    }

    fun onColorSelected(color: String) {
        _uiState.update { it.copy(selectedColor = color, currentStep = AddHabitStep.Main) }
    }

    fun onDaysChange(days: List<DayOfWeek>) {
        _uiState.update { it.copy(buildHabitDays = days.toImmutableList()) }
    }

    fun onTaskCountChange(count: Int) {
        _uiState.update { it.copy(taskCount = count) }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun onReminderTimeChange(time: String) {
        _uiState.update { it.copy(reminderTime = time, showTimePicker = false) }
    }

    fun onStepChange(step: AddHabitStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun onMotivationChange(motivation: String) {
        _uiState.update { it.copy(motivation = motivation) }
    }

    fun onShowTimePickerChange(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun saveHabit() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            try {
                val currentHabit = repository.getHabitOnce(habitId) ?: return@launch
                
                val newType = when(state.habitType) {
                    "Grow" -> HabitType.BUILD
                    "Drop" -> HabitType.QUIT
                    else -> HabitType.TASK
                }
                
                // Principal Requirement: Validate non-zero duration for GROW
                var durationInMins = if (state.habitType == "Grow") ((state.durationHours * 60) + state.durationMinutes) else null
                if (newType == HabitType.BUILD) {
                    durationInMins = (durationInMins ?: 0).coerceAtLeast(1)
                }
                
                Log.d("EditHabitSave", "Saving Habit: id=$habitId, category=$newType, durationGoal=$durationInMins, name=${state.habitName}")

                val updatedHabit = currentHabit.copy(
                    name = state.habitName.trim(),
                    type = newType,
                    durationInMinutes = durationInMins,
                    taskCount = if (state.habitType == "Task") state.taskCount else null,
                    selectedDays = DayOfWeek.entries.map { state.buildHabitDays.contains(it) }.toImmutableList(),
                    iconCode = state.emoji,
                    accentColorHex = state.selectedColor,
                    reminderTime = if (state.reminderEnabled) state.reminderTime else null,
                    motivation = if (state.motivation.isBlank()) null else state.motivation.trim()
                )

                repository.withTransaction {
                    repository.updateHabit(updatedHabit)
                    
                    // Principal Requirement: If category changed, reset today's progress to avoid "zombie" state
                    if (currentHabit.type != newType) {
                        val today = LocalDate.now().toString()
                        val todayState = dailyLogRepository.getDailyStateOnce(habitId, today)
                        if (todayState != null) {
                            val resetState = todayState.copy(
                                accumulatedTimeMs = 0L,
                                activeSessionStartMs = null,
                                isCompleted = false,
                                completedCount = 0
                            )
                            dailyLogRepository.upsertDailyState(resetState)
                            
                            // Re-calculate XP if the old state was completed
                            if (todayState.isCompleted) {
                                // Since we reset it, we subtract the XP previously earned for this habit today
                                // Note: calculateHabitXP returns the total XP for the completed state
                                val xpToRemove = com.example.pattern.utils.ExperienceUtils.calculateHabitXP(currentHabit, todayState)
                                dailyLogRepository.addXP(-xpToRemove)
                            }
                        }
                    }
                }
                
                if (updatedHabit.reminderTime != null) {
                    reminderManager.scheduleReminder(updatedHabit)
                } else {
                    reminderManager.cancelReminder(updatedHabit.id)
                }
                _eventFlow.emit(EditHabitEvent.SaveSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(EditHabitEvent.Error(e.message ?: "Failed to update habit"))
            }
        }
    }
}

sealed class EditHabitEvent {
    object SaveSuccess : EditHabitEvent()
    data class Error(val message: String) : EditHabitEvent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is EditHabitEvent.SaveSuccess -> onSaveSuccess()
                is EditHabitEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val screenTitle = remember(uiState.currentStep) {
        when (uiState.currentStep) {
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
                        if (uiState.currentStep == AddHabitStep.Main) onBack()
                        else viewModel.onStepChange(AddHabitStep.Main)
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
                            if (uiState.habitName.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a habit name") }
                            } else if (uiState.buildHabitDays.isEmpty()) {
                                scope.launch { snackbarHostState.showSnackbar("Please select at least one day") }
                            } else {
                                viewModel.saveHabit()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (uiState.currentStep == AddHabitStep.Category || uiState.currentStep == AddHabitStep.Motivation) {
                        IconButton(onClick = { viewModel.onStepChange(AddHabitStep.Main) }) {
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
                            onNameChange = viewModel::onNameChange
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                EmojiSelector(
                                    selectedEmoji = uiState.emoji,
                                    onOpen = { viewModel.onStepChange(AddHabitStep.Emoji) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ColorSelector(
                                    selectedColor = uiState.selectedColor,
                                    onOpen = { viewModel.onStepChange(AddHabitStep.Color) }
                                )
                            }
                        }

                        SectionHeader("Structure")
                        
                        HabitTypeSelectorCard(
                            selectedType = uiState.habitType,
                            onOpen = { viewModel.onStepChange(AddHabitStep.Category) }
                        )
                        
                        HabitReminderCard(
                            isEnabled = uiState.reminderEnabled,
                            reminderTime = uiState.reminderTime,
                            onEnabledChange = viewModel::onReminderEnabledChange,
                            onOpenTimePicker = { viewModel.onShowTimePickerChange(true) }
                        )

                        SectionHeader("Mindset")

                        MotivationCard(
                            onOpen = { viewModel.onStepChange(AddHabitStep.Motivation) }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                
                AddHabitStep.Category -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        HabitTypeSelectorModern(
                            selectedType = uiState.habitType,
                            onTypeChange = viewModel::onTypeChange,
                            selectedDays = uiState.buildHabitDays,
                            onDaysChange = viewModel::onDaysChange,
                            durationHours = uiState.durationHours,
                            durationMinutes = uiState.durationMinutes,
                            onDurationChange = viewModel::onDurationChange,
                            taskCount = uiState.taskCount,
                            onTaskCountChange = viewModel::onTaskCountChange
                        )
                    }
                }
                
                AddHabitStep.Color -> {
                    ColorPickerView(
                        selectedColor = uiState.selectedColor,
                        onColorSelected = viewModel::onColorSelected
                    )
                }
                
                AddHabitStep.Emoji -> {
                    EmojiPickerView(
                        selectedEmoji = uiState.emoji,
                        onEmojiSelected = viewModel::onEmojiSelected,
                        searchQuery = uiState.emojiSearchQuery,
                        onSearchQueryChange = viewModel::onEmojiSearchQueryChange,
                        selectedCategory = uiState.selectedEmojiCategory,
                        onCategorySelected = viewModel::onEmojiCategoryChange,
                        categories = uiState.availableEmojiCategories,
                        emojis = uiState.filteredEmojis,
                        onCustomEmojiClick = { viewModel.onShowCustomEmojiDialogChange(true) }
                    )
                }
                
                AddHabitStep.Motivation -> {
                    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                        BasicTextField(
                            value = uiState.motivation,
                            onValueChange = viewModel::onMotivationChange,
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

    if (uiState.showTimePicker) {
        PatternTimePickerDialog(
            initialTime = uiState.reminderTime,
            onTimeSelected = viewModel::onReminderTimeChange,
            onDismiss = { viewModel.onShowTimePickerChange(false) }
        )
    }

    PatternEmojiInputDialog(
        isVisible = uiState.showCustomEmojiDialog,
        onDismiss = { viewModel.onShowCustomEmojiDialogChange(false) },
        onEmojiSubmitted = { 
            viewModel.onEmojiSelected(it)
            viewModel.onShowCustomEmojiDialogChange(false)
        }
    )
}
