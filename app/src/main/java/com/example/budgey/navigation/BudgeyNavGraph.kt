package com.example.budgey.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.budgey.presentation.ui.screens.LoginScreen
import com.example.budgey.presentation.ui.screens.NewExpenseScreen
import com.example.budgey.presentation.ui.screens.ExpenseBreakdownScreen
import com.example.budgey.presentation.ui.screens.AddCategoryScreen
import com.example.budgey.presentation.ui.screens.MoreScreen

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

        composable(route = BudgeyDestination.More.route) {
            MoreScreen(
                onNavigateToLogin = {
                    navController.navigate(BudgeyDestination.Login.route) {
                        // Clear entire back stack when logging out
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
