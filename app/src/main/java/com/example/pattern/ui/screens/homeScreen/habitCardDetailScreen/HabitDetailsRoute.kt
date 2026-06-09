package com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

/**
 * Staff-level Route implementation.
 * Uses Crossfade to prevent "hard pops" between loading and content states.
 * For a premium experience, consider passing initial habit metadata (color, name)
 * to render a skeleton UI immediately.
 */
@Composable
fun HabitDetailsRoute(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: HabitDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isDeleting by remember { mutableStateOf(false) }

    if (isDeleting) return

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(
            targetState = uiState,
            animationSpec = tween(durationMillis = 300),
            label = "screen_transition"
        ) { state ->
            when (state) {
                HabitDetailsUiState.Loading -> {
                    Box(Modifier.fillMaxSize())
                }
                HabitDetailsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading habit")
                    }
                }
                is HabitDetailsUiState.Success -> {
                    HabitCardDetailsScreen(
                        habit = state.habit,
                        onBack = onBack,
                        onDelete = {
                            isDeleting = true
                            viewModel.deleteHabit(state.habit.id)
                            onBack()
                        },
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}
