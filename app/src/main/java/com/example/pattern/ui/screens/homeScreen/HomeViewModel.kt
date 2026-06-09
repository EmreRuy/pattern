package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import com.example.pattern.di.DefaultDispatcher
import com.example.pattern.domain.util.DataResult
import com.example.pattern.domain.util.mapResult
import com.example.pattern.ui.mapper.toCardModel
import com.example.pattern.ui.model.HabitCardModel
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.calculateCurrentStreak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private data class HomeStructuralData(
    val habits: List<com.example.pattern.domain.model.Habit>,
    val streaks: Map<Int, Int>,
    val statesByDate: Map<String, List<com.example.pattern.domain.model.HabitDailyState>>
)

private data class HabitModelKey(
    val habitId: Int,
    val habitHash: Int,
    val stateHash: Int,
    val streak: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    private val updateHabitProgressUseCase: UpdateHabitProgressUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _selectedDate = savedStateHandle.getStateFlow("selected_date", LocalDate.now())
    private val modelCache = java.util.concurrent.ConcurrentHashMap<HabitModelKey, HabitCardModel>(500)

    val levelInfo: StateFlow<DataResult<com.example.pattern.domain.model.LevelInfo>> = habitRepository.getSettingsStream()
        .mapResult { settings -> ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0) }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataResult.Loading)

    private val dailyStatesWindowFlow = _selectedDate
        .flatMapLatest { selected ->
            val startDate = selected.minusDays(14).toString()
            habitRepository.getDailyStatesFromDateStream(startDate)
        }.distinctUntilChanged()

    private val habitDataFlow = combine(
        habitRepository.getAllHabitsStream(),
        habitRepository.getCompletedDatesStream(),
        dailyStatesWindowFlow
    ) { habitsRes, completedRes, statesRes ->
        if (habitsRes is DataResult.Error) return@combine DataResult.Error(habitsRes.exception)
        if (completedRes is DataResult.Error) return@combine DataResult.Error(completedRes.exception)
        if (statesRes is DataResult.Error) return@combine DataResult.Error(statesRes.exception)

        if (habitsRes is DataResult.Loading || completedRes is DataResult.Loading || statesRes is DataResult.Loading) {
            return@combine DataResult.Loading
        }

        if (habitsRes is DataResult.Success && completedRes is DataResult.Success && statesRes is DataResult.Success) {
            val habits = habitsRes.data
            val completed = completedRes.data
            val states = statesRes.data

            val today = LocalDate.now()
            val completedEpochsByHabit = completed.mapValues { (_, dates) ->
                val set = java.util.HashSet<Long>(dates.size)
                for (d in dates) set.add(d.toEpochDay())
                set
            }
            
            val streaks = habits.associate { 
                it.id to calculateCurrentStreak(it, completedEpochsByHabit[it.id] ?: emptySet(), today) 
            }
            
            val statesByDate = states.groupBy { it.date }
            
            DataResult.Success(HomeStructuralData(habits, streaks, statesByDate))
        } else {
            DataResult.Loading
        }
    }.distinctUntilChanged().flowOn(defaultDispatcher)

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        habitDataFlow,
        levelInfo
    ) { date, dataResult, levelRes ->
        when (dataResult) {
            is DataResult.Loading -> HomeUiState.Loading
            is DataResult.Error -> HomeUiState.Error(dataResult.exception.message ?: "Sync error")
            is DataResult.Success -> {
                val data = dataResult.data
                val level = (levelRes as? DataResult.Success)?.data ?: com.example.pattern.domain.model.LevelInfo(0, "", 0, 100, 0f)
                val today = LocalDate.now()
                
                val dateWindow = calculateTightDateWindow(today, date)
                val habitsByDate = java.util.HashMap<LocalDate, ImmutableList<HabitCardModel>>(dateWindow.size)
                val activeKeys = java.util.HashSet<HabitModelKey>(dateWindow.size * data.habits.size)

                for (d in dateWindow) {
                    val dateStr = d.toString()
                    val dayOfWeekIndex = d.dayOfWeek.value - 1
                    val statesForDay = data.statesByDate[dateStr]
                    
                    val mappedHabits = ArrayList<HabitCardModel>(data.habits.size)
                    for (habit in data.habits) {
                        if (!habit.selectedDays[dayOfWeekIndex]) continue
                        if (d.isBefore(habit.createdAtLocalDate)) continue
                        
                        val dailyState = statesForDay?.find { it.habitId == habit.id }
                        val streak = data.streaks[habit.id] ?: 0
                        
                        val modelKey = HabitModelKey(
                            habitId = habit.id,
                            habitHash = habit.hashCode(),
                            stateHash = dailyState?.hashCode() ?: 0,
                            streak = streak
                        )
                        
                        activeKeys.add(modelKey)
                        mappedHabits.add(modelCache.getOrPut(modelKey) {
                            HabitWithStatus(habit, dailyState, streak).toCardModel()
                        })
                    }
                    habitsByDate[d] = mappedHabits.toImmutableList()
                }

                if (modelCache.size > 1000) modelCache.keys.retainAll(activeKeys)

                HomeUiState.Success(
                    selectedDate = date,
                    isSelectedDateToday = date == today,
                    habits = habitsByDate[date] ?: persistentListOf(),
                    habitsByDate = habitsByDate.toImmutableMap(),
                    hasAnyHabits = data.habits.isNotEmpty(),
                    levelInfo = level
                )
            }
        }
    }
    .distinctUntilChanged { old, new ->
        if (old is HomeUiState.Success && new is HomeUiState.Success) {
            old.selectedDate == new.selectedDate && 
            old.habitsByDate == new.habitsByDate &&
            old.levelInfo == new.levelInfo
        } else {
            old == new
        }
    }
    .flowOn(defaultDispatcher)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = HomeUiState.Loading
    )

    private fun calculateTightDateWindow(today: LocalDate, selectedDate: LocalDate): Set<LocalDate> {
        val window = java.util.LinkedHashSet<LocalDate>(45)
        // Optimization 4: Align window with Pager's beyondViewportPageCount (7).
        // This ensures data is pre-mapped and ready the moment the Pager requests it.
        for (i in -7..7) window.add(today.plusDays(i.toLong()))
        for (i in -7..7) window.add(selectedDate.plusDays(i.toLong()))
        return window
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnRetry -> {
                val current = _selectedDate.value
                savedStateHandle["selected_date"] = current
            }
            is HomeUiEvent.OnDateSelected -> {
                if (_selectedDate.value != event.date) {
                    savedStateHandle["selected_date"] = event.date
                }
            }
            is HomeUiEvent.OnTimerStart -> launchUpdate { updateHabitProgressUseCase.startTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerPause -> launchUpdate { updateHabitProgressUseCase.pauseTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerResume -> launchUpdate { updateHabitProgressUseCase.resumeTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerFinish -> launchUpdate { updateHabitProgressUseCase.finishTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTimerUnfinish -> launchUpdate { updateHabitProgressUseCase.unfinishTimer(event.habitId, event.date) }
            is HomeUiEvent.OnTaskToggle -> launchUpdate { updateHabitProgressUseCase.toggleTask(event.habitId, event.date, event.completed) }
            is HomeUiEvent.OnTaskIncrement -> launchUpdate { updateHabitProgressUseCase.incrementTask(event.habitId, event.date) }
        }
    }

    private fun launchUpdate(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
