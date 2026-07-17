package com.example.pattern.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

class NavActions(private val navController: NavHostController) {
    
    private var lastPopTime = 0L
    private val popDebounceMs = 500L

    fun navigateToBottomBarRoute(screen: Screen) {
        navController.navigate(screen) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun finishOnboarding() {
        navController.navigate(Screen.Dashboard) {
            popUpTo(Screen.Onboarding) { inclusive = true }
        }
    }

    fun navigateToDetail(habitId: Int) {
        navController.navigate(Screen.HabitDetail(habitId))
    }

    fun navigateToEdit(habitId: Int) {
        navController.navigate(Screen.EditHabit(habitId))
    }

    fun navigateToSettings() {
        navController.navigate(Screen.Settings)
    }

    fun navigateToPremium() {
        navController.navigate(Screen.Premium)
    }
    
    fun navigateToHabitList() {
        navController.navigate(Screen.HabitList)
    }

    fun popBackStack() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPopTime > popDebounceMs) {
            navController.popBackStack()
            lastPopTime = currentTime
        }
    }
}
