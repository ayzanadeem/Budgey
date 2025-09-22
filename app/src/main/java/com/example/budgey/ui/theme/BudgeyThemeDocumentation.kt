package com.example.budgey.ui.theme

/**
 * Budgey Theme System Documentation
 *
 * A comprehensive Material3 theme system for the Budgey expense tracking app
 * featuring soothing venice blues and lilacs with smooth animations and
 * accessibility considerations.
 *
 * ## Usage
 *
 * Wrap your app content with BudgeyTheme:
 * ```kotlin
 * BudgeyTheme {
 *     // Your app content
 * }
 * ```
 *
 * Access extended colors:
 * ```kotlin
 * @Composable
 * fun MyComponent() {
 *     val extendedColors = MaterialTheme.budgeyColors
 *     Box(backgroundColor = extendedColors.success)
 * }
 * ```
 *
 * ## Color System
 *
 * ### Primary Colors
 * - Light: Deep lilac (#9575CD) with mist lilac container
 * - Dark: Soft lilac (#B19CD9) with dark lilac container
 *
 * ### Secondary Colors
 * - Light: Venice blue (#7986CB) with light venice container
 * - Dark: Light venice (#A5B4FC) with dark venice container
 *
 * ### Extended Colors
 * - Success: Income green (#10B981 / #34D399)
 * - Error: Expense red (#EF4444 / #FF6B6B)
 * - Warning: Amber (#F59E0B / #FBBF24)
 * - Chart colors: 10 harmonious colors for data visualization
 *
 * ## Typography Scale
 *
 * ### Display Styles
 * - displayLarge: 57sp (Hero text)
 * - displayMedium: 45sp (Large headers)
 * - displaySmall: 36sp (Section headers)
 *
 * ### Headline Styles
 * - headlineLarge: 32sp SemiBold (Page titles)
 * - headlineMedium: 28sp SemiBold (Card titles)
 * - headlineSmall: 24sp SemiBold (Sub-titles)
 *
 * ### Body Text
 * - bodyLarge: 16sp (Main content)
 * - bodyMedium: 14sp (Secondary content)
 * - bodySmall: 12sp (Captions)
 *
 * ### Extended Typography
 * - Amount display: Bold, optimized for numbers
 * - Input labels: Medium weight, readable
 * - Error messages: Appropriate styling for alerts
 *
 * ## Spacing System
 *
 * Based on 4dp grid system:
 * - xs: 4dp, sm: 8dp, md: 12dp, lg: 16dp
 * - xl: 20dp, xxl: 24dp, xxxl: 32dp
 *
 * Component-specific spacing:
 * - Screen padding: 16dp
 * - Card padding: 16dp
 * - Button padding: 24dp x 12dp
 * - Section spacing: 24dp
 *
 * ## Component Library
 *
 * ### Buttons
 * - BudgeyPrimaryButton: Main action button with animations
 * - BudgeySecondaryButton: Secondary outlined button
 * - BudgeyTextButton: Text-only button
 * - BudgeyIconButton: Icon-only button with scaling
 *
 * Features:
 * - Press animations (96% scale)
 * - Loading states
 * - Disabled states
 * - Consistent elevation and radius
 *
 * ### Input Fields
 * - BudgeyTextField: Filled text field with focus animations
 * - BudgeyOutlinedTextField: Outlined variant
 * - BudgeySearchField: Search-specific field
 *
 * Features:
 * - Focus scaling (102%)
 * - Animated border colors
 * - Error state handling
 * - Icon support
 *
 * ### Loading Indicators
 * - BudgeyLoadingIndicator: Spinning arc indicator
 * - BudgeyPulsingLoadingIndicator: Pulsing circle
 * - BudgeyDotLoadingIndicator: Three-dot animation
 * - BudgeySkeletonLoader: Content placeholder
 * - BudgeyProgressIndicator: Linear/circular progress
 *
 * Features:
 * - Smooth animations
 * - Multiple variants for different contexts
 * - Customizable colors and sizes
 *
 * ### Message Components
 * - BudgeySuccessMessage: Success notifications
 * - BudgeyErrorMessage: Error alerts with retry
 * - BudgeyWarningMessage: Warning notifications
 * - BudgeyInfoMessage: Information messages
 *
 * Features:
 * - Slide-in animations
 * - Auto-dismiss options
 * - Action button support
 * - Semantic color coding
 *
 * ## Animation System
 *
 * ### Duration Constants
 * - Fast: 150ms (Button interactions)
 * - Medium: 300ms (State changes)
 * - Slow: 500ms (Complex transitions)
 * - Loading: 1000ms (Rotation cycles)
 *
 * ### Animation Types
 * - Scale animations for press feedback
 * - Color transitions for state changes
 * - Slide animations for messages
 * - Rotation for loading indicators
 *
 * ## Accessibility Features
 *
 * ### Color Contrast
 * - All color combinations meet WCAG AA standards
 * - High contrast ratios for text readability
 * - Clear visual hierarchy
 *
 * ### Touch Targets
 * - Minimum 48dp touch targets
 * - Adequate spacing between interactive elements
 * - Clear focus indicators
 *
 * ### Screen Reader Support
 * - Semantic content descriptions
 * - State announcements
 * - Navigation landmarks
 *
 * ## Dark Theme Support
 *
 * Automatic theme switching based on system preference:
 * - Darker background colors for reduced eye strain
 * - Adjusted contrast ratios
 * - Inverted color hierarchies where appropriate
 *
 * ## Usage Examples
 *
 * ### Basic Button Usage
 * ```kotlin
 * BudgeyPrimaryButton(
 *     onClick = { /* action */ },
 *     text = "Save Expense",
 *     icon = Icons.Default.Save,
 *     loading = isLoading
 * )
 * ```
 *
 * ### Input Field with Validation
 * ```kotlin
 * BudgeyTextField(
 *     value = amount,
 *     onValueChange = { amount = it },
 *     label = "Amount",
 *     isError = amountError != null,
 *     errorMessage = amountError,
 *     leadingIcon = Icons.Default.AttachMoney
 * )
 * ```
 *
 * ### Success Message
 * ```kotlin
 * BudgeySuccessMessage(
 *     message = "Expense saved successfully!",
 *     visible = showSuccess,
 *     onDismiss = { showSuccess = false }
 * )
 * ```
 *
 * ### Loading State
 * ```kotlin
 * if (isLoading) {
 *     BudgeyFullScreenLoader(
 *         message = "Saving expense..."
 *     )
 * }
 * ```
 *
 * ## Performance Considerations
 *
 * - Animations use hardware acceleration
 * - Color calculations are optimized
 * - Components use composition locals for efficient theme access
 * - Minimal recomposition through stable parameters
 *
 * ## Future Enhancements
 *
 * - Custom font loading support
 * - Additional animation variants
 * - More chart color schemes
 * - Enhanced accessibility features
 * - Localization support for typography
 */
