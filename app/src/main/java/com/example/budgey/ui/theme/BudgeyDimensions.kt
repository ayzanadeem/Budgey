package com.example.budgey.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Consistent spacing system for Budgey app
object BudgeySpacing {

    // Base spacing unit (4dp)
    val base: Dp = 4.dp

    // Standard spacing scale
    val xxs: Dp = 2.dp      // 0.5x
    val xs: Dp = 4.dp       // 1x
    val sm: Dp = 8.dp       // 2x
    val md: Dp = 12.dp      // 3x
    val lg: Dp = 16.dp      // 4x
    val xl: Dp = 20.dp      // 5x
    val xxl: Dp = 24.dp     // 6x
    val xxxl: Dp = 32.dp    // 8x

    // Component-specific spacing
    val buttonPaddingHorizontal: Dp = 24.dp
    val buttonPaddingVertical: Dp = 12.dp
    val cardPadding: Dp = 16.dp
    val screenPadding: Dp = 16.dp
    val listItemPadding: Dp = 16.dp
    val inputFieldPadding: Dp = 16.dp

    // Layout spacing
    val sectionSpacing: Dp = 24.dp
    val componentSpacing: Dp = 16.dp
    val elementSpacing: Dp = 8.dp
    val tightSpacing: Dp = 4.dp
}

// Elevation system
object BudgeyElevation {
    val none: Dp = 0.dp
    val xs: Dp = 1.dp
    val sm: Dp = 2.dp
    val md: Dp = 4.dp
    val lg: Dp = 6.dp
    val xl: Dp = 8.dp
    val xxl: Dp = 12.dp
    val xxxl: Dp = 16.dp

    // Component-specific elevations
    val card: Dp = 2.dp
    val button: Dp = 2.dp
    val fab: Dp = 6.dp
    val dialog: Dp = 8.dp
    val bottomSheet: Dp = 8.dp
    val appBar: Dp = 4.dp
}

// Border radius system
object BudgeyRadius {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val full: Dp = 9999.dp

    // Component-specific radius
    val button: Dp = 12.dp
    val card: Dp = 16.dp
    val inputField: Dp = 12.dp
    val chip: Dp = 20.dp
    val dialog: Dp = 20.dp
}

// Animation durations (in milliseconds)
object BudgeyDuration {
    const val fast = 150
    const val medium = 300
    const val slow = 500
    const val extra_slow = 700

    // Component-specific durations
    const val button = 150
    const val card = 300
    const val dialog = 300
    const val navigation = 300
    const val loading = 1000
}

// Size constants
object BudgeySize {
    // Icon sizes
    val iconXs: Dp = 12.dp
    val iconSm: Dp = 16.dp
    val iconMd: Dp = 24.dp
    val iconLg: Dp = 32.dp
    val iconXl: Dp = 48.dp
    val iconXxl: Dp = 64.dp

    // Button heights
    val buttonSmall: Dp = 32.dp
    val buttonMedium: Dp = 40.dp
    val buttonLarge: Dp = 48.dp

    // Input field heights
    val inputField: Dp = 56.dp
    val inputFieldCompact: Dp = 48.dp

    // Component sizes
    val chipHeight: Dp = 32.dp
    val cardMinHeight: Dp = 80.dp
    val listItemHeight: Dp = 64.dp
    val appBarHeight: Dp = 64.dp
    val fabSize: Dp = 56.dp
    val bottomNavHeight: Dp = 80.dp

    // Loading indicator sizes
    val loadingSmall: Dp = 16.dp
    val loadingMedium: Dp = 24.dp
    val loadingLarge: Dp = 32.dp
}
