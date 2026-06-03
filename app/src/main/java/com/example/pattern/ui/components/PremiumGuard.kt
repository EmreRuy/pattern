package com.example.pattern.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.navigation.LocalNavActions
import com.example.pattern.ui.screens.premiumScreen.PremiumViewModel
import com.example.pattern.utils.PremiumPlanScreen

/**
 * Top-Tier Premium Guard.
 * 
 * Features:
 * 1. Zero-Flicker: Uses a nullable state to distinguish between 'Loading' and 'Free'.
 * 2. Smooth Transitions: Uses Crossfade for state changes.
 * 3. Professional UX: Prevents the paywall from showing before the premium status is confirmed.
 */
/**
 * Top-Tier Premium Guard.
 *
 * This implementation follows modern Android Best Practices (Clean MVVM, SSOT):
 * 1. Zero Flicker: Uses a nullable state (null = Unknown) to prevent showing the wrong UI during initialization.
 * 2. No Loading State: Instead of a spinner, we render the screen background for the few milliseconds 
 *    it takes to fetch the status.
 * 3. Perfect Transitions: Uses the requested slide-up animation for the paywall.
 */
@Composable
fun PremiumGuard(
    content: @Composable () -> Unit
) {
    val viewModel: PremiumViewModel = hiltViewModel()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val navActions = LocalNavActions.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Step 1: Content is ONLY rendered if the user is confirmed as Premium.
        // This prevents the "Free users seeing content for a split second" flicker.
        if (isPremium == true) {
            content()
        }

        // Step 2: Paywall is ONLY rendered if the user is confirmed as Free.
        // We use the 'old animation' as requested for the best UX feel.
        AnimatedVisibility(
            visible = isPremium == false,
            enter = fadeIn(animationSpec = tween(500)) + 
                    slideInVertically(initialOffsetY = { it / 4 }),
            exit = fadeOut(animationSpec = tween(300)) + 
                    slideOutVertically(targetOffsetY = { it / 4 })
        ) {
            PremiumPlanScreen(
                onBack = { navActions.popBackStack() }
            )
        }
    }
}
