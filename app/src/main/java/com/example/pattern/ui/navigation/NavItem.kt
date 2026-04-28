package com.example.pattern.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.LensBlur
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route: String) {
    object Home : Screens("home_route")
    object Add : Screens("add_route")
    object Profile : Screens("profile_route")
    object HabitDetail : Screens("habit_detail_route/{habitId}") {
        fun createRoute(habitId: Int) = "habit_detail_route/$habitId"
    }
    object EditHabit : Screens("edit_habit_route/{habitId}") {
        fun createRoute(habitId: Int) = "edit_habit_route/$habitId"
    }
    object HabitList : Screens("habit_list_route")
    object Settings : Screens("settings_route")
}

data class BottomNavigationItem(
    val label: String = "",
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = ""
) {
    //function to get the list of bottomNavigationItems
    fun bottomNavigationItems(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = "Habits",
                icon =   Icons.Outlined.LensBlur,
                route = Screens.Home.route
            ),
            BottomNavigationItem(
                label = "Add",
                icon = Icons.Filled.Add,
                route = Screens.Add.route
            ),
            BottomNavigationItem(
                label = "Insights",
                Icons.Filled.PieChart,
                route = Screens.Profile.route
            ),
        )
    }
}
