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
     * Retrieves expenses for a specific user with pagination support
     * @param userId The ID of the user whose expenses to retrieve
     * @param pageNumber The page number (starting from 1)
     * @param pageSize The number of items per page (default 10)
     * @return Flow emitting Result with list of expenses ordered by date descending
     */
    fun getExpensesPaginated(userId: String, pageNumber: Int = 1, pageSize: Int = 10): Flow<Result<List<Expense>>>

    /**
     * Reset pagination cursor (useful for refresh operations)
     */
    fun resetPaginationCursor()
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    private val expensesCollection = firestore.collection(ExpenseConstants.COLLECTION_EXPENSES)

    // Store the last document for pagination cursor
    private var lastDocumentSnapshot: com.google.firebase.firestore.DocumentSnapshot? = null
    private var currentUserId: String? = null

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

            val budgetMonthKey = generateBudgetMonthKey(budgetStartDate, budgetEndDate)

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
                .limit(10)
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

    override fun getExpensesPaginated(userId: String, pageNumber: Int, pageSize: Int): Flow<Result<List<Expense>>> = callbackFlow {
        Log.d(ExpenseConstants.TAG, "Fetching paginated expenses for user: $userId, page: $pageNumber, size: $pageSize")

        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        try {
            // Reset cursor if different user or starting fresh
            if (currentUserId != userId || pageNumber == 1) {
                lastDocumentSnapshot = null
                currentUserId = userId
            }

            val baseQuery = expensesCollection
                .whereEqualTo(ExpenseConstants.FIELD_USER_ID, userId)
                .orderBy(ExpenseConstants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            val query = if (pageNumber == 1 || lastDocumentSnapshot == null) {
                // First page or no cursor available
                baseQuery
            } else {
                // Subsequent pages - use cursor
                baseQuery.startAfter(lastDocumentSnapshot!!)
            }

            listenerRegistration = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(ExpenseConstants.TAG, "Error listening to paginated expenses", exception)
                    trySend(Result.failure(exception))
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    try {
                        val expenses = querySnapshot.documents.mapNotNull { doc ->
                            doc.toExpense()?.also { expense ->
                                Log.d(
                                    ExpenseConstants.TAG,
                                    "Retrieved paginated expense: ${expense.amount}"
                                )
                            }
                        }

                        // Update cursor for next page
                        if (querySnapshot.documents.isNotEmpty()) {
                            lastDocumentSnapshot = querySnapshot.documents.last()
                            println("🔥 DEBUG Repository: Updated cursor to document with createdAt: ${lastDocumentSnapshot?.get("created_at")}")
                        } else {
                            println("🔥 DEBUG Repository: No documents received, cursor remains: ${lastDocumentSnapshot?.get("created_at")}")
                        }

                        println("🔥 DEBUG Repository: Page $pageNumber returned ${expenses.size} expenses")
                        trySend(Result.success(expenses))
                    } catch (e: Exception) {
                        Log.e(ExpenseConstants.TAG, "Error parsing paginated expense documents", e)
                        trySend(Result.failure(e))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ExpenseConstants.TAG, "Error setting up paginated expenses listener", e)
            trySend(Result.failure(e))
        }

        awaitClose {
            Log.d(ExpenseConstants.TAG, "Closing paginated expenses listener for user: $userId")
            listenerRegistration?.remove()
        }
    }

    /**
     * Reset pagination cursor (useful for refresh operations)
     */
    override fun resetPaginationCursor() {
        lastDocumentSnapshot = null
        currentUserId = null
    }

    /**
     * Helper method to generate budget month key from date
     * @param date The date to generate month key from
     * @return Month key in format "yyyy-MM"
     */
    private fun generateBudgetMonthKey(budgetStartDate: Timestamp, budgetEndDate: Timestamp): String {
        val formatter = SimpleDateFormat("ddMMyy", Locale.getDefault())
        val startDate = Date(budgetStartDate.seconds * 1000)
        val endDate = Date(budgetEndDate.seconds * 1000)
        return formatter.format(startDate) + formatter.format(endDate)
    }
}
