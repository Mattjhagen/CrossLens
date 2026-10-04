package com.crosslens.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crosslens.app.BuildConfig
import com.crosslens.app.feature.articlenavigator.ArticleNavigatorScreen
import com.crosslens.app.feature.comparison.CrossLensScreen
import com.crosslens.app.feature.coveragedetails.CoverageDetailsScreen
import com.crosslens.app.feature.editorial.EditorialReviewScreen
import com.crosslens.app.feature.eventcomparison.EventComparisonScreen
import com.crosslens.app.feature.explore.ExploreScreen
import com.crosslens.app.feature.home.HomeScreen
import com.crosslens.app.feature.settings.SettingsScreen
import com.crosslens.app.feature.sourcedetail.SourceDetailScreen
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
                onEventClick = { storyId ->
                    navController.navigate(CrossLensDestination.EventComparison.createRoute(storyId))
                },
                onExploreClick = {
                    navController.navigate(CrossLensDestination.Explore.route)
                },
                onSettingsClick = {
                    navController.navigate(CrossLensDestination.Settings.route)
                },
                onArticleNavigatorClick = { storyId ->
                    navController.navigate(CrossLensDestination.ArticleNavigator.createRoute(storyId))
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
                },
                onArticleClick = { articleId ->
                    navController.navigate(CrossLensDestination.SourceDetail.createRoute(articleId))
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
                onBackClick = { navController.popBackStack() },
                onOpenSourceDetail = { articleId ->
                    navController.navigate(CrossLensDestination.SourceDetail.createRoute(articleId))
                }
            )
        }

        composable(
            route = CrossLensDestination.SourceDetail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            SourceDetailScreen(
                articleId = articleId,
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
                },
                onSourceHealthClick = {
                    navController.navigate(CrossLensDestination.SourceHealth.route)
                },
                onEventIntegrityClick = if (BuildConfig.DEBUG) {
                    { navController.navigate(CrossLensDestination.EventIntegrity.route) }
                } else {
                    null
                }
            )
        }

        composable(CrossLensDestination.EditorialReview.route) {
            EditorialReviewScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = CrossLensDestination.EventComparison.route,
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            EventComparisonScreen(
                storyId = storyId,
                onBackClick = { navController.popBackStack() },
                onCoverageDetailsClick = { sid ->
                    navController.navigate(CrossLensDestination.CoverageDetails.createRoute(sid))
                }
            )
        }

        composable(
            route = CrossLensDestination.CoverageDetails.route,
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            CoverageDetailsScreen(
                storyId = storyId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(CrossLensDestination.SourceHealth.route) {
            com.crosslens.app.feature.sourcehealth.SourceHealthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // DEBUG-ONLY destinations (implemented in debug/release source sets)
        addDebugDestinations(onBackClick = { navController.popBackStack() })

        composable(
            route = CrossLensDestination.ArticleNavigator.route,
            arguments = listOf(
                navArgument("storyId") { type = NavType.StringType },
                navArgument("articleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            val articleId = backStackEntry.arguments?.getString("articleId")
            ArticleNavigatorScreen(
                storyId = storyId,
                articleId = articleId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
