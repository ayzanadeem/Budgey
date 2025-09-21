package com.example.budgey.data.repository

import android.util.Log
import com.example.budgey.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore collection and query constants for expenses
 */
object ExpenseConstants {
    const val COLLECTION_EXPENSES = "expenses"
    const val FIELD_USER_ID = "user_id"
    const val FIELD_CREATED_AT = "created_at"
    const val FIELD_BUDGET_MONTH_KEY = "budget_month_key"
    const val TAG = "ExpenseRepository"
}

/**
 * Repository interface for expense management operations
 * Follows clean architecture principles with proper error handling
 */
interface ExpenseRepository {
    /**
     * Adds a new expense to the database with auto-generated budgetMonthKey
     * @param expense The expense to add
     * @param budgetStartDate The start date of the budget period
     * @param budgetEndDate The end date of the budget period
     * @return Result containing the created expense ID on success, exception on failure
     */
    suspend fun addExpense(expense: Expense, budgetStartDate: Timestamp, budgetEndDate: Timestamp): Result<String>

    /**
     * Retrieves all expenses for a specific user with real-time updates
     * @param userId The ID of the user whose expenses to retrieve
     * @return Flow emitting Result with list of expenses ordered by date descending
     */
    fun getExpenses(userId: String): Flow<Result<List<Expense>>>

    /**
     * Retrieves expenses for a specific user within a given month
     * @param userId The ID of the user whose expenses to retrieve
     * @param monthKey The month key in format "yyyy-MM" (e.g., "2025-09")
     * @return Result containing list of expenses for the specified month
     */
    suspend fun getExpensesByMonth(userId: String, monthKey: String): Result<List<Expense>>
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    private val expensesCollection = firestore.collection(ExpenseConstants.COLLECTION_EXPENSES)

    override suspend fun addExpense(
        expense: Expense,
        budgetStartDate: Timestamp,
        budgetEndDate: Timestamp
    ): Result<String> {
        return try {
            Log.d(
                ExpenseConstants.TAG,
                "Adding new expense: ${expense.amount} for user: ${expense.userId}"
            )

            // Auto-generate budget month key
            val currentDate = Date()
            val budgetMonthKey = generateBudgetMonthKey(currentDate)

            val expenseWithGeneratedFields = expense.copy(
                budgetMonthKey = budgetMonthKey,
                budgetStartDate = budgetStartDate,
                budgetEndDate = budgetEndDate,
                createdAt = Timestamp.now()
            )

            val documentRef = expensesCollection.add(expenseWithGeneratedFields.toMap()).await()

            Log.d(ExpenseConstants.TAG, "Successfully added expense with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(ExpenseConstants.TAG, "Failed to add expense: ${expense.amount}", e)
            Result.failure(e)
        }
    }

    override fun getExpenses(userId: String): Flow<Result<List<Expense>>> = callbackFlow {
        Log.d(ExpenseConstants.TAG, "Setting up real-time listener for user expenses: $userId")

        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        try {
            listenerRegistration = expensesCollection
                .whereEqualTo(ExpenseConstants.FIELD_USER_ID, userId)
                .orderBy(ExpenseConstants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e(ExpenseConstants.TAG, "Error listening to expenses", exception)
                        trySend(Result.failure(exception))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        try {
                            val expenses = snapshot.documents.mapNotNull { doc ->
                                doc.toExpense()?.also { expense ->
                                    Log.d(
                                        ExpenseConstants.TAG,
                                        "Retrieved expense: ${expense.amount}"
                                    )
                                }
                            }
                            Log.d(
                                ExpenseConstants.TAG,
                                "Retrieved ${expenses.size} expenses for user $userId"
                            )
                            trySend(Result.success(expenses))
                        } catch (e: Exception) {
                            Log.e(ExpenseConstants.TAG, "Error parsing expense documents", e)
                            trySend(Result.failure(e))
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(ExpenseConstants.TAG, "Error setting up expenses listener", e)
            trySend(Result.failure(e))
        }

        awaitClose {
            Log.d(ExpenseConstants.TAG, "Closing expenses listener for user: $userId")
            listenerRegistration?.remove()
        }
    }

    override suspend fun getExpensesByMonth(
        userId: String,
        monthKey: String
    ): Result<List<Expense>> {
        return try {
            Log.d(ExpenseConstants.TAG, "Fetching expenses for user: $userId, month: $monthKey")

            val snapshot = expensesCollection
                .whereEqualTo(ExpenseConstants.FIELD_USER_ID, userId)
                .whereEqualTo(ExpenseConstants.FIELD_BUDGET_MONTH_KEY, monthKey)
                .orderBy(ExpenseConstants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            val expenses = snapshot.documents.mapNotNull { doc ->
                doc.toExpense()?.also { expense ->
                    Log.d(ExpenseConstants.TAG, "Retrieved monthly expense: ${expense.amount}")
                }
            }

            Log.d(ExpenseConstants.TAG, "Retrieved ${expenses.size} expenses for month $monthKey")
            Result.success(expenses)
        } catch (e: Exception) {
            Log.e(ExpenseConstants.TAG, "Failed to get expenses for month $monthKey", e)
            Result.failure(e)
        }
    }

    /**
     * Helper method to generate budget month key from date
     * @param date The date to generate month key from
     * @return Month key in format "yyyy-MM"
     */
    private fun generateBudgetMonthKey(date: Date = Date()): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return formatter.format(date)
    }
}


