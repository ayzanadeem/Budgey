package com.example.budgey.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom color schemes using our extended color system
private val LightColorScheme = lightColorScheme(
    primary = BudgeyColors.Light.primary,
    onPrimary = BudgeyColors.Light.onPrimary,
    primaryContainer = BudgeyColors.Light.primaryContainer,
    onPrimaryContainer = BudgeyColors.Light.onPrimaryContainer,

    secondary = BudgeyColors.Light.secondary,
    onSecondary = BudgeyColors.Light.onSecondary,
    secondaryContainer = BudgeyColors.Light.secondaryContainer,
    onSecondaryContainer = BudgeyColors.Light.onSecondaryContainer,

    tertiary = BudgeyColors.Light.tertiary,
    onTertiary = BudgeyColors.Light.onTertiary,
    tertiaryContainer = BudgeyColors.Light.tertiaryContainer,
    onTertiaryContainer = BudgeyColors.Light.onTertiaryContainer,

    background = BudgeyColors.Light.background,
    onBackground = BudgeyColors.Light.onBackground,
    surface = BudgeyColors.Light.surface,
    onSurface = BudgeyColors.Light.onSurface,
    surfaceVariant = BudgeyColors.Light.surfaceVariant,
    onSurfaceVariant = BudgeyColors.Light.onSurfaceVariant,

    outline = BudgeyColors.Light.outline,
    outlineVariant = BudgeyColors.Light.outlineVariant,
    scrim = BudgeyColors.Light.scrim,

    error = BudgeyColors.Light.error,
    onError = BudgeyColors.Light.onError,
    errorContainer = BudgeyColors.Light.errorContainer,
    onErrorContainer = BudgeyColors.Light.onErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = BudgeyColors.Dark.primary,
    onPrimary = BudgeyColors.Dark.onPrimary,
    primaryContainer = BudgeyColors.Dark.primaryContainer,
    onPrimaryContainer = BudgeyColors.Dark.onPrimaryContainer,

    secondary = BudgeyColors.Dark.secondary,
    onSecondary = BudgeyColors.Dark.onSecondary,
    secondaryContainer = BudgeyColors.Dark.secondaryContainer,
    onSecondaryContainer = BudgeyColors.Dark.onSecondaryContainer,

    tertiary = BudgeyColors.Dark.tertiary,
    onTertiary = BudgeyColors.Dark.onTertiary,
    tertiaryContainer = BudgeyColors.Dark.tertiaryContainer,
    onTertiaryContainer = BudgeyColors.Dark.onTertiaryContainer,

    background = BudgeyColors.Dark.background,
    onBackground = BudgeyColors.Dark.onBackground,
    surface = BudgeyColors.Dark.surface,
    onSurface = BudgeyColors.Dark.onSurface,
    surfaceVariant = BudgeyColors.Dark.surfaceVariant,
    onSurfaceVariant = BudgeyColors.Dark.onSurfaceVariant,

    outline = BudgeyColors.Dark.outline,
    outlineVariant = BudgeyColors.Dark.outlineVariant,
    scrim = BudgeyColors.Dark.scrim,

    error = BudgeyColors.Dark.error,
    onError = BudgeyColors.Dark.onError,
    errorContainer = BudgeyColors.Dark.errorContainer,
    onErrorContainer = BudgeyColors.Dark.onErrorContainer
)

// CompositionLocal for extended colors
val LocalBudgeyExtendedColors = staticCompositionLocalOf { lightExtendedColors }

// Extension property to access extended colors
val MaterialTheme.budgeyColors: BudgeyExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalBudgeyExtendedColors.current

@Composable
fun BudgeyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to use our custom colors
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

    val extendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalBudgeyExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BudgeyTypography,
            content = content
        )
    }
}