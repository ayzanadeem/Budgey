package com.example.budgey.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Expense data class for Firestore integration
 * Represents expenses and income in the Budgey app
 */
data class Expense(
    @PropertyName("user_id")
    val userId: String = "",

    @PropertyName("category_id")
    val categoryId: String = "",

    @PropertyName("category_name")
    val categoryName: String = "",

    @PropertyName("amount")
    val amount: Double = 0.0,

    @PropertyName("budget_start_date")
    val budgetStartDate: Timestamp? = null,

    @PropertyName("budget_end_date")
    val budgetEndDate: Timestamp? = null,

    @PropertyName("created_at")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("budget_month_key")
    val budgetMonthKey: String = "",

    @PropertyName("description")
    val description: String? = null,

    @PropertyName("type")
    val type: ExpenseType = ExpenseType.EXPENSE,

    @PropertyName("currency")
    val currency: String = "USD",

    @PropertyName("payment_method")
    val paymentMethod: String? = null,

    @PropertyName("receipt_url")
    val receiptUrl: String? = null,

    @PropertyName("tags")
    val tags: List<String> = emptyList()
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", "", 0.0, null, null, null, "", null, ExpenseType.EXPENSE, "USD", null, null, emptyList())
}

/**
 * Enum for expense types
 */
enum class ExpenseType {
    @PropertyName("EXPENSE")
    EXPENSE,

    @PropertyName("INCOME")
    INCOME
}

/**
 * Extension functions for Expense data class
 */
fun Expense.toMap(): Map<String, Any?> {
    return mapOf(
        "user_id" to userId,
        "category_id" to categoryId,
        "category_name" to categoryName,
        "amount" to amount,
        "budget_start_date" to budgetStartDate,
        "budget_end_date" to budgetEndDate,
        "created_at" to createdAt,
        "budget_month_key" to budgetMonthKey,
        "description" to description,
        "type" to type.name,
        "currency" to currency,
        "payment_method" to paymentMethod,
        "receipt_url" to receiptUrl,
        "tags" to tags
    )
}

fun DocumentSnapshot.toExpense(): Expense? {
    return try {
        val typeString = getString("type") ?: "EXPENSE"
        val expenseType = try {
            ExpenseType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            ExpenseType.EXPENSE
        }

        Expense(
            userId = getString("user_id") ?: "",
            categoryId = getString("category_id") ?: "",
            categoryName = getString("category_name") ?: "",
            amount = getDouble("amount") ?: 0.0,
            budgetStartDate = getTimestamp("budget_start_date"),
            budgetEndDate = getTimestamp("budget_end_date"),
            createdAt = getTimestamp("created_at"),
            budgetMonthKey = getString("budget_month_key") ?: "",
            description = getString("description"),
            type = expenseType,
            currency = getString("currency") ?: "USD",
            paymentMethod = getString("payment_method"),
            receiptUrl = getString("receipt_url"),
            tags = get("tags") as? List<String> ?: emptyList()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Helper functions for budget month key generation and date handling
 */
fun generateBudgetMonthKey(date: Date = Date()): String {
    val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return formatter.format(date)
}

fun generateBudgetMonthKey(timestamp: Timestamp): String {
    return generateBudgetMonthKey(timestamp.toDate())
}

fun getBudgetPeriodDates(date: Date = Date()): Pair<Timestamp, Timestamp> {
    val calendar = Calendar.getInstance()
    calendar.time = date

    // Start of month
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startDate = Timestamp(calendar.time)

    // End of month
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endDate = Timestamp(calendar.time)

    return Pair(startDate, endDate)
}

/**
 * Extension functions for Expense operations
 */
fun Expense.isIncome(): Boolean = type == ExpenseType.INCOME

fun Expense.isExpense(): Boolean = type == ExpenseType.EXPENSE

fun Expense.withDescription(newDescription: String): Expense {
    return this.copy(description = newDescription)
}

fun Expense.withAmount(newAmount: Double): Expense {
    return this.copy(amount = newAmount)
}

fun Expense.withCategory(categoryId: String, categoryName: String): Expense {
    return this.copy(categoryId = categoryId, categoryName = categoryName)
}

fun Expense.addTag(tag: String): Expense {
    return if (tag !in tags) {
        this.copy(tags = tags + tag)
    } else {
        this
    }
}

fun Expense.removeTag(tag: String): Expense {
    return this.copy(tags = tags - tag)
}

fun Expense.withPaymentMethod(method: String): Expense {
    return this.copy(paymentMethod = method)
}

fun Expense.getFormattedAmount(): String {
    val sign = if (isIncome()) "+" else "-"
    return "$sign$currency %.2f".format(amount)
}

fun Expense.isInCurrentMonth(): Boolean {
    val currentMonthKey = generateBudgetMonthKey()
    return budgetMonthKey == currentMonthKey
}

/**
 * Factory function to create a new expense with proper budget period setup
 */
fun createExpense(
    userId: String,
    categoryId: String,
    categoryName: String,
    amount: Double,
    type: ExpenseType = ExpenseType.EXPENSE,
    description: String? = null,
    date: Date = Date()
): Expense {
    val budgetPeriod = getBudgetPeriodDates(date)
    val monthKey = generateBudgetMonthKey(date)

    return Expense(
        userId = userId,
        categoryId = categoryId,
        categoryName = categoryName,
        amount = amount,
        budgetStartDate = budgetPeriod.first,
        budgetEndDate = budgetPeriod.second,
        budgetMonthKey = monthKey,
        description = description,
        type = type
    )
}
