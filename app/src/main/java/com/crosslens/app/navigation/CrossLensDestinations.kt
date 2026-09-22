package com.crosslens.app.navigation

sealed class CrossLensDestination(val route: String) {
    data object Home : CrossLensDestination("home")
    data object Explore : CrossLensDestination("explore")
    data object Settings : CrossLensDestination("settings")
    data object Story : CrossLensDestination("story/{storyId}") {
        fun createRoute(storyId: String) = "story/$storyId"
    }
    data object CrossLens : CrossLensDestination("crosslens/{storyId}") {
        fun createRoute(storyId: String) = "crosslens/$storyId"
    }
}
