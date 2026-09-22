package com.crosslens.app.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TealAccent,
    onPrimary = IvoryBackground,
    primaryContainer = LightGray,
    onPrimaryContainer = InkPrimary,

    secondary = WarmGray,
    onSecondary = IvoryBackground,
    secondaryContainer = LightGray,
    onSecondaryContainer = InkPrimary,

    tertiary = TealAccentVariant,
    onTertiary = IvoryBackground,

    error = ErrorLight,
    onError = IvoryBackground,

    background = IvoryBackground,
    onBackground = InkPrimary,

    surface = IvorySurface,
    onSurface = InkPrimary,
    surfaceVariant = LightGray,
    onSurfaceVariant = WarmGray,

    outline = WarmGray,
    outlineVariant = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = TealAccentDark,
    onPrimary = CharcoalBackground,
    primaryContainer = DarkGray,
    onPrimaryContainer = WarmLight,

    secondary = WarmGrayDark,
    onSecondary = CharcoalBackground,
    secondaryContainer = DarkGray,
    onSecondaryContainer = WarmLight,

    tertiary = TealAccentDarkVariant,
    onTertiary = CharcoalBackground,

    error = ErrorDark,
    onError = CharcoalBackground,

    background = CharcoalBackground,
    onBackground = WarmLight,

    surface = CharcoalSurface,
    onSurface = WarmLight,
    surfaceVariant = DarkGray,
    onSurfaceVariant = WarmGrayDark,

    outline = WarmGrayDark,
    outlineVariant = DarkGray
)

@Composable
fun CrossLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CrossLensTypography,
        shapes = CrossLensShapes,
        content = content
    )
}
