package com.example.pattern.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

class NavActions(private val navController: NavHostController) {
    
    fun navigateToBottomBarRoute(route: String) {
        if (route == Screens.Add.route) {
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
        navController.navigate(Screens.Home.route) {
            popUpTo(Screens.Onboarding.route) { inclusive = true }
        }
    }

    fun navigateToDetail(habitId: Int) {
        navController.navigate(Screens.HabitDetail.createRoute(habitId))
    }

    fun navigateToEdit(habitId: Int) {
        navController.navigate(Screens.EditHabit.createRoute(habitId))
    }

    fun navigateToSettings() {
        navController.navigate(Screens.Settings.route)
    }

    fun navigateToThemeSelection() {
        navController.navigate(Screens.ThemeSelection.route)
    }

    fun navigateToLanguageSelection() {
        navController.navigate(Screens.LanguageSelection.route)
    }

    fun navigateToPremium() {
        navController.navigate(Screens.Premium.route)
    }
    
    fun navigateToHabitList() {
        navController.navigate(Screens.HabitList.route)
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}
