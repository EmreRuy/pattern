package com.example.pattern.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.local.entity.Habit
import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.notifications.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.LevelInfo
import com.example.pattern.utils.calculateStreak
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

data class HomeUiState(
    val habitList: List<Habit> = emptyList(),
    val streaks: Map<Int, Int> = emptyMap(),
    val levelInfo: LevelInfo = ExperienceUtils.getLevelInfo(0),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {
    /*
      The StateFlow that the HomeScreen will observe. It contains the list of all habits
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.getAllHabitsStream(),
        repository.getSettingsStream()
    ) { habits, settings ->
        habits to (settings?.totalXP ?: 0)
    }.flatMapLatest { (habits, totalXP) ->
        val levelInfo = ExperienceUtils.getLevelInfo(totalXP)
        val streakFlows = habits.map { habit ->
            repository.getDailyStatesForHabit(habit.id).map { states ->
                habit.id to calculateStreak(habit, states).currentStreak
            }
        }
        if (streakFlows.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(
                HomeUiState(
                    habitList = habits,
                    levelInfo = levelInfo
                )
            )
        } else {
            combine(streakFlows) { streakPairs ->
                HomeUiState(
                    habitList = habits,
                    streaks = streakPairs.toMap(),
                    levelInfo = levelInfo
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun getDailyStatesForDate(date: String): Flow<List<HabitDailyState>> {
        return repository.getDailyStatesForDate(date)
    }

    fun saveNewHabit(
        name: String,
        type: HabitType,
        durationHours: Int,
        durationMinutes: Int,
        selectedDays: List<Boolean>,
        iconCode: String,
        accentColorHex: String,
        reminderTime: String? = null,
        motivation: String? = null
    ) {
        if (name.isBlank()) {
            println("Error: Habit name cannot be empty.")
            return
        }
        // Converts hours/minutes into a single Int in minutes
        val totalDurationInMinutes = if (type == HabitType.BUILD) {
            (durationHours * 60) + durationMinutes
        } else {
            null
        }

        // Creates the Habit Entity object
        val newHabit = Habit(
            name = name.trim(),
            type = type,
            iconCode = iconCode,
            durationInMinutes = totalDurationInMinutes,
            selectedDays = selectedDays,
            accentColorHex = accentColorHex,
            reminderTime = reminderTime,
            motivation = if (motivation.isNullOrBlank()) null else motivation.trim()
        )
        viewModelScope.launch {
            try {
                val id = repository.upsertHabit(newHabit)
                val habitWithId = newHabit.copy(id = id.toInt())
                reminderManager.scheduleReminder(habitWithId)
                println("Habit saved and reminder scheduled: ${newHabit.name}")

            } catch (e: Exception) {
                println("Failed to save habit: ${e.message}")
            }
        }
    }


    fun updateQuietHoursSettings(enabled: Boolean, start: String, end: String) {
        viewModelScope.launch {
            repository.updateQuietHours(enabled, start, end)
        }
    }
    val settingsState = repository.getSettingsStream()
        .map { it ?: SettingsEntity() } // If DB is empty, provide defaults
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            // Reschedule to update the notification data (e.g. new name)
            reminderManager.scheduleReminder(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            reminderManager.cancelReminder(habit.id)
        }
    }
    fun startTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            // Verify habit exists first to avoid FK constraint violation
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
            if (currentDaily.isCompleted) return@launch
            if (currentDaily.timerStartTime == null) return@launch
            val updated = currentDaily.copy(
                timerPauseTime = System.currentTimeMillis()
            )
            repository.upsertDailyState(updated)
        }
    }

    fun resumeTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: return@launch
            if (currentDaily.isCompleted) return@launch
            if (currentDaily.timerStartTime == null) return@launch
            if (currentDaily.timerPauseTime == null) return@launch
            val now = System.currentTimeMillis()
            val pausedDuration = now - currentDaily.timerPauseTime
            val newStartTime = currentDaily.timerStartTime + pausedDuration
            val updated = currentDaily.copy(
                timerStartTime = newStartTime,
                timerPauseTime = null
            )
            repository.upsertDailyState(updated)
        }
    }

    fun finishTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: HabitDailyState(habitId = habitId, date = date)
            
            if (currentDaily.isCompleted) return@launch

            val updated = currentDaily.copy(
                isCompleted = true,
                timerStartTime = null,
                timerPauseTime = null
            )
            repository.upsertDailyState(updated)
            
            val xp = ExperienceUtils.calculateHabitXP(habit, updated)
            repository.addXP(xp)
        }
    }

    fun unfinishTimer(habitId: Int, date: String) {
        viewModelScope.launch {
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, date) ?: return@launch
            
            if (!currentDaily.isCompleted) return@launch

            val updated = currentDaily.copy(
                isCompleted = false
            )
            repository.upsertDailyState(updated)
            
            val xp = ExperienceUtils.calculateHabitXP(habit, currentDaily)
            repository.addXP(-xp)
        }
    }

    //For the task type of habit completion
    fun setTaskCompleted(habitId: Int, date: String, completed: Boolean) {
        viewModelScope.launch {
            val habit = repository.getHabitOnce(habitId) ?: return@launch
            val currentDaily = repository.getDailyStateOnce(habitId, date)
            
            // Only award XP if transitioning from not completed to completed
            val wasCompleted = when(habit.type) {
                HabitType.TASK, HabitType.QUIT -> currentDaily?.isTaskCompleted == true
                HabitType.BUILD -> currentDaily?.isCompleted == true
            }

            repository.setTaskCompleted(habitId, date, completed)
            
            if (completed && !wasCompleted) {
                val updatedState = repository.getDailyStateOnce(habitId, date) ?: return@launch
                val xp = ExperienceUtils.calculateHabitXP(habit, updatedState)
                repository.addXP(xp)
            } else if (!completed && wasCompleted) {
                val updatedState = currentDaily ?: return@launch
                val xp = ExperienceUtils.calculateHabitXP(habit, updatedState)
                repository.addXP(-xp) // Subtract XP when un-completing
            }
        }
    }
    fun ensureDailyStateExists(habitId: Int, date: String) {
        viewModelScope.launch {
            // Verify habit exists first
            repository.getHabitOnce(habitId) ?: return@launch

            val current = repository.getDailyStateOnce(habitId, date)
            if (current == null) {
                repository.upsertDailyState(
                    HabitDailyState(
                        habitId = habitId,
                        date = date,
                        isCompleted = false,
                        isTaskCompleted = false
                    )
                )
            }
        }
    }
}