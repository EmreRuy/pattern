package com.example.pattern.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * Navigation animation constants to ensure consistency across the app.
 * Snappy durations (300ms/200ms) are preferred for a "premium" and responsive feel.
 */
private const val ANIMATION_DURATION_STANDARD = 300
private const val ANIMATION_DURATION_FAST = 200
private val NavigationEasing: Easing = FastOutSlowInEasing

fun fadeEnter(): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION_FAST,
        easing = NavigationEasing
    )
)

fun fadeExit(): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION_FAST,
        easing = NavigationEasing
    )
)

fun scaleEnter(): EnterTransition = fadeIn(
    animationSpec = tween(ANIMATION_DURATION_STANDARD, easing = NavigationEasing)
) + scaleIn(
    initialScale = 0.92f,
    animationSpec = tween(ANIMATION_DURATION_STANDARD, easing = NavigationEasing)
)

fun scaleExit(): ExitTransition = fadeOut(
    animationSpec = tween(ANIMATION_DURATION_STANDARD, easing = NavigationEasing)
) + scaleOut(
    targetScale = 0.92f,
    animationSpec = tween(ANIMATION_DURATION_STANDARD, easing = NavigationEasing)
)

fun AnimatedContentTransitionScope<*>.slideUpEnter(): EnterTransition = slideIntoContainer(
    towards = AnimatedContentTransitionScope.SlideDirection.Up,
    animationSpec = tween(ANIMATION_DURATION_STANDARD, easing = NavigationEasing)
)

fun AnimatedContentTransitionScope<*>.slideDownExit(): ExitTransition = slideOutOfContainer(
    towards = AnimatedContentTransitionScope.SlideDirection.Down,
    animationSpec = tween(ANIMATION_DURATION_STANDARD, easing = NavigationEasing)
)
