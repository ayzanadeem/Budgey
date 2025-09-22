package com.example.budgey.ui.theme

import androidx.compose.ui.graphics.Color

// Extended color system for Budgey app
object BudgeyColors {

    // Primary brand colors - Soft lilacs and venice blues
    object Light {
        val primary = Color(0xFF9575CD)           // Deep lilac
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFE1BEE7)  // Mist lilac
        val onPrimaryContainer = Color(0xFF4A148C)

        val secondary = Color(0xFF7986CB)         // Venice blue
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFA5B4FC) // Light venice
        val onSecondaryContainer = Color(0xFF1A237E)

        val tertiary = Color(0xFFB19CD9)          // Soft lilac
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFE8DEF8)
        val onTertiaryContainer = Color(0xFF371E73)

        val background = Color(0xFFFDFBFF)        // Light background
        val onBackground = Color(0xFF1D1B20)
        val surface = Color(0xFFFDFBFF)
        val onSurface = Color(0xFF1D1B20)
        val surfaceVariant = Color(0xFFF5F3FF)    // Soft gray
        val onSurfaceVariant = Color(0xFF4A4458)  // Dark gray

        val outline = Color(0xFF7A757F)
        val outlineVariant = Color(0xFFCAC4D0)
        val scrim = Color(0xFF000000)

        val error = Color(0xFFEF4444)             // Expense red
        val onError = Color(0xFFFFFFFF)
        val errorContainer = Color(0xFFFEF2F2)
        val onErrorContainer = Color(0xFF7F1D1D)

        // Custom semantic colors
        val success = Color(0xFF10B981)           // Income green
        val onSuccess = Color(0xFFFFFFFF)
        val successContainer = Color(0xFFECFDF5)
        val onSuccessContainer = Color(0xFF064E3B)

        val warning = Color(0xFFF59E0B)           // Warning amber
        val onWarning = Color(0xFFFFFFFF)
        val warningContainer = Color(0xFFFEF3C7)
        val onWarningContainer = Color(0xFF78350F)

        // Special UI colors
        val income = Color(0xFF10B981)
        val expense = Color(0xFFEF4444)
        val neutral = Color(0xFF6B7280)

        // Gradient colors for special effects
        val gradientStart = Color(0xFFB19CD9)
        val gradientEnd = Color(0xFF7986CB)

        // Chart colors for expense breakdown
        val chartColors = listOf(
            Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFFB19CD9),
            Color(0xFFA5B4FC), Color(0xFFE1BEE7), Color(0xFFE8DEF8),
            Color(0xFF81C784), Color(0xFFFFB74D), Color(0xFFFF8A65),
            Color(0xFF90A4AE)
        )
    }

    object Dark {
        val primary = Color(0xFFB19CD9)           // Soft lilac (lighter in dark)
        val onPrimary = Color(0xFF371E73)
        val primaryContainer = Color(0xFF6A4C93)  // Dark lilac
        val onPrimaryContainer = Color(0xFFE1BEE7)

        val secondary = Color(0xFFA5B4FC)         // Light venice (lighter in dark)
        val onSecondary = Color(0xFF1A237E)
        val secondaryContainer = Color(0xFF3F51B5) // Dark venice
        val onSecondaryContainer = Color(0xFFA5B4FC)

        val tertiary = Color(0xFFE1BEE7)
        val onTertiary = Color(0xFF4A148C)
        val tertiaryContainer = Color(0xFF512DA8)  // Deep purple
        val onTertiaryContainer = Color(0xFFE8DEF8)

        val background = Color(0xFF1A1625)        // Dark background
        val onBackground = Color(0xFFE6E0E9)
        val surface = Color(0xFF1A1625)
        val onSurface = Color(0xFFE6E0E9)
        val surfaceVariant = Color(0xFF2D2438)    // Dark surface
        val onSurfaceVariant = Color(0xFFCAC4D0)

        val outline = Color(0xFF948F9A)
        val outlineVariant = Color(0xFF49454F)
        val scrim = Color(0xFF000000)

        val error = Color(0xFFFF6B6B)
        val onError = Color(0xFF7F1D1D)
        val errorContainer = Color(0xFF7F1D1D)
        val onErrorContainer = Color(0xFFFFEBEE)

        // Custom semantic colors
        val success = Color(0xFF34D399)
        val onSuccess = Color(0xFF064E3B)
        val successContainer = Color(0xFF064E3B)
        val onSuccessContainer = Color(0xFFECFDF5)

        val warning = Color(0xFFFBBF24)
        val onWarning = Color(0xFF78350F)
        val warningContainer = Color(0xFF78350F)
        val onWarningContainer = Color(0xFFFEF3C7)

        // Special UI colors
        val income = Color(0xFF34D399)
        val expense = Color(0xFFFF6B6B)
        val neutral = Color(0xFF9CA3AF)

        // Gradient colors for special effects
        val gradientStart = Color(0xFF6A4C93)
        val gradientEnd = Color(0xFF3F51B5)

        // Chart colors for expense breakdown (darker variants)
        val chartColors = listOf(
            Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFFB19CD9),
            Color(0xFF81C784), Color(0xFFFFB74D), Color(0xFFFF8A65),
            Color(0xFF90A4AE), Color(0xFFBA68C8), Color(0xFF64B5F6),
            Color(0xFFA1887F)
        )
    }
}

// Extended color tokens for special use cases
data class BudgeyExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val income: Color,
    val expense: Color,
    val neutral: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val chartColors: List<Color>
)

val lightExtendedColors = BudgeyExtendedColors(
    success = BudgeyColors.Light.success,
    onSuccess = BudgeyColors.Light.onSuccess,
    successContainer = BudgeyColors.Light.successContainer,
    onSuccessContainer = BudgeyColors.Light.onSuccessContainer,
    warning = BudgeyColors.Light.warning,
    onWarning = BudgeyColors.Light.onWarning,
    warningContainer = BudgeyColors.Light.warningContainer,
    onWarningContainer = BudgeyColors.Light.onWarningContainer,
    income = BudgeyColors.Light.income,
    expense = BudgeyColors.Light.expense,
    neutral = BudgeyColors.Light.neutral,
    gradientStart = BudgeyColors.Light.gradientStart,
    gradientEnd = BudgeyColors.Light.gradientEnd,
    chartColors = BudgeyColors.Light.chartColors
)

val darkExtendedColors = BudgeyExtendedColors(
    success = BudgeyColors.Dark.success,
    onSuccess = BudgeyColors.Dark.onSuccess,
    successContainer = BudgeyColors.Dark.successContainer,
    onSuccessContainer = BudgeyColors.Dark.onSuccessContainer,
    warning = BudgeyColors.Dark.warning,
    onWarning = BudgeyColors.Dark.onWarning,
    warningContainer = BudgeyColors.Dark.warningContainer,
    onWarningContainer = BudgeyColors.Dark.onWarningContainer,
    income = BudgeyColors.Dark.income,
    expense = BudgeyColors.Dark.expense,
    neutral = BudgeyColors.Dark.neutral,
    gradientStart = BudgeyColors.Dark.gradientStart,
    gradientEnd = BudgeyColors.Dark.gradientEnd,
    chartColors = BudgeyColors.Dark.chartColors
)

// Legacy colors (keeping for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)