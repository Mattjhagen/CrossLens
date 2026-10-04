package com.crosslens.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.crosslens.app.feature.diagnostics.EventIntegrityScreen

/**
 * DEBUG-ONLY navigation extensions.
 */
fun NavGraphBuilder.addDebugDestinations(onBackClick: () -> Unit) {
    composable(CrossLensDestination.EventIntegrity.route) {
        EventIntegrityScreen(onBackClick = onBackClick)
    }
}
