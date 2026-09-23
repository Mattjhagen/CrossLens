package com.crosslens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosslens.app.core.model.Theme
import com.crosslens.app.core.ui.theme.CrossLensTheme
import com.crosslens.app.navigation.CrossLensNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()

            val darkTheme = when (preferences?.theme) {
                Theme.SYSTEM -> systemDarkTheme
                Theme.LIGHT -> false
                Theme.DARK -> true
                null -> systemDarkTheme
            }

            CrossLensTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrossLensNavHost()
                }
            }
        }
    }
}
