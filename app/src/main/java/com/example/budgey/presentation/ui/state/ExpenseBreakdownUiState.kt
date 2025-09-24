package com.example.budgey.presentation.ui.state

import com.example.budgey.domain.usecase.CategoryBreakdown
import com.example.budgey.domain.usecase.ExpenseBreakdown
import com.example.budgey.domain.usecase.MonthlyBreakdown
import com.example.budgey.domain.usecase.OverallTotals

/**
 * UI state for Expense Breakdown screen with comprehensive state management
 */
data class ExpenseBreakdownUiState(
    // Loading states
    val isLoading: Boolean = false,
    // Data states
    val expenseBreakdown: ExpenseBreakdown? = null,
    val monthlyBreakdowns: List<MonthlyBreakdown> = emptyList(),
    val overallTotals: OverallTotals? = null,
    val selectedMonthBreakdown: MonthlyBreakdown? = null,
    // Error states
    val errorMessage: String? = null,
    // Chart data states
    val isChartDataReady: Boolean = false,
    // Pagination states
    val pageNo: Int = 1,
    val pageSize: Int = 10,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val totalPages: Int = 0,
    val totalItems: Int = 0
)