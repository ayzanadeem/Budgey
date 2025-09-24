package com.example.budgey.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.sql.Time
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
    val budgetStartDate: Timestamp,

    @PropertyName("budget_end_date")
    val budgetEndDate: Timestamp,

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
    constructor() : this("", "", "", 0.0,
        Timestamp.now(),
        Timestamp.now(), null, "", null, ExpenseType.EXPENSE, "USD", null, null, emptyList())
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
            budgetStartDate = getTimestamp("budget_start_date") ?: Timestamp.now(),
            budgetEndDate = getTimestamp("budget_end_date") ?: Timestamp.now(),
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