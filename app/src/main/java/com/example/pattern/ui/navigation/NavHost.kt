package com.example.pattern.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
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
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route,
        modifier = modifier
    ) {
        composable(Screens.Home.route) {
            HomeScreen(
                navController = navController,
                onOpenMenuScreen = { navController.navigate(Screens.HabitList.route) },
                onPremiumClick = { navController.navigate(Screens.Premium.route) }
            )
        }
        composable(
            route = Screens.Settings.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screens.Premium.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            PremiumPlanScreen(
                onPurchase = {
                    // Handle billing then pop
                    navController.popBackStack()
                }
            ) }
        composable(
            route = Screens.HabitList.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.9f, animationSpec = tween(400))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.9f, animationSpec = tween(400))
            }
        ) {
            HabitListScreen(
                onHabitClick = { id ->
                    navController.navigate(Screens.HabitDetail.createRoute(id))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screens.Add.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.9f, animationSpec = tween(400))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.9f, animationSpec = tween(400))
            }
        ) {
            AddHabitScreen(
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screens.Profile.route) {
            // Pass the state and the click action here
            ProfileScreen(
                isPro = isPro,
                onOpenMenuSheet = { navController.navigate(Screens.HabitList.route) },
                onOpenSettings = { navController.navigate(Screens.Settings.route) },
                onPremiumClick = { navController.navigate(Screens.Premium.route) }
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
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screens.EditHabit.createRoute(habitId)) }
            )
        }
        composable(
            route = Screens.EditHabit.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.IntType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) {
            EditHabitScreen(
                onSaveSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screens.Settings.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.9f, animationSpec = tween(400))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.9f, animationSpec = tween(400))
            }
        ) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}