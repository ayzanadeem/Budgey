package com.example.budgey.data.repository

import com.example.budgey.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    suspend fun createCategory(category: Category): Result<String>
    suspend fun updateCategory(categoryId: String, category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun deactivateCategory(categoryId: String): Result<Unit>
    suspend fun activateCategory(categoryId: String): Result<Unit>
    fun getUserCategories(userId: String): Flow<List<Pair<String, Category>>>
    fun getActiveUserCategories(userId: String): Flow<List<Pair<String, Category>>>
    suspend fun getCategory(categoryId: String): Result<Category?>
    suspend fun createDefaultCategories(userId: String): Result<List<String>>
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val categoriesCollection = firestore.collection(FirestoreCollections.CATEGORIES)

    override suspend fun createCategory(category: Category): Result<String> {
        return try {
            val documentRef = categoriesCollection.add(category.toMap()).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(categoryId: String, category: Category): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).set(category.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateCategory(categoryId: String): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).update(
                FirestoreFields.IS_ACTIVE, false
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateCategory(categoryId: String): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).update(
                FirestoreFields.IS_ACTIVE, true
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserCategories(userId: String): Flow<List<Pair<String, Category>>> = flow {
        try {
            val snapshot = categoriesCollection
                .whereEqualTo(FirestoreFields.USER_ID, userId)
                .orderBy(FirestoreFields.NAME, Query.Direction.ASCENDING)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toCategory()?.let { category ->
                    doc.id to category
                }
            }
            emit(categories)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getActiveUserCategories(userId: String): Flow<List<Pair<String, Category>>> = flow {
        try {
            val snapshot = categoriesCollection
                .whereEqualTo(FirestoreFields.USER_ID, userId)
                .whereEqualTo(FirestoreFields.IS_ACTIVE, true)
                .orderBy(FirestoreFields.NAME, Query.Direction.ASCENDING)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toCategory()?.let { category ->
                    doc.id to category
                }
            }
            emit(categories)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCategory(categoryId: String): Result<Category?> {
        return try {
            val snapshot = categoriesCollection.document(categoryId).get().await()
            val category = snapshot.toCategory()
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDefaultCategories(userId: String): Result<List<String>> {
        return try {
            val defaultCategories = listOf(
                Category(name = "Food & Dining", userId = userId, icon = "restaurant", color = "#FF6B6B"),
                Category(name = "Transportation", userId = userId, icon = "directions_car", color = "#4ECDC4"),
                Category(name = "Shopping", userId = userId, icon = "shopping_bag", color = "#45B7D1"),
                Category(name = "Entertainment", userId = userId, icon = "movie", color = "#96CEB4"),
                Category(name = "Bills & Utilities", userId = userId, icon = "receipt", color = "#FECA57"),
                Category(name = "Healthcare", userId = userId, icon = "local_hospital", color = "#FF9FF3"),
                Category(name = "Education", userId = userId, icon = "school", color = "#54A0FF"),
                Category(name = "Income", userId = userId, icon = "account_balance_wallet", color = "#5F27CD"),
                Category(name = "Miscellaneous", userId = userId, icon = "category", color = "#00D2D3")
            )

            val createdIds = mutableListOf<String>()

            for (category in defaultCategories) {
                val documentRef = categoriesCollection.add(category.toMap()).await()
                createdIds.add(documentRef.id)
            }

            Result.success(createdIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
