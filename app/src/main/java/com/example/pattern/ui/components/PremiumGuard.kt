package com.example.pattern.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.navigation.LocalNavActions
import com.example.pattern.ui.screens.premiumScreen.PremiumViewModel
import com.example.pattern.utils.PremiumPlanScreen

@Composable
fun PremiumGuard(
    content: @Composable () -> Unit
) {
    val viewModel: PremiumViewModel = hiltViewModel()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val navActions = LocalNavActions.current

    Box(modifier = Modifier.fillMaxSize()) {
        // The actual content
        content()

        // Staff-Level Overlay: Smooth fade-in of the paywall
        AnimatedVisibility(
            visible = !isPremium,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 4 }),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it / 4 })
        ) {
            PremiumPlanScreen(
                onBack = { navActions.popBackStack() }
            )
        }
    }
}
