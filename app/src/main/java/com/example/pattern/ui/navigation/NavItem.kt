package com.example.pattern.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route: String, val showBottomBar: Boolean = false) {
    /**
     * Extracts the base segment of the route (e.g., "habit_detail_route/{id}" -> "habit_detail_route").
     * This acts as the unique identifier for matching, regardless of dynamic arguments.
     */
    val rootRoute: String = route.substringBefore("/").substringBefore("?")

    data object Onboarding : Screens("onboarding_route")
    data object Home : Screens("home_route", showBottomBar = true)
    data object Add : Screens("add_route")
    data object Profile : Screens("profile_route", showBottomBar = true)
    
    data object HabitDetail : Screens("habit_detail_route/{habitId}") {
        fun createRoute(habitId: Int) = "habit_detail_route/$habitId"
    }
    
    data object EditHabit : Screens("edit_habit_route/{habitId}") {
        fun createRoute(habitId: Int) = "edit_habit_route/$habitId"
    }
    
    data object HabitList : Screens("habit_list_route")
    data object Settings : Screens("settings_route")
    data object ThemeSelection : Screens("theme_selection_route")
    data object LanguageSelection : Screens("language_selection_route")
    data object Premium : Screens("premium_route")

    companion object {
        private val allScreens by lazy {
            listOf(
                Onboarding, Home, Add, Profile, HabitDetail, 
                EditHabit, HabitList, Settings, ThemeSelection, LanguageSelection, Premium
            )
        }

        /**
         * Robustly finds the [Screens] object for any given route string.
         * Handles templates, resolved paths, and query parameters.
         */
        fun fromRoute(route: String?): Screens? {
            val requestRoot = route?.substringBefore("/")?.substringBefore("?") ?: return null
            return allScreens.find { it.rootRoute == requestRoot }
        }

        /**
         * Single source of truth for bottom bar visibility.
         */
        fun shouldShowBottomBar(route: String?): Boolean {
            return fromRoute(route)?.showBottomBar == true
        }

        /**
         * Robust route matching for selection state.
         * Prevents false positives by comparing root segments instead of simple [startsWith].
         */
        fun isRouteSelected(currentRoute: String?, targetRoute: String): Boolean {
            if (currentRoute == null) return false
            val currentRoot = currentRoute.substringBefore("/").substringBefore("?")
            val targetRoot = targetRoute.substringBefore("/").substringBefore("?")
            return currentRoot == targetRoot
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
