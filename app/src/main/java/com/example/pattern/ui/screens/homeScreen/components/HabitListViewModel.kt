package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class HabitList(val items: List<Habit>)

@Immutable
data class DailyStateMap(val states: Map<Int, HabitDailyState>)

@Immutable
sealed interface HabitListUiState {
    data object Loading : HabitListUiState
    data class Success(
        val habits: HabitList,
        val todayStates: DailyStateMap
    ) : HabitListUiState
    data class Error(val message: String) : HabitListUiState
}

/**
 * Optimized ViewModel for the Home Screen.
 * 
 * Performance Fix:
 * Instead of fetching ALL historical daily states and filtering in memory (O(History)),
 * it now only observes today's states from the database (O(1)).
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    // Optimized stream using specific today's data
    val uiState: StateFlow<HabitListUiState> = combine(
        repository.getAllHabitsStream(),
        // Only observe today's states - massive performance win for long-term users
        repository.getDailyStatesForDate(LocalDate.now().toString())
    ) { habits, todayStates ->
        val todayStatesMap = todayStates.associateBy { it.habitId }

        HabitListUiState.Success(
            habits = HabitList(habits),
            todayStates = DailyStateMap(todayStatesMap)
        ) as HabitListUiState
    }
    .distinctUntilChanged()
    .flowOn(Dispatchers.Default)
    .catch { e ->
        emit(HabitListUiState.Error(e.message ?: "Unknown error"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitListUiState.Loading
    )
}
