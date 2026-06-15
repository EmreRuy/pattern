package com.example.pattern.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

class NavActions(private val navController: NavHostController) {
    
    private var lastPopTime = 0L
    private val popDebounceMs = 500L

    fun navigateToBottomBarRoute(route: String) {
        if (route == Screens.Add.route) {
            navController.navigate(route)
            return
        }

        // If trying to navigate to Home or Profile from root (e.g. from Onboarding),
        // target the MainShell instead.
        val targetRoute = if (route == Screens.Home.route || route == Screens.Profile.route) {
            Screens.MainShell.route
        } else {
            route
        }

        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun finishOnboarding() {
        navController.navigate(Screens.MainShell.route) {
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

    fun navigateToPremium() {
        navController.navigate(Screens.Premium.route)
    }
    
    fun navigateToHabitList() {
        navController.navigate(Screens.HabitList.route)
    }

    fun popBackStack() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPopTime > popDebounceMs) {
            navController.popBackStack()
            lastPopTime = currentTime
        }
    }
}
