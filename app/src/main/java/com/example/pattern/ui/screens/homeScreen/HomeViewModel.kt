package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.GetHabitProjectionDataUseCase
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import com.example.pattern.di.DefaultDispatcher
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.ui.screens.homeScreen.mapper.HabitProjectionMapper
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.TimePeriod
import com.example.pattern.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getHabitProjectionDataUseCase: GetHabitProjectionDataUseCase,
    private val updateHabitProgressUseCase: UpdateHabitProgressUseCase,
    private val projectionMapper: HabitProjectionMapper,
    private val savedStateHandle: SavedStateHandle,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _selectedDate = savedStateHandle.getStateFlow("selected_date", LocalDate.now())
    private val _todayFlow = MutableStateFlow(LocalDate.now())

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

    val uiState: StateFlow<HomeUiState> = combine(
        combine(_selectedDate, _todayFlow) { date, today -> date to today },
        getHabitProjectionDataUseCase(),
        levelInfoFlow,
        timePeriodFlow,
        habitRepository.getAllHabitsStream().map { it.isNotEmpty() }.distinctUntilChanged()
    ) { dateAndToday, projectionData, level, timePeriod, hasHabits ->
        val (date, today) = dateAndToday
        
        HomeUiState.Success(
            selectedDate = date,
            isSelectedDateToday = date == today,
            projectionData = projectionData,
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
            selectedDate = _selectedDate.value,
            levelInfo = ExperienceUtils.getLevelInfo(0),
            timePeriod = TimeUtils.getCurrentTimePeriod(),
            isLoading = true
        )
    )

    fun project(date: LocalDate): List<HabitCardModel> {
        val state = uiState.value
        return if (state is HomeUiState.Success && state.projectionData != null) {
            projectionMapper.map(state.projectionData, date)
        } else {
            emptyList()
        }
    }

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
