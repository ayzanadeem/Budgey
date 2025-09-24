package com.example.budgey.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations in the Budgey app
 */
sealed class BudgeyDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    // Auth destinations
    object Login : BudgeyDestination(
        route = "login",
        title = "Login",
        icon = Icons.Default.AccountCircle
    )

    // Main tab destinations
    object NewEntry : BudgeyDestination(
        route = "new_entry",
        title = "New Entry",
        icon = Icons.Default.Add,
        selectedIcon = Icons.Default.Add
    )

    object MyExpenses : BudgeyDestination(
        route = "my_expenses",
        title = "My Expenses",
        icon = Icons.AutoMirrored.Filled.List,
        selectedIcon = Icons.AutoMirrored.Filled.List
    )

    object NewCategory : BudgeyDestination(
        route = "new_category",
        title = "New Category",
        icon = Icons.Default.Add,
        selectedIcon = Icons.Filled.Add
    )

    object More : BudgeyDestination(
        route = "more",
        title = "More",
        icon = Icons.AutoMirrored.Default.List,
        selectedIcon = Icons.AutoMirrored.Filled.List
    )

    companion object {
        /**
         * Get all main tab destinations for bottom navigation
         */
        val mainTabDestinations = listOf(
            NewEntry,
            MyExpenses,
            NewCategory,
            More
        )

        /**
         * Check if route is a main tab destination with comprehensive null safety
         */
        fun isMainTabDestination(route: String?): Boolean {
            return try {
                if (route.isNullOrBlank()) return false

                // Ensure mainTabDestinations is not null and not empty
                if (mainTabDestinations.isEmpty()) return false

                // Safe iteration with explicit null checks
                mainTabDestinations.forEach { destination ->
                    if (destination != null && destination.route == route) {
                        return true
                    }
                }
                false
            } catch (e: Exception) {
                // Log the error for debugging but don't crash the app
                println("Error in isMainTabDestination: ${e.message}")
                false
            }
        }
    }
}
