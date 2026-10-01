package com.crosslens.app.navigation

sealed class CrossLensDestination(val route: String) {
    data object Home : CrossLensDestination("home")
    data object Explore : CrossLensDestination("explore")
    data object Settings : CrossLensDestination("settings")
    data object EditorialReview : CrossLensDestination("editorial_review")
    data object Story : CrossLensDestination("story/{storyId}") {
        fun createRoute(storyId: String) = "story/$storyId"
    }
    data object CrossLens : CrossLensDestination("crosslens/{storyId}") {
        fun createRoute(storyId: String) = "crosslens/$storyId"
    }
    data object SourceDetail : CrossLensDestination("sourcedetail/{articleId}") {
        fun createRoute(articleId: String) = "sourcedetail/$articleId"
    }
    data object EventComparison : CrossLensDestination("eventcomparison/{storyId}") {
        fun createRoute(storyId: String) = "eventcomparison/$storyId"
    }
    data object ArticleNavigator : CrossLensDestination("articlenavigator/{storyId}?articleId={articleId}") {
        fun createRoute(storyId: String, articleId: String? = null): String {
            return if (articleId != null) {
                "articlenavigator/$storyId?articleId=$articleId"
            } else {
                "articlenavigator/$storyId"
            }
        }
    }
}
