package com.example.pattern.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pattern.domain.usecase.HabitLimitStatus
import com.example.pattern.ui.screens.homeScreen.HomeScreen
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.screens.profileScreen.ProfileScreen

@Composable
fun MainAppShell(
    rootActions: NavActions,
    habitLimitStatus: HabitLimitStatus
) {
    val shellNavController = rememberNavController()
    val navBackStackEntry by shellNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screens.Home.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomBar(
                currentRoute = currentRoute,
                onItemClick = { item ->
                    if (item.route == Screens.Add.route) {
                        if (habitLimitStatus is HabitLimitStatus.Reached) {
                            rootActions.navigateToPremium()
                        } else {
                            rootActions.navigateToBottomBarRoute(item.route)
                        }
                    } else {
                        shellNavController.navigate(item.route) {
                            popUpTo(shellNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = shellNavController,
                startDestination = Screens.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screens.Home.route) {
                    HomeScreen(
                        onOpenMenuScreen = { rootActions.navigateToHabitList() },
                        onSettingsClick = { rootActions.navigateToSettings() },
                        onHabitClick = { id -> rootActions.navigateToDetail(id) },
                        onPremiumClick = { rootActions.navigateToPremium() }
                    )
                }

                composable(Screens.Profile.route) {
                    ProfileScreen(
                        onOpenMenuSheet = { rootActions.navigateToHabitList() },
                        onOpenSettings = { rootActions.navigateToSettings() },
                        onPremiumClick = { rootActions.navigateToPremium() }
                    )
                }
            }
        }
    }
}
