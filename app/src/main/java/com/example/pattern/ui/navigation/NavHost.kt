package com.example.pattern.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    habitLimitStatus: HabitLimitStatus = HabitLimitStatus.Unlimited,
    startDestination: Screen = Screen.Home,
    onOnboardingFinish: () -> Unit = {}
) {
    val actions = remember(navController) { NavActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hasRoute<Screen.Home>() == true ||
                       currentDestination?.hasRoute<Screen.Profile>() == true ||
                       currentDestination?.hasRoute<Screen.Add>() == true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                CustomBottomBar(
                    currentDestination = currentDestination,
                    onItemClick = { item ->
                        if (item.screen == Screen.Add) {
                            if (habitLimitStatus is HabitLimitStatus.Reached) {
                                actions.navigateToPremium()
                            } else {
                                actions.navigateToBottomBarRoute(item.screen)
                            }
                        } else {
                            actions.navigateToBottomBarRoute(item.screen)
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable<Screen.Onboarding> {
                com.example.pattern.ui.screens.onboarding.OnboardingScreen(
                    onFinish = onOnboardingFinish
                )
            }

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

            composable<Screen.HabitList>(
                enterTransition = { scaleEnter() },
                exitTransition = { scaleExit() },
                popEnterTransition = { scaleEnter() },
                popExitTransition = { scaleExit() }
            ) {
                HabitListScreen(
                    onHabitClick = { id -> actions.navigateToDetail(id) },
                    onBack = { actions.popBackStack() }
                )
            }

            composable<Screen.Add>(
                enterTransition = { scaleEnter() },
                exitTransition = { scaleExit() },
                popEnterTransition = { scaleEnter() },
                popExitTransition = { scaleExit() }
            ) {
                AddHabitScreen(
                    onSaveSuccess = { actions.popBackStack() },
                    onBack = { actions.popBackStack() }
                )
            }

            composable<Screen.HabitDetail>(
                enterTransition = { slideUpEnter() },
                exitTransition = { slideDownExit() },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideDownExit() }
            ) { backStackEntry ->
                val habitDetail: Screen.HabitDetail = backStackEntry.toRoute()
                HabitDetailsRoute(
                    onBack = { actions.popBackStack() },
                    onEdit = { actions.navigateToEdit(habitDetail.habitId) }
                )
            }

            composable<Screen.EditHabit>(
                enterTransition = { slideUpEnter() },
                exitTransition = { slideDownExit() }
            ) { backStackEntry ->
                val editHabit: Screen.EditHabit = backStackEntry.toRoute()
                EditHabitScreen(
                    onSaveSuccess = { actions.popBackStack() },
                    onBack = { actions.popBackStack() }
                )
            }

            composable<Screen.Settings>(
                enterTransition = { scaleEnter() },
                exitTransition = { scaleExit() },
                popEnterTransition = { scaleEnter() },
                popExitTransition = { scaleExit() }
            ) {
                SettingsScreen(onBack = { actions.popBackStack() })
            }

            composable<Screen.Premium>(
                enterTransition = { fadeEnter() },
                exitTransition = { fadeExit() }
            ) {
                PremiumPlanScreen(
                    onBack = { actions.popBackStack() }
                )
            }
        }
    }
}
