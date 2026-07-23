package com.example.pattern.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Onboarding : Destination

    @Serializable
    data object Home : Destination

    @Serializable
    data object Add : Destination

    @Serializable
    data object Profile : Destination

    @Serializable
    data object HabitList : Destination

    @Serializable
    data class HabitDetail(val habitId: Int) : Destination

    @Serializable
    data class EditHabit(val habitId: Int) : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data object ThemeSelection : Destination

    @Serializable
    data object LanguageSelection : Destination

    @Serializable
    data object Premium : Destination
}
