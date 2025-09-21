package com.example.budgey.data.repository

import com.example.budgey.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense): Result<String>
    suspend fun updateExpense(expenseId: String, expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
    fun getExpenses(userId: String): Flow<List<Pair<String, Expense>>>
    suspend fun getExpensesByMonth(userId: String, monthKey: String): Result<List<Pair<String, Expense>>>
    suspend fun getExpensesByDateRange(userId: String, startDate: Long, endDate: Long): Result<List<Pair<String, Expense>>>
    suspend fun getTotalAmountByType(userId: String, monthKey: String, type: ExpenseType): Result<Double>
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

    override suspend fun updateExpense(expenseId: String, expense: Expense): Result<Unit> {
        return try {
            expensesCollection.document(expenseId).set(expense.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            expensesCollection.document(expenseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getExpenses(userId: String): Flow<List<Pair<String, Expense>>> = flow {
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
            emit(expenses)
        } catch (e: Exception) {
            emit(emptyList())
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

    override suspend fun getExpensesByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<Pair<String, Expense>>> {
        return try {
            val snapshot = expensesCollection
                .whereEqualTo(FirestoreFields.USER_ID, userId)
                .whereGreaterThanOrEqualTo(FirestoreFields.CREATED_AT, com.google.firebase.Timestamp(java.util.Date(startDate)))
                .whereLessThanOrEqualTo(FirestoreFields.CREATED_AT, com.google.firebase.Timestamp(java.util.Date(endDate)))
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

    override suspend fun getTotalAmountByType(userId: String, monthKey: String, type: ExpenseType): Result<Double> {
        return try {
            val snapshot = expensesCollection
                .whereEqualTo(FirestoreFields.USER_ID, userId)
                .whereEqualTo(FirestoreFields.BUDGET_MONTH_KEY, monthKey)
                .whereEqualTo(FirestoreFields.TYPE, type.name)
                .get()
                .await()

            val total = snapshot.documents.sumOf { doc ->
                doc.getDouble(FirestoreFields.AMOUNT) ?: 0.0
            }
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
