package com.example.pattern.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pattern.ui.screens.addHabitScreen.AddHabitScreen
import com.example.pattern.ui.screens.addHabitScreen.EditHabitScreen
import com.example.pattern.ui.screens.homeScreen.HomeScreen
import com.example.pattern.ui.screens.homeScreen.components.HabitListScreen
import com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen.HabitDetailsRoute
import com.example.pattern.ui.screens.profileScreen.ProfileScreen
import com.example.pattern.ui.screens.settings.SettingsScreen
import com.example.pattern.utils.PremiumPlanScreen

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isPro: Boolean = false,
    startDestination: String = Screens.Home.route,
    onOnboardingFinish: () -> Unit = {}
) {
    val actions = remember(navController) { NavActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screens.Onboarding.route) {
            com.example.pattern.ui.screens.onboarding.OnboardingScreen(
                onFinish = onOnboardingFinish
            )
        }

        composable(Screens.Home.route) {
            HomeScreen(
                onOpenMenuScreen = { actions.navigateToHabitList() },
                onSettingsClick = { actions.navigateToSettings() },
                onHabitClick = { id -> actions.navigateToDetail(id) },
                onPremiumClick = { actions.navigateToPremium() }
            )
        }

        composable(
            route = Screens.HabitList.route,
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

        composable(
            route = Screens.Add.route,
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

        composable(Screens.Profile.route) {
            ProfileScreen(
                onOpenMenuSheet = { actions.navigateToHabitList() },
                onOpenSettings = { actions.navigateToSettings() },
                onPremiumClick = { actions.navigateToPremium() }
            )
        }

        composable(
            route = Screens.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId") ?: 0
            HabitDetailsRoute(
                onBack = { actions.popBackStack() },
                onEdit = { actions.navigateToEdit(habitId) }
            )
        }

        composable(
            route = Screens.EditHabit.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.IntType }
            ),
            enterTransition = { slideUpEnter() },
            exitTransition = { slideDownExit() }
        ) {
            EditHabitScreen(
                onSaveSuccess = { actions.popBackStack() },
                onBack = { actions.popBackStack() }
            )
        }

        composable(
            route = Screens.Settings.route,
            enterTransition = { scaleEnter() },
            exitTransition = { scaleExit() },
            popEnterTransition = { scaleEnter() },
            popExitTransition = { scaleExit() }
        ) {
            SettingsScreen(onBack = { actions.popBackStack() })
        }

        composable(
            route = Screens.Premium.route,
            enterTransition = { fadeEnter() },
            exitTransition = { fadeExit() }
        ) {
            PremiumPlanScreen(
                onPurchase = { actions.popBackStack() }
            )
        }
    }
}
