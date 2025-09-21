package com.example.budgey.domain.usecase

import com.example.budgey.data.model.Expense
import com.example.budgey.data.model.ExpenseType
import com.example.budgey.data.repository.ExpenseRepository
import com.google.firebase.Timestamp
import java.util.*
import javax.inject.Inject

/**
 * Use case for adding new expenses with validation and business logic
 * Follows single responsibility principle for expense creation
 */
class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Adds a new expense with validation and date logic
     * @param userId The ID of the user creating the expense
     * @param categoryId The ID of the selected category
     * @param categoryName The name of the selected category
     * @param amount The amount of the expense
     * @param type The type of expense (EXPENSE or INCOME)
     * @param description Optional description for the expense
     * @param budgetStartDate The start date of the budget period
     * @param budgetEndDate The end date of the budget period
     * @param currency Optional currency code (defaults to USD)
     * @return Result indicating success or failure of the operation
     */
    suspend operator fun invoke(
        userId: String,
        categoryId: String,
        categoryName: String,
        amount: Double,
        type: ExpenseType = ExpenseType.EXPENSE,
        description: String? = null,
        budgetStartDate: Timestamp,
        budgetEndDate: Timestamp,
        currency: String = "USD"
    ): Result<String> {
        // Input validation
        val validationResult = validateInput(
            userId, categoryId, categoryName, amount, budgetStartDate, budgetEndDate, currency
        )
        if (validationResult != null) {
            return Result.failure(validationResult)
        }

        // Clean inputs
        val cleanUserId = userId.trim()
        val cleanCategoryId = categoryId.trim()
        val cleanCategoryName = categoryName.trim()
        val cleanDescription = description?.trim()
        val cleanCurrency = currency.trim().uppercase()

        try {
            // Validate date logic
            val dateValidationResult = validateDateLogic(budgetStartDate, budgetEndDate)
            if (dateValidationResult != null) {
                return Result.failure(dateValidationResult)
            }

            // Create new expense
            val newExpense = Expense(
                userId = cleanUserId,
                categoryId = cleanCategoryId,
                categoryName = cleanCategoryName,
                amount = amount,
                budgetStartDate = budgetStartDate,
                budgetEndDate = budgetEndDate,
                description = cleanDescription,
                type = type,
                currency = cleanCurrency
            )

            // Add expense through repository
            val addResult = expenseRepository.addExpense(newExpense, budgetStartDate, budgetEndDate)

            return addResult.fold(
                onSuccess = { expenseId ->
                    Result.success(expenseId)
                },
                onFailure = { exception ->
                    Result.failure(mapExpenseException(exception))
                }
            )

        } catch (e: Exception) {
            return Result.failure(AddExpenseException.UnknownError(e.message ?: "Unknown error occurred"))
        }
    }

    /**
     * Validates input parameters for expense creation
     */
    private fun validateInput(
        userId: String,
        categoryId: String,
        categoryName: String,
        amount: Double,
        budgetStartDate: Timestamp,
        budgetEndDate: Timestamp,
        currency: String
    ): AddExpenseException? {
        if (userId.isBlank()) {
            return AddExpenseException.InvalidUserId
        }

        if (categoryId.isBlank()) {
            return AddExpenseException.InvalidCategoryId
        }

        if (categoryName.isBlank()) {
            return AddExpenseException.InvalidCategoryName
        }

        if (amount <= 0) {
            return AddExpenseException.InvalidAmount
        }

        if (amount > MAX_EXPENSE_AMOUNT) {
            return AddExpenseException.AmountTooLarge
        }

        if (currency.isBlank() || currency.length != 3) {
            return AddExpenseException.InvalidCurrency
        }

        return null
    }

    /**
     * Validates date logic for budget periods
     */
    private fun validateDateLogic(
        budgetStartDate: Timestamp,
        budgetEndDate: Timestamp
    ): AddExpenseException? {
        val startDate = budgetStartDate.toDate()
        val endDate = budgetEndDate.toDate()
        val currentDate = Date()

        // Check if start date is before end date
        if (startDate.after(endDate)) {
            return AddExpenseException.InvalidDateRange
        }

        // Check if the date range is reasonable (not more than 1 year)
        val oneYearInMs = 365L * 24 * 60 * 60 * 1000
        if ((endDate.time - startDate.time) > oneYearInMs) {
            return AddExpenseException.DateRangeTooLarge
        }

        // Check if dates are not too far in the future (more than 1 year)
        if (startDate.time > (currentDate.time + oneYearInMs)) {
            return AddExpenseException.DateTooFarInFuture
        }

        return null
    }

    /**
     * Maps repository exceptions to use case specific exceptions
     */
    private fun mapExpenseException(exception: Throwable): AddExpenseException {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> {
                AddExpenseException.NetworkError
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                AddExpenseException.PermissionDenied
            }
            exception.message?.contains("quota", ignoreCase = true) == true -> {
                AddExpenseException.QuotaExceeded
            }
            else -> AddExpenseException.UnknownError(exception.message ?: "Failed to add expense")
        }
    }

    companion object {
        private const val MAX_EXPENSE_AMOUNT = 1_000_000.0 // 1 million
    }
}

/**
 * Domain-specific exceptions for add expense use case
 */
sealed class AddExpenseException(message: String) : Exception(message) {
    object InvalidUserId : AddExpenseException("User ID cannot be empty")
    object InvalidCategoryId : AddExpenseException("Category ID cannot be empty")
    object InvalidCategoryName : AddExpenseException("Category name cannot be empty")
    object InvalidAmount : AddExpenseException("Amount must be greater than 0")
    object AmountTooLarge : AddExpenseException("Amount cannot exceed 1,000,000")
    object InvalidCurrency : AddExpenseException("Currency must be a valid 3-letter code")
    object InvalidDateRange : AddExpenseException("Start date must be before end date")
    object DateRangeTooLarge : AddExpenseException("Date range cannot exceed 1 year")
    object DateTooFarInFuture : AddExpenseException("Date cannot be more than 1 year in the future")
    object NetworkError : AddExpenseException("Network connection failed. Please check your internet connection")
    object PermissionDenied : AddExpenseException("You don't have permission to add expenses")
    object QuotaExceeded : AddExpenseException("Expense limit exceeded. Please contact support")
    data class UnknownError(val details: String) : AddExpenseException("Failed to add expense: $details")
}
