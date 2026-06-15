package com.example.pattern.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable data object Onboarding : Screen
    @Serializable data object Home : Screen
    @Serializable data object Add : Screen
    @Serializable data object Profile : Screen
    @Serializable data class HabitDetail(val habitId: Int) : Screen
    @Serializable data class EditHabit(val habitId: Int) : Screen
    @Serializable data object HabitList : Screen
    @Serializable data object Settings : Screen
    @Serializable data object Premium : Screen
}

data class BottomNavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
) {
    companion object {
        fun items(): List<BottomNavigationItem> = listOf(
            BottomNavigationItem(
                label = "Habits",
                selectedIcon = Icons.Rounded.GridView,
                unselectedIcon = Icons.Outlined.GridView,
                screen = Screen.Home
            ),
            BottomNavigationItem(
                label = "Add",
                selectedIcon = Icons.Rounded.Add,
                unselectedIcon = Icons.Rounded.Add,
                screen = Screen.Add
            ),
            BottomNavigationItem(
                label = "Insights",
                selectedIcon = Icons.Rounded.Analytics,
                unselectedIcon = Icons.Outlined.Analytics,
                screen = Screen.Profile
            )
        )
    }
}
