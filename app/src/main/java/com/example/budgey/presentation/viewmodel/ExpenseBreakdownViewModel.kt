package com.example.budgey.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgey.domain.usecase.GetExpenseBreakdownUseCase
import com.example.budgey.presentation.ui.state.ExpenseBreakdownUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Expense Breakdown screen following MVVM with clean architecture
 * Handles UI state management and data loading
 */
@HiltViewModel
class ExpenseBreakdownViewModel @Inject constructor(
    private val getExpenseBreakdownUseCase: GetExpenseBreakdownUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseBreakdownUiState())
    val uiState: StateFlow<ExpenseBreakdownUiState> = _uiState.asStateFlow()

    init {
        loadExpenseBreakdown()
    }

    /**
     * Loads expense breakdown with loading state management
     */
    fun loadExpenseBreakdown() {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not authenticated",
                isLoading = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            getExpenseBreakdownUseCase(currentUserId)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load expense breakdown: ${exception.message}",
                    )
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { breakdown ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                expenseBreakdown = breakdown,
                                errorMessage = null
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Failed to load expense breakdown"
                            )
                        }
                    )
                }
        }
    }

    /**
     * Refreshes expense breakdown data
     */
    fun refreshExpenseBreakdown() {
        loadExpenseBreakdown()
    }

    /**
     * Retries loading expense breakdown
     */
    fun retryLoadExpenseBreakdown() {
        loadExpenseBreakdown()
    }

    /**
     * Formats amount with comma separation for display
     */
    fun formatAmount(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        if (formatter is DecimalFormat) {
            formatter.applyPattern("#,##0.00")
        }
        return "PKR ${formatter.format(amount)}"
    }

    /**
     * Formats amount without currency for calculations
     */
    fun formatAmountOnly(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        if (formatter is DecimalFormat) {
            formatter.applyPattern("#,##0.00")
        }
        return formatter.format(amount)
    }

    /**
     * Formats percentage with one decimal place
     */
    fun formatPercentage(percentage: Double): String {
        val formatter = DecimalFormat("#0.0")
        return "${formatter.format(percentage)}%"
    }

    /**
     * Gets formatted month display name
     */
    fun formatMonthDisplay(monthKey: String): String {
        return try {
            val parts = monthKey.split("-")
            if (parts.size == 2) {
                val year = parts[0]
                val month = parts[1].toInt()
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year.toInt())
                calendar.set(Calendar.MONTH, month - 1)
                val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                "$monthName $year"
            } else {
                monthKey
            }
        } catch (e: Exception) {
            monthKey
        }
    }

    /**
     * Gets summary statistics for display
     */
    fun getSummaryStats(): Triple<String, String, String> {
        val breakdown = _uiState.value.expenseBreakdown
        return if (breakdown != null) {
            val totalExpenses = breakdown.monthlyBreakdowns.sumOf {
                it.categories.sumOf { category -> category.totalAmount }
            }
            Triple(
                formatAmount(totalExpenses),
                "PKR 0.00", // No income tracking in current design
                formatAmount(-totalExpenses) // Net is negative for expenses
            )
        } else {
            Triple("PKR 0.00", "PKR 0.00", "PKR 0.00")
        }
    }

    /**
     * Gets top spending category
     */
    fun getTopSpendingCategory(): String {
        val breakdown = _uiState.value.expenseBreakdown
        return if (breakdown != null) {
            val categoryTotals = mutableMapOf<String, Double>()
            breakdown.monthlyBreakdowns.forEach { month ->
                month.categories.forEach { category ->
                    categoryTotals[category.categoryName] =
                        (categoryTotals[category.categoryName] ?: 0.0) + category.totalAmount
                }
            }
            categoryTotals.maxByOrNull { it.value }?.key ?: "No data"
        } else {
            "No data"
        }
    }

    /**
     * Gets average monthly spending
     */
    fun getAverageMonthlySpending(): String {
        val breakdown = _uiState.value.expenseBreakdown
        return if (breakdown != null && breakdown.monthlyBreakdowns.isNotEmpty()) {
            val totalSpending = breakdown.monthlyBreakdowns.sumOf {
                it.categories.sumOf { category -> category.totalAmount }
            }
            val averageSpending = totalSpending / breakdown.monthlyBreakdowns.size
            formatAmount(averageSpending)
        } else {
            "PKR 0.00"
        }
    }

    /**
     * Gets expense count for a month
     */
    fun getMonthExpenseCount(monthKey: String): Int {
        return _uiState.value.expenseBreakdown?.monthlyBreakdowns
            ?.find { it.monthKey == monthKey }
            ?.categories?.sumOf { it.expenseCount } ?: 0
    }

    /**
     * Gets category count for a month
     */
    fun getMonthCategoryCount(monthKey: String): Int {
        return _uiState.value.expenseBreakdown?.monthlyBreakdowns
            ?.find { it.monthKey == monthKey }
            ?.categories?.size ?: 0
    }

    /**
     * Clears all error states
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}
