package com.example.pattern.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.pattern.domain.usecase.HabitLimitStatus
import com.example.pattern.ui.screens.addHabitScreen.AddHabitScreen
import com.example.pattern.ui.screens.addHabitScreen.EditHabitScreen
import com.example.pattern.ui.screens.homeScreen.HomeScreen
import com.example.pattern.ui.screens.homeScreen.components.CustomBottomBar
import com.example.pattern.ui.screens.homeScreen.components.HabitListScreen
import com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen.HabitDetailsRoute
import com.example.pattern.ui.screens.profileScreen.ProfileScreen
import com.example.pattern.ui.screens.settings.SettingsScreen
import com.example.pattern.utils.PremiumPlanScreen

/**
 * Root Navigation Host: Manages full-viewport transitions and the Main Dashboard.
 * Now using the "Perfect Section" transition style found in the Add Habit screen.
 */
@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    habitLimitStatus: HabitLimitStatus = HabitLimitStatus.Unlimited,
    startDestination: Screen = Screen.Dashboard,
    onOnboardingFinish: () -> Unit = {}
) {
    val actions = remember(navController) { NavActions(navController) }
    
    val rootStartDestination = remember(startDestination) {
        when (startDestination) {
            is Screen.Home, is Screen.Profile, is Screen.HabitList -> Screen.Dashboard
            else -> startDestination
        }
    }

    NavHost(
        navController = navController,
        startDestination = rootStartDestination,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn(tween(250)) },
        exitTransition = { fadeOut(tween(200)) }
    ) {
        composable<Screen.Onboarding> {
            com.example.pattern.ui.screens.onboarding.OnboardingScreen(
                onFinish = onOnboardingFinish
            )
        }

        composable<Screen.Dashboard> {
            MainDashboard(
                rootNavController = navController,
                habitLimitStatus = habitLimitStatus,
                actions = actions
            )
        }

        // --- FULL VIEWPORT OVERLAY SCREENS ---
        // Using the snappy "Perfect Section" transition (Subtle Slide + Scale + Fade)

        composable<Screen.Add>(
            enterTransition = { perfectSectionEnter() },
            exitTransition = { perfectSectionExit() },
            popEnterTransition = { perfectSectionPopEnter() },
            popExitTransition = { perfectSectionPopExit() }
        ) {
            AddHabitScreen(
                onSaveSuccess = { actions.popBackStack() },
                onBack = { actions.popBackStack() }
            )
        }

        composable<Screen.HabitDetail>(
            enterTransition = { habitDetailEnter() },
            exitTransition = { habitDetailExit() },
            popEnterTransition = { habitDetailPopEnter() },
            popExitTransition = { habitDetailPopExit() }
        ) { backStackEntry ->
            val habitDetail: Screen.HabitDetail = backStackEntry.toRoute()
            HabitDetailsRoute(
                onBack = { actions.popBackStack() },
                onEdit = { actions.navigateToEdit(habitDetail.habitId) }
            )
        }

        composable<Screen.EditHabit>(
            enterTransition = { perfectSectionEnter() },
            exitTransition = { perfectSectionExit() },
            popEnterTransition = { perfectSectionPopEnter() },
            popExitTransition = { perfectSectionPopExit() }
        ) {
            EditHabitScreen(
                onSaveSuccess = { actions.popBackStack() },
                onBack = { actions.popBackStack() }
            )
        }

        composable<Screen.Settings>(
            enterTransition = { perfectSectionEnter() },
            exitTransition = { perfectSectionExit() },
            popEnterTransition = { perfectSectionPopEnter() },
            popExitTransition = { perfectSectionPopExit() }
        ) {
            SettingsScreen(onBack = { actions.popBackStack() })
        }

        composable<Screen.HabitList>(
            enterTransition = { perfectSectionEnter() },
            exitTransition = { perfectSectionExit() },
            popEnterTransition = { perfectSectionPopEnter() },
            popExitTransition = { perfectSectionPopExit() }
        ) {
            HabitListScreen(
                onHabitClick = { id -> actions.navigateToDetail(id) },
                onBack = { actions.popBackStack() }
            )
        }

        composable<Screen.Premium>(
            enterTransition = { perfectSectionEnter() },
            exitTransition = { perfectSectionExit() },
            popEnterTransition = { perfectSectionPopEnter() },
            popExitTransition = { perfectSectionPopExit() }
        ) {
            PremiumPlanScreen(onBack = { actions.popBackStack() })
        }
    }
}

/**
 * Main Dashboard: Hosts the Bottom Navigation Bar and persistent tab destinations.
 */
@Composable
fun MainDashboard(
    rootNavController: NavHostController,
    habitLimitStatus: HabitLimitStatus,
    actions: NavActions
) {
    val dashboardNavController = rememberNavController()
    val navBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomBar(
                currentDestination = currentDestination,
                onItemClick = { item ->
                    when (item.screen) {
                        is Screen.Add -> {
                            if (habitLimitStatus is HabitLimitStatus.Reached) {
                                actions.navigateToPremium()
                            } else {
                                rootNavController.navigate(Screen.Add)
                            }
                        }
                        else -> {
                            dashboardNavController.navigate(item.screen) {
                                popUpTo(dashboardNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = dashboardNavController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onOpenMenuScreen = { actions.navigateToHabitList() },
                    onSettingsClick = { actions.navigateToSettings() },
                    onHabitClick = { id -> actions.navigateToDetail(id) },
                    onPremiumClick = { actions.navigateToPremium() }
                )
            }

            composable<Screen.Profile> {
                ProfileScreen(
                    onOpenMenuSheet = { actions.navigateToHabitList() },
                    onOpenSettings = { actions.navigateToSettings() },
                    onPremiumClick = { actions.navigateToPremium() }
                )
            }
        }
    }
}
