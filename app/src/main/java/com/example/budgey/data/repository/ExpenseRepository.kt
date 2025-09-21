package com.example.budgey.data.repository

import com.example.budgey.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for expense management operations
 * Follows clean architecture principles with proper error handling
 */
interface ExpenseRepository {
    /**
     * Adds a new expense to the database
     * @param expense The expense to add
     * @return Result containing the created expense ID on success, exception on failure
     */
    suspend fun addExpense(expense: Expense): Result<String>

    /**
     * Retrieves all expenses for a specific user
     * @param userId The ID of the user whose expenses to retrieve
     * @return Flow emitting Result with list of expenses and their IDs
     */
    fun getExpenses(userId: String): Flow<Result<List<Pair<String, Expense>>>>

    /**
     * Retrieves expenses for a specific user within a given month
     * @param userId The ID of the user whose expenses to retrieve
     * @param monthKey The month key in format "yyyy-MM" (e.g., "2025-09")
     * @return Result containing list of expenses and their IDs for the specified month
     */
    suspend fun getExpensesByMonth(userId: String, monthKey: String): Result<List<Pair<String, Expense>>>
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    private val expensesCollection = firestore.collection(FirestoreCollections.EXPENSES)

    override suspend fun addExpense(expense: Expense): Result<String> {
        return try {
            val documentRef = expensesCollection.add(expense.toMap()).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getExpenses(userId: String): Flow<Result<List<Pair<String, Expense>>>> = flow {
        try {
            val snapshot = expensesCollection
                .whereEqualTo(FirestoreFields.USER_ID, userId)
                .orderBy(FirestoreFields.CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            val expenses = snapshot.documents.mapNotNull { doc ->
                doc.toExpense()?.let { expense ->
                    doc.id to expense
                }
            }
            emit(Result.success(expenses))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getExpensesByMonth(userId: String, monthKey: String): Result<List<Pair<String, Expense>>> {
        return try {
            val snapshot = expensesCollection
                .whereEqualTo(FirestoreFields.USER_ID, userId)
                .whereEqualTo(FirestoreFields.BUDGET_MONTH_KEY, monthKey)
                .orderBy(FirestoreFields.CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            val expenses = snapshot.documents.mapNotNull { doc ->
                doc.toExpense()?.let { expense ->
                    doc.id to expense
                }
            }
            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
