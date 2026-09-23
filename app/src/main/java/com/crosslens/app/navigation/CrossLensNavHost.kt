package com.crosslens.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crosslens.app.feature.comparison.CrossLensScreen
import com.crosslens.app.feature.editorial.EditorialReviewScreen
import com.crosslens.app.feature.explore.ExploreScreen
import com.crosslens.app.feature.home.HomeScreen
import com.crosslens.app.feature.settings.SettingsScreen
import com.crosslens.app.feature.story.StoryScreen

@Composable
fun CrossLensNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = CrossLensDestination.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(CrossLensDestination.Home.route) {
            HomeScreen(
                onStoryClick = { storyId ->
                    navController.navigate(CrossLensDestination.Story.createRoute(storyId))
                },
                onExploreClick = {
                    navController.navigate(CrossLensDestination.Explore.route)
                },
                onSettingsClick = {
                    navController.navigate(CrossLensDestination.Settings.route)
                }
            )
        }

        composable(
            route = CrossLensDestination.Story.route,
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            StoryScreen(
                storyId = storyId,
                onBackClick = { navController.popBackStack() },
                onCompareClick = { sid ->
                    navController.navigate(CrossLensDestination.CrossLens.createRoute(sid))
                }
            )
        }

        composable(
            route = CrossLensDestination.CrossLens.route,
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            CrossLensScreen(
                storyId = storyId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(CrossLensDestination.Explore.route) {
            ExploreScreen(
                onStoryClick = { storyId ->
                    navController.navigate(CrossLensDestination.Story.createRoute(storyId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(CrossLensDestination.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onEditorialReviewClick = {
                    navController.navigate(CrossLensDestination.EditorialReview.route)
                }
            )
        }

        composable(CrossLensDestination.EditorialReview.route) {
            EditorialReviewScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
