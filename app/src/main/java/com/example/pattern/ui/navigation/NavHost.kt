package com.example.pattern.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pattern.ui.screens.homeScreen.HomeScreen
import com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen.HabitDetailsRoute
import com.example.pattern.ui.screens.profileScreen.ProfileScreen

@Composable
fun NavHost(
    navController: NavHostController,
    showMenuSheet: () -> Unit,
    showSettingsSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route,
        modifier = modifier
    ) {
        composable(Screens.Home.route) {
            HomeScreen(
                navController = navController,
                onOpenMenuSheet = showMenuSheet,
                onOpenSettingsSheet = showSettingsSheet
            )
        }
        composable(Screens.Profile.route) {
            ProfileScreen(
                onOpenMenuSheet = showMenuSheet,
                onOpenSettingsSheet = showSettingsSheet
            )
        }
        composable(
            route = Screens.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.IntType }
            )
        ) {
           HabitDetailsRoute(onBack = { navController.popBackStack() })
        }
    }
}