package com.example.pattern.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

class NavActions(private val navController: NavHostController) {
    
    fun navigateToBottomBarRoute(route: Any) {
        if (route is Destination.Add) {
            navController.navigate(route)
            return
        }

        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun finishOnboarding() {
        navController.navigate(Destination.Home) {
            popUpTo<Destination.Onboarding> { inclusive = true }
        }
    }

    fun navigateToDetail(habitId: Int) {
        navController.navigate(Destination.HabitDetail(habitId))
    }

    fun navigateToEdit(habitId: Int) {
        navController.navigate(Destination.EditHabit(habitId))
    }

    fun navigateToSettings() {
        navController.navigate(Destination.Settings)
    }

    fun navigateToThemeSelection() {
        navController.navigate(Destination.ThemeSelection)
    }

    fun navigateToLanguageSelection() {
        navController.navigate(Destination.LanguageSelection)
    }

    fun navigateToPremium() {
        navController.navigate(Destination.Premium)
    }
    
    fun navigateToHabitList() {
        navController.navigate(Destination.HabitList)
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}
