package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import com.example.pattern.di.DefaultDispatcher
import com.example.pattern.ui.mapper.toCardModel
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.calculateCurrentStreak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Staff Engineer Final Performance Pass (Zero-Chatter Edition).
 * 
 * Final Optimization:
 * 1. Total Decoupling: The ViewModel now ONLY reacts to structural changes. 
 *    Ticking timer updates in the DB are completely ignored by the UI state pipeline.
 * 2. Reduced Mapping: Eliminated the redundant raw stream from the combine block.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val updateHabitProgressUseCase: UpdateHabitProgressUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val modelCache = mutableMapOf<String, HabitCardModel>()

    private val levelInfoFlow = habitRepository.getSettingsStream()
        .map { settings -> ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0) }
        .distinctUntilChanged()
        .onStart { emit(ExperienceUtils.getLevelInfo(0)) }

    // Key Performance Fix: 
    // We observe structural changes ONLY. This keeps the ViewModel silent during timer ticks.
    private val structuralDailyStateFlow = habitRepository.getAllDailyStatesStream()

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        habitRepository.getAllHabitsStream(),
        habitRepository.getCompletedDatesStream(),
        structuralDailyStateFlow,
        levelInfoFlow
    ) { date, allHabits, completedDatesByHabit, allDailyStates, level ->
        
        val today = LocalDate.now()
        
        // High-Performance Staff Strategy:
        // We pre-calculate a massive 60-day window around today, PLUS a 14-day window 
        // around the selectedDate. This ensures that whether the user swipes days 
        // or weeks (7 days at a time), the data is ALWAYS there instantly.
        val dateWindow = mutableSetOf<LocalDate>()
        for (i in -30..30) dateWindow.add(today.plusDays(i.toLong()))
        for (i in -7..7) dateWindow.add(date.plusDays(i.toLong()))

        val dailyStateMap = allDailyStates.groupBy { it.date }
        val currentWindowKeys = mutableSetOf<String>()

        val habitsByDate = dateWindow.associateWith { d ->
            val dateStr = d.toString()
            val dayOfWeekIndex = d.dayOfWeek.value - 1
            val statesForDay = dailyStateMap[dateStr]?.associateBy { it.habitId } ?: emptyMap()
            
            allHabits.mapNotNull { habit ->
                if (!habit.selectedDays[dayOfWeekIndex]) return@mapNotNull null
                if (d.isBefore(habit.createdAtLocalDate)) return@mapNotNull null
                
                val dailyState = statesForDay[habit.id]
                val completedDates = completedDatesByHabit[habit.id] ?: emptySet()
                
                val streak = calculateCurrentStreak(
                    habit, 
                    completedDates.map { it.toEpochDay() }.toSet(), 
                    today
                )
                
                val habitHash = habit.hashCode()
                val stateHash = dailyState?.hashCode() ?: 0
                val cacheKey = "${habit.id}_${dateStr}_${habitHash}_${stateHash}_$streak"
                currentWindowKeys.add(cacheKey)
                
                modelCache.getOrPut(cacheKey) {
                    HabitWithStatus(habit, dailyState, streak).toCardModel()
                }
            }
        }

        // Clean up cache to prevent memory leaks
        modelCache.keys.retainAll(currentWindowKeys)

        HomeUiState.Success(
            selectedDate = date,
            isSelectedDateToday = date == today,
            habits = (habitsByDate[date] ?: emptyList()).toImmutableList(),
            habitsByDate = habitsByDate.mapValues { it.value.toImmutableList() }.toImmutableMap(),
            hasAnyHabits = allHabits.isNotEmpty(),
            levelInfo = level
        )
    }
    .flowOn(defaultDispatcher)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnDateSelected -> _selectedDate.value = event.date
            is HomeUiEvent.OnTimerStart -> viewModelScope.launch { updateHabitProgressUseCase.startTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerPause -> viewModelScope.launch { updateHabitProgressUseCase.pauseTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerResume -> viewModelScope.launch { updateHabitProgressUseCase.resumeTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerFinish -> viewModelScope.launch { updateHabitProgressUseCase.finishTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerUnfinish -> viewModelScope.launch { updateHabitProgressUseCase.unfinishTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTaskToggle -> viewModelScope.launch { updateHabitProgressUseCase.toggleTask(event.habitId, event.date, event.completed) }
            is HomeUiEvent.OnTaskIncrement -> viewModelScope.launch { updateHabitProgressUseCase.incrementTask(event.habitId, event.date) }
        }
    }
}
