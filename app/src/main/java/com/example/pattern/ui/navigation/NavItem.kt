package com.example.pattern.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route: String, val showBottomBar: Boolean = false) {
    data object Onboarding : Screens("onboarding_route")
    data object Home : Screens("home_route", showBottomBar = true)
    data object Add : Screens("add_route")
    data object Profile : Screens("profile_route", showBottomBar = true)
    
    data object HabitDetail : Screens("habit_detail_route/{habitId}") {
        const val ROOT = "habit_detail_route"
        fun createRoute(habitId: Int) = "$ROOT/$habitId"
    }
    
    data object EditHabit : Screens("edit_habit_route/{habitId}") {
        const val ROOT = "edit_habit_route"
        fun createRoute(habitId: Int) = "$ROOT/$habitId"
    }
    
    data object HabitList : Screens("habit_list_route")
    data object Settings : Screens("settings_route")
    data object Premium : Screens("premium_route")

    companion object {
        fun shouldShowBottomBar(route: String?): Boolean {
            if (route == null) return false
            return when {
                route == Home.route -> true
                route == Profile.route -> true
                // Add more bottom bar destinations here if needed
                else -> false
            }
        }
    }
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
                unselectedIcon = Icons.Rounded.Add,
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
