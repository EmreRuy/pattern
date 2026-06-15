package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.model.HabitWithHistory
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
import com.example.pattern.utils.ExperienceUtils
import com.example.pattern.utils.calculateStreak
import com.example.pattern.utils.toUiDate
import java.time.Instant
import java.time.ZoneId
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.pattern.notifications.ReminderManager
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan

@Immutable
sealed interface HabitDetailsUiState {
    data object Loading : HabitDetailsUiState
    data class Success(val habit: HabitDetailsUi, val isDeleting: Boolean = false) : HabitDetailsUiState
    data object Error : HabitDetailsUiState
}

sealed interface DetailEvent {
    data object NavigateBack : DetailEvent
}

@HiltViewModel
class HabitDetailsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val reminderManager: ReminderManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val habitId: Int = checkNotNull(savedStateHandle["habitId"])
    
    // CONFLATED channel to ensure we don't process stale or duplicate navigation events
    private val _events = Channel<DetailEvent>(Channel.CONFLATED)
    val events = _events.receiveAsFlow()

    /**
     * Staff/Principal Pattern: State Preservation.
     * We use [scan] to ensure that once a habit is loaded, the UI never "flashes" 
     * to a blank/error state during the deletion or navigation transition.
     */
    val uiState: StateFlow<HabitDetailsUiState> = repository.getHabitWithHistoryStream(habitId)
        .distinctUntilChanged()
        .map { result ->
            when (result) {
                is DataResult.Loading -> HabitDetailsUiState.Loading
                is DataResult.Error -> HabitDetailsUiState.Error
                is DataResult.Success -> {
                    val habitWithHistory = result.data
                    if (habitWithHistory == null) HabitDetailsUiState.Error 
                    else HabitDetailsUiState.Success(habitWithHistory.toUi())
                }
            }
        }
        .scan(HabitDetailsUiState.Loading as HabitDetailsUiState) { previous, current ->
            // If the habit is deleted (current == Error) but we previously had Success,
            // we "freeze" the state to allow for a smooth exit animation.
            if ((current is HabitDetailsUiState.Error) && (previous is HabitDetailsUiState.Success)) {
                previous.copy(isDeleting = true)
            } else {
                current
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HabitDetailsUiState.Loading
        )

    fun deleteHabit() {
        viewModelScope.launch {
            val habitToDelete = repository.getHabitOnce(habitId)
            if (habitToDelete != null) {
                repository.deleteHabit(habitToDelete)
                reminderManager.cancelReminder(habitId)
                _events.send(DetailEvent.NavigateBack)
            }
        }
    }

    private fun HabitWithHistory.toUi(): HabitDetailsUi {
        val habit = this.habit
        val dailyStates = this.history
        val streakInfo = calculateStreak(habit, dailyStates)
        val totalXP = dailyStates.sumOf { ExperienceUtils.calculateHabitXP(habit, it) }
        
        return HabitDetailsUi(
            id = habit.id,
            name = habit.name,
            icon = habit.iconCode,
            accentColor = Color(habit.accentColorHex.toColorInt()),
            currentStreak = streakInfo.currentStreak,
            totalCompletions = streakInfo.totalCompletions,
            goal = goalLabel(habit.type, habit.durationInMinutes, habit.taskCount),
            frequency = frequencyLabel(habit.selectedDays),
            createdOn = habit.createdAt.toUiDate(),
            createdAtLocalDate = Instant.ofEpochMilli(habit.createdAt).atZone(ZoneId.systemDefault()).toLocalDate(),
            totalXP = totalXP,
            reminderTime = habit.reminderTime,
            motivation = habit.motivation,
            completedDates = CompletedDates(
                dailyStates.asSequence()
                    .filter { it.isCompleted || it.isTaskCompleted }
                    .map { it.date }
                    .toSet()
                    .toImmutableSet()
            )
        )
    }
}

fun goalLabel(type: HabitType, minutes: Int?, taskCount: Int?): String {
    return when (type) {
        HabitType.BUILD -> {
            if (minutes == null) return "0 m"
            val h = minutes / 60
            val m = minutes % 60
            when {
                h > 0 && m > 0 -> "$h h $m m"
                h > 0 -> "$h h"
                m > 0 -> "$m m"
                else -> "0 m"
            }
        }
        HabitType.TASK -> {
            val count = taskCount ?: 1
            if (count > 1) "$count times a day" else "Complete task"
        }
        HabitType.QUIT -> "Drop habit"
    }
}

fun frequencyLabel(selectedDays: List<Boolean>): String {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val active = days.zip(selectedDays).filter { it.second }.map { it.first }

    return when {
        active.size == 7 -> "Everyday"
        active.size == 5 && active.containsAll(
            listOf(
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri"
            )
        ) -> "Weekdays"

        active.size == 2 && active.containsAll(listOf("Sat", "Sun")) -> "Weekends"
        active.isEmpty() -> "No schedule"
        else -> active.joinToString(", ")
    }
}
