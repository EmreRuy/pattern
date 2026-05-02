package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.domain.usecase.GetHomeHabitsUseCase
import com.example.pattern.domain.usecase.UpdateHabitProgressUseCase
import com.example.pattern.utils.ExperienceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getHomeHabitsUseCase: GetHomeHabitsUseCase,
    private val updateHabitProgressUseCase: UpdateHabitProgressUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _explodeConfetti = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        _explodeConfetti,
        habitRepository.getSettingsStream().distinctUntilChanged(),
        habitRepository.getAllHabitsStream().map { it.isNotEmpty() }.distinctUntilChanged()
    ) { date, explode, settings, hasAnyHabits ->
        val dateWindow = listOf(date.minusDays(1), date, date.plusDays(1))
        
        combine(
            dateWindow.map { d ->
                getHomeHabitsUseCase(d).map { habits -> d to habits }
            }
        ) { results ->
            val habitsMap = results.toMap()
            HomeUiState.Success(
                selectedDate = date,
                isSelectedDateToday = date == LocalDate.now(),
                habits = habitsMap[date] ?: emptyList(),
                habitsByDate = habitsMap,
                hasAnyHabits = hasAnyHabits,
                levelInfo = ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0),
                explodeConfetti = explode
            )
        }
    }.flatMapLatest { it }
        .map<HomeUiState, HomeUiState> { it }
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
                triggerConfetti()
            }
            is HomeUiEvent.OnTimerUnfinish -> viewModelScope.launch {
                updateHabitProgressUseCase.unfinishTimer(event.habitId, event.date)
            }
            is HomeUiEvent.OnTaskToggle -> viewModelScope.launch {
                updateHabitProgressUseCase.toggleTask(event.habitId, event.date, event.completed)
                if (event.completed) triggerConfetti()
            }
            HomeUiEvent.OnConfettiAnimationShown -> _explodeConfetti.value = false
        }
    }

    private fun triggerConfetti() {
        _explodeConfetti.value = true
    }
}
