package com.example.pattern.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route: String) {
    data object Onboarding : Screens("onboarding_route")
    data object Home : Screens("home_route")
    data object Add : Screens("add_route")
    data object Profile : Screens("profile_route")
    data object HabitDetail : Screens("habit_detail_route/{habitId}") {
        fun createRoute(habitId: Int) = "habit_detail_route/$habitId"
    }
    data object EditHabit : Screens("edit_habit_route/{habitId}") {
        fun createRoute(habitId: Int) = "edit_habit_route/$habitId"
    }
    data object HabitList : Screens("habit_list_route")
    data object Settings : Screens("settings_route")
    data object Premium : Screens("premium_route")
}

data class BottomNavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    companion object {
        fun items(): List<BottomNavigationItem> = listOf(
            BottomNavigationItem(
                label = "Habits",
                selectedIcon = Icons.Rounded.GridView,
                unselectedIcon = Icons.Outlined.GridView,
                route = Screens.Home.route
            ),
            BottomNavigationItem(
                label = "Add",
                selectedIcon = Icons.Rounded.Add,
                unselectedIcon = Icons.Rounded.Add, // Add usually stays same
                route = Screens.Add.route
            ),
            BottomNavigationItem(
                label = "Insights",
                selectedIcon = Icons.Rounded.Analytics,
                unselectedIcon = Icons.Outlined.Analytics,
                route = Screens.Profile.route
            )
        )
    }
}
