package com.example.pattern.ui.screens.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.LevelInfo
import com.example.pattern.utils.calculateStreak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val habitList: List<Habit> = emptyList(),
    val streaks: Map<Int, Int> = emptyMap(),
    val todayStates: Map<Int, HabitDailyState> = emptyMap(),
    val levelInfo: LevelInfo = ExperienceUtils.getLevelInfo(0),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val habitsFlow = repository.getAllHabitsStream().distinctUntilChanged()
    private val settingsFlow = repository.getSettingsStream().distinctUntilChanged()
    private val allDailyStatesFlow = repository.getAllDailyStatesStream().distinctUntilChanged()

    val uiState: StateFlow<HomeUiState> = combine(
        habitsFlow,
        settingsFlow,
        allDailyStatesFlow
    ) { habits, settings, allStates ->
        val levelInfo = ExperienceUtils.getLevelInfo(settings?.totalXP ?: 0)
        val today = LocalDate.now().toString()
        
        val statesByHabit = allStates.groupBy { it.habitId }
        val streaks = habits.associate { habit ->
            habit.id to calculateStreak(habit, statesByHabit[habit.id] ?: emptyList()).currentStreak
        }
        
        val todayStatesMap = allStates.filter { it.date == today }
            .associateBy { it.habitId }

        HomeUiState(
            habitList = habits,
            streaks = streaks,
            todayStates = todayStatesMap,
            levelInfo = levelInfo,
            isLoading = false
        )
    }.flowOn(Dispatchers.Default)
        .catch { e ->
        emit(HomeUiState(error = e.message, isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>> {
        return repository.getDailyStatesForDate(date)
    }

    fun startTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, date)
            if (currentDaily?.isCompleted == true) return@launch
            val updated = HabitDailyState(
                habitId = habitId,
                date = date,
                timerStartTime = System.currentTimeMillis(),
                timerPauseTime = null,
                isCompleted = false
            )
            repository.upsertDailyState(updated)
        }
    }

    fun pauseTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: return@launch
            if (currentDaily.isCompleted || currentDaily.timerStartTime == null) return@launch
            repository.upsertDailyState(currentDaily.copy(timerPauseTime = System.currentTimeMillis()))
        }
    }

    fun resumeTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: return@launch
            if (currentDaily.isCompleted || currentDaily.timerStartTime == null || currentDaily.timerPauseTime == null) return@launch
            val now = System.currentTimeMillis()
            val pausedDuration = now - currentDaily.timerPauseTime
            val newStartTime = currentDaily.timerStartTime + pausedDuration
            repository.upsertDailyState(currentDaily.copy(timerStartTime = newStartTime, timerPauseTime = null))
        }
    }

    fun finishTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: HabitDailyState(habitId = habitId, date = date)
            if (currentDaily.isCompleted) return@launch
            val updated = currentDaily.copy(isCompleted = true, timerStartTime = null, timerPauseTime = null)
            repository.upsertDailyState(updated)
            repository.addXP(ExperienceUtils.calculateHabitXP(habit, updated))
        }
    }

    fun unfinishTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: return@launch
            if (!currentDaily.isCompleted) return@launch
            repository.upsertDailyState(currentDaily.copy(isCompleted = false))
            repository.addXP(-ExperienceUtils.calculateHabitXP(habit, currentDaily))
        }
    }

    fun setTaskCompleted(habitId: Int, date: String, completed: Boolean) {
        viewModelScope.launch {
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, date)
            val wasCompleted = when(habit.type) {
                HabitType.TASK, HabitType.QUIT -> currentDaily?.isTaskCompleted == true
                HabitType.BUILD -> currentDaily?.isCompleted == true
            }
            repository.setTaskCompleted(habitId, date, completed)
            val updatedState = repository.getDailyStateOnce(habitId, date) ?: return@launch
            if (completed && !wasCompleted) {
                repository.addXP(ExperienceUtils.calculateHabitXP(habit, updatedState))
            } else if (!completed && wasCompleted) {
                repository.addXP(-ExperienceUtils.calculateHabitXP(habit, currentDaily ?: updatedState))
            }
        }
    }

    fun ensureDailyStateExists(habitId: Int, date: String) {
        viewModelScope.launch {
            repository.getHabitOnce(habitId) ?: return@launch
            val current = repository.getDailyStateOnce(habitId, date)
            if (current == null) {
                repository.upsertDailyState(HabitDailyState(habitId = habitId, date = date))
            }
        }
    }
}
