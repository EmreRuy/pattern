package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.GetHomeHabitsUseCase
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import com.example.pattern.di.DefaultDispatcher
import com.example.pattern.domain.streak.StreakCalculator
import com.example.pattern.ui.mapper.toCardModel
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.TimePeriod
import com.example.pattern.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
Optimization:
 * 1. Total Decoupling: The ViewModel now ONLY reacts to structural changes. 
 *    Ticking timer updates in the DB are completely ignored by the UI state pipeline.
 * 2. Reduced Mapping: Eliminated the redundant raw stream from the combine block.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    habitRepository: HabitRepository,
    dailyLogRepository: DailyLogRepository,
    private val getHomeHabitsUseCase: GetHomeHabitsUseCase,
    private val updateHabitProgressUseCase: UpdateHabitProgressUseCase,
    private val streakCalculator: StreakCalculator,
    private val savedStateHandle: SavedStateHandle,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _selectedDate = savedStateHandle.getStateFlow("selected_date", LocalDate.now())
    private val _todayFlow = MutableStateFlow(LocalDate.now())
    private val modelCache = mutableMapOf<String, HabitCardModel>()

    private val levelInfoFlow = habitRepository.getSettingsStream()
        .map { settings -> ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0) }
        .distinctUntilChanged()
        .onStart { emit(ExperienceUtils.getLevelInfo(0)) }

    private val timePeriodFlow = flow {
        while (true) {
            emit(TimeUtils.getCurrentTimePeriod())
            delay(60_000) // Update every minute
        }
    }.distinctUntilChanged()

    // Optimization: Habit list for the window is now delegated to a specialized UseCase.
    private val habitWindowFlow = _selectedDate.flatMapLatest { date ->
        getHomeHabitsUseCase(date)
    }.map { windowMap ->
        windowMap.mapValues { (_, habits) ->
            habits.map { status ->
                val cacheKey = "${status.habit.hashCode()}_${status.dailyState?.hashCode()}_${status.currentStreak}"
                modelCache.getOrPut(cacheKey) {
                    status.toCardModel()
                }
            }.toImmutableList()
        }.toImmutableMap()
    }

    // Optimization: The calendar window dots are calculated separately and much more simply.
    // We only need to know if SOMETHING was completed on a given date.
    private val calendarDotsFlow = dailyLogRepository.getDailyStatesFromDateStream(
        LocalDate.now().minusDays(14).toString()
    ).map { states ->
        states.filter { it.isCompleted || it.isTaskCompleted }
            .groupBy { it.date }
            .mapValues { true }
    }.distinctUntilChanged()

    val uiState: StateFlow<HomeUiState> = combine(
        combine(_selectedDate, _todayFlow) { date, today -> date to today },
        habitWindowFlow,
        levelInfoFlow,
        timePeriodFlow,
        habitRepository.getAllHabitsStream().map { it.isNotEmpty() }.distinctUntilChanged()
    ) { dateAndToday, window, level, timePeriod, hasHabits ->
        val (date, today) = dateAndToday
        
        HomeUiState.Success(
            selectedDate = date,
            isSelectedDateToday = date == today,
            habits = window[date] ?: persistentListOf(),
            habitsByDate = window,
            hasAnyHabits = hasHabits,
            levelInfo = level,
            timePeriod = timePeriod,
            isLoading = false
        ) as HomeUiState
    }
    .catch { e ->
        emit(HomeUiState.Error(e.message ?: "An unexpected error occurred"))
    }
    .flowOn(defaultDispatcher)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Success(
            levelInfo = ExperienceUtils.getLevelInfo(0),
            timePeriod = TimeUtils.getCurrentTimePeriod(),
            isLoading = true
        )
    )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnResume -> {
                // Assert date validity upon screen re-entry
                refreshDateIfStale()
            }
            is HomeUiEvent.OnDateSelected -> {
                if (_selectedDate.value != event.date) {
                    savedStateHandle["selected_date"] = event.date
                }
            }
            is HomeUiEvent.OnTimerStart -> viewModelScope.launch { updateHabitProgressUseCase.startTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerPause -> viewModelScope.launch { updateHabitProgressUseCase.pauseTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerResume -> viewModelScope.launch { updateHabitProgressUseCase.resumeTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerFinish -> viewModelScope.launch { updateHabitProgressUseCase.finishTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerUnfinish -> viewModelScope.launch { updateHabitProgressUseCase.unfinishTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTaskToggle -> viewModelScope.launch { updateHabitProgressUseCase.toggleTask(event.habitId, event.date, event.completed) }
            is HomeUiEvent.OnTaskIncrement -> viewModelScope.launch { updateHabitProgressUseCase.incrementTask(event.habitId, event.date) }
        }
    }

    private fun refreshDateIfStale() {
        val currentToday = LocalDate.now()
        val oldToday = _todayFlow.value
        
        if (oldToday != currentToday) {
            _todayFlow.value = currentToday
            
            // If the user was viewing "Today" on the old date, automatically 
            // shift them to the new "Today".
            if (_selectedDate.value == oldToday) {
                savedStateHandle["selected_date"] = currentToday
            }
        }
    }
}
