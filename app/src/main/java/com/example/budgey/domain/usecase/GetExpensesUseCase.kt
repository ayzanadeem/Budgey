package com.example.budgey.domain.usecase

import com.example.budgey.data.model.Expense
import com.example.budgey.data.model.ExpenseType
import com.example.budgey.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class for monthly expense grouping
 */
data class MonthlyExpenses(
    val monthKey: String,
    val monthDisplayName: String,
    val expenses: List<Expense>,
    val totalExpenses: Double,
    val totalIncome: Double,
    val netAmount: Double
)

/**
 * Use case for fetching and grouping expenses by budget month
 * Follows single responsibility principle for expense retrieval and grouping
 */
@Singleton
class GetExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Fetches expenses for a user and groups them by budget month
     * @param userId The ID of the user whose expenses to fetch
     * @return Flow emitting Result with list of monthly expense groups
     */
    operator fun invoke(userId: String): Flow<Result<List<MonthlyExpenses>>> {
        // Input validation
        if (userId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(
                Result.failure(GetExpensesException.InvalidUserId)
            )
        }

        return expenseRepository.getExpenses(userId)
            .map { result ->
                result.fold(
                    onSuccess = { expenses ->
                        try {
                            val groupedExpenses = expenses.groupByBudgetMonth()
                            Result.success(groupedExpenses)
                        } catch (e: Exception) {
                            Result.failure(GetExpensesException.DataProcessingError(e.message ?: "Failed to process expenses"))
                        }
                    },
                    onFailure = { exception ->
                        Result.failure(mapExpenseException(exception))
                    }
                )
            }
            .catch { exception ->
                emit(Result.failure(mapExpenseException(exception)))
            }
    }

    /**
     * Fetches expenses for a user with pagination and groups them by budget month
     * @param userId The ID of the user whose expenses to fetch
     * @param pageNumber The page number (starting from 1)
     * @param pageSize The number of items per page (default 10)
     * @return Flow emitting Result with list of monthly expense groups
     */
    fun getPaginated(userId: String, pageNumber: Int = 1, pageSize: Int = 10): Flow<Result<List<MonthlyExpenses>>> {
        // Input validation
        if (userId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(
                Result.failure(GetExpensesException.InvalidUserId)
            )
        }

        if (pageNumber < 1) {
            return kotlinx.coroutines.flow.flowOf(
                Result.failure(GetExpensesException.InvalidPageNumber)
            )
        }

        if (pageSize < 1 || pageSize > 100) {
            return kotlinx.coroutines.flow.flowOf(
                Result.failure(GetExpensesException.InvalidPageSize)
            )
        }

        return expenseRepository.getExpensesPaginated(userId, pageNumber, pageSize)
            .map { result ->
                result.fold(
                    onSuccess = { expenses ->
                        try {
                            val groupedExpenses = expenses.groupByBudgetMonth()
                            Result.success(groupedExpenses)
                        } catch (e: Exception) {
                            Result.failure(GetExpensesException.DataProcessingError(e.message ?: "Failed to process expenses"))
                        }
                    },
                    onFailure = { exception ->
                        Result.failure(mapExpenseException(exception))
                    }
                )
            }
            .catch { exception ->
                emit(Result.failure(mapExpenseException(exception)))
            }
    }

    /**
     * Maps repository exceptions to use case specific exceptions
     */
    private fun mapExpenseException(exception: Throwable): GetExpensesException {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> {
                GetExpensesException.NetworkError
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                GetExpensesException.PermissionDenied
            }
            exception.message?.contains("timeout", ignoreCase = true) == true -> {
                GetExpensesException.RequestTimeout
            }
            else -> GetExpensesException.UnknownError(exception.message ?: "Failed to fetch expenses")
        }
    }
}

/**
 * Extension functions for data transformation
 */
fun List<Expense>.groupByBudgetMonth(): List<MonthlyExpenses> {
    return this.groupBy { it.budgetMonthKey }
        .map { (monthKey, expenses) ->
            expenses.toMonthlyExpenses(monthKey)
        }
        .sortedByDescending { it.monthKey } // Most recent first
}

fun List<Expense>.toMonthlyExpenses(monthKey: String): MonthlyExpenses {
    val totalExpenses = this.filter { it.type == ExpenseType.EXPENSE }.sumOf { it.amount }
    val totalIncome = this.filter { it.type == ExpenseType.INCOME }.sumOf { it.amount }
    val netAmount = totalIncome - totalExpenses

    return MonthlyExpenses(
        monthKey = monthKey,
        monthDisplayName = monthKey.toDisplayName(),
        expenses = this.sortedByDescending { it.createdAt?.toDate()?.time ?: 0 },
        totalExpenses = totalExpenses,
        totalIncome = totalIncome,
        netAmount = netAmount
    )
}

fun String.toDisplayName(): String {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = formatter.parse(this)
        val displayFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        displayFormatter.format(date!!)
    } catch (e: Exception) {
        this // Fallback to original string
    }
}

/**
 * Domain-specific exceptions for get expenses use case
 */
sealed class GetExpensesException(message: String) : Exception(message) {
    object InvalidUserId : GetExpensesException("User ID cannot be empty")
    object InvalidMonthKey : GetExpensesException("Month key must be in format yyyy-MM")
    object NetworkError : GetExpensesException("Network connection failed. Please check your internet connection")
    object PermissionDenied : GetExpensesException("You don't have permission to access these expenses")
    object RequestTimeout : GetExpensesException("Request timed out. Please try again")
    data class DataProcessingError(val details: String) : GetExpensesException("Failed to process expense data: $details")
    data class UnknownError(val details: String) : GetExpensesException("Failed to fetch expenses: $details")
    object InvalidPageNumber : GetExpensesException("Page number must be greater than 0")
    object InvalidPageSize : GetExpensesException("Page size must be between 1 and 100")
}
