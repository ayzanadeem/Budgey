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
 * Data class for category breakdown within a month
 */
data class CategoryBreakdown(
    val categoryId: String,
    val categoryName: String,
    val expenses: List<Expense>,
    val totalAmount: Double,
    val expenseCount: Int,
    val averageAmount: Double,
    val percentage: Double
)

/**
 * Data class for monthly breakdown with categories
 */
data class MonthlyBreakdown(
    val monthKey: String,
    val monthDisplayName: String,
    val categories: List<CategoryBreakdown>,
    val totalExpenses: Double,
    val totalIncome: Double,
    val netAmount: Double,
    val expenseCount: Int,
    val incomeCount: Int
)

/**
 * Data class for complete expense breakdown
 */
data class ExpenseBreakdown(
    val monthlyBreakdowns: List<MonthlyBreakdown>,
    val overallTotals: OverallTotals
)

/**
 * Data class for overall totals across all months
 */
data class OverallTotals(
    val totalExpenses: Double,
    val totalIncome: Double,
    val netAmount: Double,
    val monthCount: Int,
    val averageMonthlyExpenses: Double,
    val averageMonthlyIncome: Double,
    val topExpenseCategory: String?,
    val topIncomeCategory: String?
)

/**
 * Use case for getting detailed expense breakdown grouped by month then category
 * Follows single responsibility principle for expense analysis
 */
@Singleton
class GetExpenseBreakdownUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Gets comprehensive expense breakdown grouped by month then category
     * @param userId The ID of the user whose expense breakdown to fetch
     * @param limitMonths Optional limit for number of recent months (default: 12)
     * @return Flow emitting Result with complete expense breakdown
     */
    operator fun invoke(userId: String, limitMonths: Int = 12): Flow<Result<ExpenseBreakdown>> {
        // Input validation
        if (userId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(
                Result.failure(ExpenseBreakdownException.InvalidUserId)
            )
        }

        if (limitMonths <= 0 || limitMonths > 24) {
            return kotlinx.coroutines.flow.flowOf(
                Result.failure(ExpenseBreakdownException.InvalidMonthLimit)
            )
        }

        return expenseRepository.getExpenses(userId)
            .map { result ->
                result.fold(
                    onSuccess = { expenses ->
                        try {
                            val breakdown = expenses.toExpenseBreakdown(limitMonths)
                            Result.success(breakdown)
                        } catch (e: Exception) {
                            Result.failure(ExpenseBreakdownException.DataProcessingError(e.message ?: "Failed to process breakdown"))
                        }
                    },
                    onFailure = { exception ->
                        Result.failure(mapBreakdownException(exception))
                    }
                )
            }
            .catch { exception ->
                emit(Result.failure(mapBreakdownException(exception)))
            }
    }

    /**
     * Gets breakdown for a specific month with detailed category analysis
     * @param userId The ID of the user
     * @param monthKey The month key in format "yyyy-MM"
     * @return Result with detailed monthly breakdown
     */
    suspend fun getMonthlyBreakdown(userId: String, monthKey: String): Result<MonthlyBreakdown> {
        // Input validation
        if (userId.isBlank()) {
            return Result.failure(ExpenseBreakdownException.InvalidUserId)
        }

        if (monthKey.isBlank() || !isValidMonthKey(monthKey)) {
            return Result.failure(ExpenseBreakdownException.InvalidMonthKey)
        }

        return try {
            val result = expenseRepository.getExpensesByMonth(userId, monthKey)
            result.fold(
                onSuccess = { expenses ->
                    val breakdown = expenses.toMonthlyBreakdown(monthKey)
                    Result.success(breakdown)
                },
                onFailure = { exception ->
                    Result.failure(mapBreakdownException(exception))
                }
            )
        } catch (e: Exception) {
            Result.failure(ExpenseBreakdownException.UnknownError(e.message ?: "Unknown error occurred"))
        }
    }

    /**
     * Maps repository exceptions to use case specific exceptions
     */
    private fun mapBreakdownException(exception: Throwable): ExpenseBreakdownException {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> {
                ExpenseBreakdownException.NetworkError
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                ExpenseBreakdownException.PermissionDenied
            }
            exception.message?.contains("timeout", ignoreCase = true) == true -> {
                ExpenseBreakdownException.RequestTimeout
            }
            else -> ExpenseBreakdownException.UnknownError(exception.message ?: "Failed to fetch breakdown")
        }
    }

    /**
     * Validates month key format (yyyy-MM)
     */
    private fun isValidMonthKey(monthKey: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            formatter.isLenient = false
            formatter.parse(monthKey)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Extension functions for expense breakdown data transformation
 */
fun List<Expense>.toExpenseBreakdown(limitMonths: Int): ExpenseBreakdown {
    // Group by month and take only the specified number of recent months
    val monthlyBreakdowns = this.groupBy { it.budgetMonthKey }
        .toSortedMap(reverseOrder()) // Most recent first
        .entries
        .take(limitMonths)
        .map { (monthKey, expenses) ->
            expenses.toMonthlyBreakdown(monthKey)
        }

    // Calculate overall totals
    val overallTotals = monthlyBreakdowns.toOverallTotals()

    return ExpenseBreakdown(
        monthlyBreakdowns = monthlyBreakdowns,
        overallTotals = overallTotals
    )
}

fun List<Expense>.toMonthlyBreakdown(monthKey: String): MonthlyBreakdown {
    val expensesList = this.filter { it.type == ExpenseType.EXPENSE }
    val incomeList = this.filter { it.type == ExpenseType.INCOME }

    // Group expenses by category
    val categoryBreakdowns = expensesList.groupBy { it.categoryId to it.categoryName }
        .map { (categoryInfo, categoryExpenses) ->
            categoryExpenses.toCategoryBreakdown(categoryInfo.first, categoryInfo.second, expensesList.sumOf { it.amount })
        }
        .sortedByDescending { it.totalAmount }

    return MonthlyBreakdown(
        monthKey = monthKey,
        monthDisplayName = monthKey.toDisplayName(),
        categories = categoryBreakdowns,
        totalExpenses = expensesList.sumOf { it.amount },
        totalIncome = incomeList.sumOf { it.amount },
        netAmount = incomeList.sumOf { it.amount } - expensesList.sumOf { it.amount },
        expenseCount = expensesList.size,
        incomeCount = incomeList.size
    )
}

fun List<Expense>.toCategoryBreakdown(categoryId: String, categoryName: String, monthlyTotal: Double): CategoryBreakdown {
    val totalAmount = this.sumOf { it.amount }
    val percentage = if (monthlyTotal > 0) (totalAmount / monthlyTotal) * 100 else 0.0
    val averageAmount = if (this.isNotEmpty()) totalAmount / this.size else 0.0

    return CategoryBreakdown(
        categoryId = categoryId,
        categoryName = categoryName,
        expenses = this.sortedByDescending { it.createdAt?.toDate()?.time ?: 0 },
        totalAmount = totalAmount,
        expenseCount = this.size,
        averageAmount = averageAmount,
        percentage = percentage
    )
}

fun List<MonthlyBreakdown>.toOverallTotals(): OverallTotals {
    val totalExpenses = this.sumOf { it.totalExpenses }
    val totalIncome = this.sumOf { it.totalIncome }
    val netAmount = totalIncome - totalExpenses
    val monthCount = this.size

    val averageMonthlyExpenses = if (monthCount > 0) totalExpenses / monthCount else 0.0
    val averageMonthlyIncome = if (monthCount > 0) totalIncome / monthCount else 0.0

    // Find top categories across all months
    val allCategories = this.flatMap { it.categories }
    val topExpenseCategory = allCategories
        .groupBy { it.categoryName }
        .maxByOrNull { it.value.sumOf { category -> category.totalAmount } }
        ?.key

    val topIncomeCategory = this.flatMap { monthlyBreakdown ->
        monthlyBreakdown.categories.filter { it.categoryName.contains("income", ignoreCase = true) }
    }.groupBy { it.categoryName }
        .maxByOrNull { it.value.sumOf { category -> category.totalAmount } }
        ?.key

    return OverallTotals(
        totalExpenses = totalExpenses,
        totalIncome = totalIncome,
        netAmount = netAmount,
        monthCount = monthCount,
        averageMonthlyExpenses = averageMonthlyExpenses,
        averageMonthlyIncome = averageMonthlyIncome,
        topExpenseCategory = topExpenseCategory,
        topIncomeCategory = topIncomeCategory
    )
}

/**
 * Domain-specific exceptions for expense breakdown use case
 */
sealed class ExpenseBreakdownException(message: String) : Exception(message) {
    object InvalidUserId : ExpenseBreakdownException("User ID cannot be empty")
    object InvalidMonthKey : ExpenseBreakdownException("Month key must be in format yyyy-MM")
    object InvalidMonthLimit : ExpenseBreakdownException("Month limit must be between 1 and 24")
    object NetworkError : ExpenseBreakdownException("Network connection failed. Please check your internet connection")
    object PermissionDenied : ExpenseBreakdownException("You don't have permission to access expense breakdown")
    object RequestTimeout : ExpenseBreakdownException("Request timed out. Please try again")
    data class DataProcessingError(val details: String) : ExpenseBreakdownException("Failed to process breakdown data: $details")
    data class UnknownError(val details: String) : ExpenseBreakdownException("Failed to fetch breakdown: $details")
}
