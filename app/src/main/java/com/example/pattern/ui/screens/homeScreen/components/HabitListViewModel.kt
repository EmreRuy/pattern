package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.domain.model.Habit
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
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
 * Now integrated with DataResult for unified state handling.
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    val uiState: StateFlow<HabitListUiState> = combine(
        repository.getAllHabitsStream(),
        repository.getDailyStatesForDate(LocalDate.now().toString())
    ) { habitsRes, todayStatesRes ->
        
        if (habitsRes is DataResult.Error) return@combine HabitListUiState.Error(habitsRes.exception.message ?: "Sync error")
        if (todayStatesRes is DataResult.Error) return@combine HabitListUiState.Error(todayStatesRes.exception.message ?: "Sync error")

        if (habitsRes is DataResult.Loading || todayStatesRes is DataResult.Loading) {
            return@combine HabitListUiState.Loading
        }

        if (habitsRes is DataResult.Success && todayStatesRes is DataResult.Success) {
            val habits = habitsRes.data
            val todayStates = todayStatesRes.data
            val todayStatesMap = todayStates.associateBy { it.habitId }

            HabitListUiState.Success(
                habits = HabitList(habits),
                todayStates = DailyStateMap(todayStatesMap)
            )
        } else {
            HabitListUiState.Loading
        }
    }
    .distinctUntilChanged()
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitListUiState.Loading
    )
}
