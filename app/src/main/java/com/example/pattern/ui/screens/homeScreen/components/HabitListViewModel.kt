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

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    val uiState: StateFlow<HabitListUiState> = combine(
        repository.getAllHabitsStream(),
        repository.getAllDailyStatesStream() 
    ) { habits, allStates ->
        val today = LocalDate.now().toString()
        val todayStatesMap = allStates.filter { it.date == today }
            .associateBy { it.habitId }

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
