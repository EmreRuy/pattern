package com.example.pattern.ui.navigation

import androidx.compose.runtime.compositionLocalOf

val LocalNavActions = compositionLocalOf<NavActions> {
    error("No NavActions provided. Did you forget to wrap your app in CompositionLocalProvider?")
}
