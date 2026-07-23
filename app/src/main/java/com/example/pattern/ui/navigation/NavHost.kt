package com.example.pattern.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.pattern.ui.screens.addHabitScreen.AddHabitScreen
import com.example.pattern.ui.screens.addHabitScreen.EditHabitScreen
import com.example.pattern.ui.screens.homeScreen.HomeScreen
import com.example.pattern.ui.screens.homeScreen.components.HabitListScreen
import com.example.pattern.ui.screens.homeScreen.habitCardDetailScreen.HabitDetailsRoute
import com.example.pattern.ui.screens.profileScreen.ProfileScreen
import com.example.pattern.ui.screens.settings.LanguageSelectionScreen
import com.example.pattern.ui.screens.settings.SettingsScreen
import com.example.pattern.ui.screens.settings.ThemeSelectionScreen
import com.example.pattern.utils.PremiumPlanScreen

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isPro: Boolean = false,
    startDestination: Any = Destination.Home,
    onUiReady: () -> Unit = {},
    onOnboardingFinish: () -> Unit = {}
) {
    val actions = remember(navController) { NavActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Destination.Onboarding> {
            com.example.pattern.ui.screens.onboarding.OnboardingScreen(
                onFinish = onOnboardingFinish
            )
        }

        composable<Destination.Home> {
            HomeScreen(
                onOpenMenuScreen = { actions.navigateToHabitList() },
                onSettingsClick = { actions.navigateToSettings() },
                onHabitClick = { id -> actions.navigateToDetail(id) },
                onPremiumClick = { actions.navigateToPremium() },
                onHomeReady = onUiReady
            )
        }

        composable<Destination.HabitList>(
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

        composable<Destination.Add>(
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

        composable<Destination.Profile> {
            ProfileScreen(
                onOpenMenuSheet = { actions.navigateToHabitList() },
                onOpenSettings = { actions.navigateToSettings() },
                onPremiumClick = { actions.navigateToPremium() }
            )
        }

        composable<Destination.HabitDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Destination.HabitDetail>()
            HabitDetailsRoute(
                onBack = { actions.popBackStack() },
                onEdit = { actions.navigateToEdit(detail.habitId) }
            )
        }

        composable<Destination.EditHabit>(
            enterTransition = { slideUpEnter() },
            exitTransition = { slideDownExit() }
        ) {
            EditHabitScreen(
                onSaveSuccess = { actions.popBackStack() },
                onBack = { actions.popBackStack() }
            )
        }

        composable<Destination.Settings>(
            enterTransition = { scaleEnter() },
            exitTransition = { scaleExit() },
            popEnterTransition = { scaleEnter() },
            popExitTransition = { scaleExit() }
        ) {
            SettingsScreen(
                onBack = { actions.popBackStack() },
                onThemeClick = { actions.navigateToThemeSelection() },
                onLanguageClick = { actions.navigateToLanguageSelection() }
            )
        }

        composable<Destination.ThemeSelection>(
            enterTransition = { scaleEnter() },
            exitTransition = { scaleExit() },
            popEnterTransition = { scaleEnter() },
            popExitTransition = { scaleExit() }
        ) {
            ThemeSelectionScreen(onBack = { actions.popBackStack() })
        }

        composable<Destination.LanguageSelection>(
            enterTransition = { scaleEnter() },
            exitTransition = { scaleExit() },
            popEnterTransition = { scaleEnter() },
            popExitTransition = { scaleExit() }
        ) {
            LanguageSelectionScreen(onBack = { actions.popBackStack() })
        }

        composable<Destination.Premium>(
            enterTransition = { fadeEnter() },
            exitTransition = { fadeExit() }
        ) {
            PremiumPlanScreen(
                onPurchase = { actions.popBackStack() }
            )
        }
    }
}
