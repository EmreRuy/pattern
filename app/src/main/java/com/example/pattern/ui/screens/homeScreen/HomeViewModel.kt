package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.model.HabitCardModel
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.domain.usecase.GetHomeHabitsUseCase
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.LevelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isSelectedDateToday: Boolean = true,
    val habits: List<HabitCardModel> = emptyList(),
    val habitsByDate: Map<LocalDate, List<HabitCardModel>> = emptyMap(),
    val hasAnyHabits: Boolean = false,
    val levelInfo: LevelInfo = ExperienceUtils.getLevelInfo(0),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val getHomeHabitsUseCase: GetHomeHabitsUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val settingsFlow = repository.getSettingsStream().distinctUntilChanged()
    private val habitsFlow = repository.getAllHabitsStream().distinctUntilChanged()

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        settingsFlow,
        habitsFlow.map { it.isNotEmpty() }.distinctUntilChanged()
    ) { date, settings, hasAnyHabits ->
        Triple(date, settings, hasAnyHabits)
    }.flatMapLatest { (date, settings, hasAnyHabits) ->
        // Fetch a window of days (yesterday, today, tomorrow) for snappy swiping
        val dateWindow = listOf(date.minusDays(1), date, date.plusDays(1))

        combine(
            dateWindow.map { d ->
                getHomeHabitsUseCase(d).map { habits -> d to habits }
            }
        ) { results ->
            val habitsMap = results.toMap()
            HomeUiState(
                selectedDate = date,
                isSelectedDateToday = date == LocalDate.now(),
                habits = habitsMap[date] ?: emptyList(),
                habitsByDate = habitsMap,
                hasAnyHabits = hasAnyHabits,
                levelInfo = ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0),
                isLoading = false
            )
        }
    }
        .flowOn(Dispatchers.Default)
        .catch { e ->
            emit(HomeUiState(error = e.message, isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun startTimer(habitId: Int, date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.toString()
            repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr)
            if (currentDaily?.isCompleted == true) return@launch
            val updated = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
                timerStartTime = System.currentTimeMillis(),
                timerPauseTime = null,
                isCompleted = false
            )
            repository.upsertDailyState(updated)
        }
    }

    fun pauseTimer(habitId: Int, date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return@launch
            if (currentDaily.isCompleted || currentDaily.timerStartTime == null) return@launch
            repository.upsertDailyState(currentDaily.copy(timerPauseTime = System.currentTimeMillis()))
        }
    }

    fun resumeTimer(habitId: Int, date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return@launch
            if (currentDaily.isCompleted || currentDaily.timerStartTime == null || currentDaily.timerPauseTime == null) return@launch
            val now = System.currentTimeMillis()
            val pausedDuration = now - currentDaily.timerPauseTime
            val newStartTime = currentDaily.timerStartTime + pausedDuration
            repository.upsertDailyState(currentDaily.copy(timerStartTime = newStartTime, timerPauseTime = null))
        }
    }

    fun finishTimer(habitId: Int, date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: HabitDailyState(habitId = habitId, date = dateStr)
            if (currentDaily.isCompleted) return@launch
            val updated = currentDaily.copy(isCompleted = true, timerStartTime = null, timerPauseTime = null)
            repository.upsertDailyState(updated)
            repository.addXP(ExperienceUtils.calculateHabitXP(habit, updated))
        }
    }

    fun unfinishTimer(habitId: Int, date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return@launch
            if (!currentDaily.isCompleted) return@launch
            repository.upsertDailyState(currentDaily.copy(isCompleted = false))
            repository.addXP(-ExperienceUtils.calculateHabitXP(habit, currentDaily))
        }
    }

    fun setTaskCompleted(habitId: Int, date: LocalDate, completed: Boolean) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr)
            
            val wasCompleted = when(habit.type) {
                HabitType.TASK, HabitType.QUIT -> currentDaily?.isTaskCompleted == true
                HabitType.BUILD -> currentDaily?.isCompleted == true
            }

            if (wasCompleted == completed) return@launch

            val updatedState = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
                isTaskCompleted = completed,
                isCompleted = completed 
            )
            repository.upsertDailyState(updatedState)

            if (completed) {
                repository.addXP(ExperienceUtils.calculateHabitXP(habit, updatedState))
            } else {
                repository.addXP(-ExperienceUtils.calculateHabitXP(habit, currentDaily ?: updatedState))
            }
        }
    }
}
