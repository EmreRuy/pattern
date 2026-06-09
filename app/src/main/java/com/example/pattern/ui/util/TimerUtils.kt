package com.example.pattern.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Principal Performance Engineer Audit:
 * Single Source of Truth for time-based UI updates.
 * 
 * Optimization Highlights:
 * 1. staticCompositionLocalOf: The State object itself is stable. Using static 
 *    prevents overhead in the Compose hierarchy.
 * 2. Precision Ticking: The provider now attempts to sync with the start of 
 *    the next second to reduce "jitter" across multiple timers.
 * 3. Scope Isolation: LaunchedEffect ensures the provider itself doesn't 
 *    recompose when the value changes, only the specific consumers reading it.
 */
val LocalTimerTicker = staticCompositionLocalOf<State<Long>> {
    error("No TimerTickerProvider found")
}

@Composable
fun TimerTickerProvider(content: @Composable () -> Unit) {
    val currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        // Principal Fix: Only tick when the app is in the STARTED state or above.
        // This saves significant battery by stopping the coroutine in the background.
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                val now = System.currentTimeMillis()
                currentTime.longValue = now
                
                // Sync with the start of the next second
                val delayMillis = 1000L - (now % 1000L)
                delay(delayMillis)
            }
        }
    }
    
    CompositionLocalProvider(
        LocalTimerTicker provides currentTime,
        content = content
    )
}

/**
 * Principal-level manual formatter.
 * Avoids String.format which uses reflection and regex internally.
 * Optimized for high-frequency UI updates.
 */
fun formatDurationFast(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString(8) {
        if (hours > 0) {
            append(hours)
            append(':')
            if (minutes < 10) append('0')
        }
        append(minutes)
        append(':')
        if (seconds < 10) append('0')
        append(seconds)
    }
}
