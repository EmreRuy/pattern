package com.example.pattern.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

/**
 * Snappy, high-performance motion constants.
 * Optimized for a "Premium Section" feel inspired by the Add Habit sub-sections.
 */
private const val DURATION_SNAPPY = 250
private const val DURATION_ULTRA_FAST = 200

/**
 * Ultra-Responsive Easing:
 * A custom "Decelerate" curve that makes the UI feel light and immediate.
 */
private val SnappyEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

fun fadeEnter(): EnterTransition = fadeIn(
    animationSpec = tween(durationMillis = DURATION_ULTRA_FAST)
)

fun fadeExit(): ExitTransition = fadeOut(
    animationSpec = tween(durationMillis = DURATION_ULTRA_FAST)
)

/**
 * Perfect Section Transition:
 * Inspired by the "Color, Icon, Category" sections in the Add Habit screen.
 * Uses a subtle vertical slide (it / 10) combined with a snappy fade and scale
 * for an extremely clean and "instant" feel.
 */
fun AnimatedContentTransitionScope<*>.perfectSectionEnter(): EnterTransition = 
    fadeIn(animationSpec = tween(DURATION_SNAPPY, delayMillis = 50)) + 
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        initialOffset = { it / 10 },
        animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
    ) + scaleIn(
        initialScale = 0.98f,
        animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
    )

fun AnimatedContentTransitionScope<*>.perfectSectionExit(): ExitTransition = 
    fadeOut(animationSpec = tween(DURATION_ULTRA_FAST)) + 
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        targetOffset = { it / 10 },
        animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
    )

fun AnimatedContentTransitionScope<*>.perfectSectionPopEnter(): EnterTransition = 
    fadeIn(animationSpec = tween(DURATION_SNAPPY)) + 
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        initialOffset = { it / 10 },
        animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
    )

fun AnimatedContentTransitionScope<*>.perfectSectionPopExit(): ExitTransition = 
    fadeOut(animationSpec = tween(DURATION_ULTRA_FAST)) + 
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        targetOffset = { it / 10 },
        animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
    ) + scaleOut(
        targetScale = 0.98f,
        animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
    )

// --- LEGACY SHARED AXIS Z ---

fun sharedAxisZEnter(): EnterTransition = fadeIn(
    animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
) + scaleIn(
    initialScale = 0.96f,
    animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
)

fun sharedAxisZExit(): ExitTransition = fadeOut(
    animationSpec = tween(DURATION_ULTRA_FAST, easing = SnappyEasing)
) + scaleOut(
    targetScale = 1.02f,
    animationSpec = tween(DURATION_ULTRA_FAST, easing = SnappyEasing)
)

fun sharedAxisZPopEnter(): EnterTransition = fadeIn(
    animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
) + scaleIn(
    initialScale = 1.02f,
    animationSpec = tween(DURATION_SNAPPY, easing = SnappyEasing)
)

fun sharedAxisZPopExit(): ExitTransition = fadeOut(
    animationSpec = tween(DURATION_ULTRA_FAST, easing = SnappyEasing)
) + scaleOut(
    targetScale = 0.96f,
    animationSpec = tween(DURATION_ULTRA_FAST, easing = SnappyEasing)
)
