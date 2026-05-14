package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.HabitWithStatus
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import com.example.pattern.ui.mapper.toCardModel
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.calculateCurrentStreak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val updateHabitProgressUseCase: UpdateHabitProgressUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        habitRepository.getSettingsStream().distinctUntilChanged(),
        habitRepository.getAllHabitsStream().distinctUntilChanged(),
        habitRepository.getCompletedDatesStream().distinctUntilChanged()
    ) { date, settings, habits, completedDatesByHabit ->
        val dateWindow = listOf(date.minusDays(1), date, date.plusDays(1))
        
        // High-Performance Optimization: 
        // We only fetch full DailyState objects for the 3-day visible window.
        // We use a lightweight ID->Dates map for streak calculations.
        combine(
            dateWindow.map { d ->
                habitRepository.getDailyStatesForDate(d.toString()).map { dailyStates ->
                    val dayOfWeekIndex = d.dayOfWeek.value - 1
                    val dateStatesMap = dailyStates.associateBy { it.habitId }
                    
                    val processedHabits = habits.mapNotNull { habit ->
                        val creationDate = Instant.ofEpochMilli(habit.createdAt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        
                        val wasCreated = !d.isBefore(creationDate)
                        val isScheduled = habit.selectedDays.getOrNull(dayOfWeekIndex) == true
                        
                        if (wasCreated && isScheduled) {
                            val completedDates = completedDatesByHabit[habit.id] ?: emptySet()
                            val currentStreak = calculateCurrentStreak(habit, completedDates, LocalDate.now())
                            
                            HabitWithStatus(
                                habit = habit,
                                dailyState = dateStatesMap[habit.id],
                                currentStreak = currentStreak
                            ).toCardModel()
                        } else null
                    }
                    d to processedHabits
                }
            }
        ) { results ->
            val habitsMap = results.toMap()
            HomeUiState.Success(
                selectedDate = date,
                isSelectedDateToday = date == LocalDate.now(),
                habits = habitsMap[date] ?: emptyList(),
                habitsByDate = habitsMap,
                hasAnyHabits = habits.isNotEmpty(),
                levelInfo = ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0)
            ) as HomeUiState
        }
    }.flatMapLatest { it }
        .flowOn(Dispatchers.Default)
        .catch { e -> emit(HomeUiState.Error(e.message ?: "Unknown Error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnDateSelected -> _selectedDate.value = event.date
            is HomeUiEvent.OnTimerStart -> viewModelScope.launch {
                updateHabitProgressUseCase.startTimer(event.habitId, event.date)
            }
            is HomeUiEvent.OnTimerPause -> viewModelScope.launch {
                updateHabitProgressUseCase.pauseTimer(event.habitId, event.date)
            }
            is HomeUiEvent.OnTimerResume -> viewModelScope.launch {
                updateHabitProgressUseCase.resumeTimer(event.habitId, event.date)
            }
            is HomeUiEvent.OnTimerFinish -> viewModelScope.launch {
                updateHabitProgressUseCase.finishTimer(event.habitId, event.date)
            }
            is HomeUiEvent.OnTimerUnfinish -> viewModelScope.launch {
                updateHabitProgressUseCase.unfinishTimer(event.habitId, event.date)
            }
            is HomeUiEvent.OnTaskToggle -> viewModelScope.launch {
                updateHabitProgressUseCase.toggleTask(event.habitId, event.date, event.completed)
            }
        }
    }
}
