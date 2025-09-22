package com.example.budgey.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.budgey.presentation.ui.screens.LoginScreen
import com.example.budgey.presentation.ui.screens.NewExpenseScreen

/**
 * Main navigation graph for the Budgey app
 * Handles navigation between all screens including auth and main app destinations
 */
@Composable
fun BudgeyNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = BudgeyDestination.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Authentication Flow
        composable(route = BudgeyDestination.Login.route) {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate(BudgeyDestination.NewEntry.route) {
                        // Clear the back stack when navigating to main
                        popUpTo(BudgeyDestination.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Main App Flow - Tab Destinations
        composable(route = BudgeyDestination.NewEntry.route) {
            NewExpenseScreen()
        }

        composable(route = BudgeyDestination.MyExpenses.route) {
            ExpenseBreakdownScreen()
        }

        composable(route = BudgeyDestination.NewCategory.route) {
            AddCategoryScreen()
        }
    }
}

// Placeholder screen composables - these will be replaced with actual implementations
@Composable
private fun ExpenseBreakdownScreen() {
    // TODO: Implement ExpenseBreakdownScreen using ExpenseBreakdownViewModel
    PlaceholderScreen(title = "My Expenses", description = "View your expense breakdown")
}

@Composable
private fun AddCategoryScreen() {
    // TODO: Implement AddCategoryScreen using AddCategoryViewModel
    PlaceholderScreen(title = "New Category", description = "Create expense categories")
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
