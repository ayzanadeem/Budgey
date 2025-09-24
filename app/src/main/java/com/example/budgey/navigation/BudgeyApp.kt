package com.example.budgey.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgey.ui.theme.BudgeyTheme

/**
 * Main container for the Budgey app that handles the overall navigation structure
 * Includes bottom navigation for main tabs and manages navigation state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgeyApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = BudgeyDestination.Login.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if we should show the bottom navigation using direct string comparison
    val shouldShowBottomBar = currentRoute in listOf(
        "new_entry",
        "my_expenses",
        "new_category",
        "more"
    )

    BudgeyTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                if (shouldShowBottomBar) {
                    BudgeyBottomNavigation(navController = navController)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            BudgeyNavGraph(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Extension function to handle back button behavior in the main app
 */
fun NavHostController.navigateToMainTab(destination: BudgeyDestination) {
    navigate(destination.route) {
        // Pop up to the start destination of main tabs to avoid building up a large stack
        popUpTo(BudgeyDestination.NewEntry.route) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when reselecting
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}
