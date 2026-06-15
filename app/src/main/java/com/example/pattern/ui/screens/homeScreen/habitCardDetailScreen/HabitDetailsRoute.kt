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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest

/**
 * Principal-level Route implementation.
 * Uses AnimatedContent for high-performance coordinated transitions.
 * Fixed: State preservation is now handled in the ViewModel using the 'scan' pattern,
 * ensuring the UI remains stable during the entire deletion/navigation phase.
 */
@Composable
fun HabitDetailsRoute(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: HabitDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation events from ViewModel (Principal: decoupled event handling)
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is DetailEvent.NavigateBack) onBack()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(250, delayMillis = 50)) togetherWith 
                fadeOut(animationSpec = tween(150))
            },
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
                    // Success state now includes an 'isDeleting' flag if needed, 
                    // but we always render the content to prevent flashes.
                    HabitCardDetailsScreen(
                        habit = state.habit,
                        onBack = onBack,
                        onDelete = viewModel::deleteHabit,
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}
