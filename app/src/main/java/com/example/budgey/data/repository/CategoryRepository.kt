package com.example.budgey.data.repository

import com.example.budgey.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for expense category operations
 * Follows clean architecture principles with proper error handling
 */
interface CategoryRepository {
    /**
     * Retrieves all categories for a specific user
     * @param userId The ID of the user whose categories to retrieve
     * @return Flow emitting Result with list of categories and their IDs
     */
    fun getCategories(userId: String): Flow<Result<List<Pair<String, Category>>>>

    /**
     * Adds a new category for a user
     * @param category The category to add
     * @return Result containing the created category ID on success, exception on failure
     */
    suspend fun addCategory(category: Category): Result<String>
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val categoriesCollection = firestore.collection(FirestoreCollections.CATEGORIES)

    override fun getCategories(userId: String): Flow<Result<List<Pair<String, Category>>>> = flow {
        try {
            val snapshot = categoriesCollection
                .whereEqualTo(FirestoreFields.USER_ID, userId)
                .whereEqualTo(FirestoreFields.IS_ACTIVE, true)
                .orderBy(FirestoreFields.NAME)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toCategory()?.let { category ->
                    doc.id to category
                }
            }
            emit(Result.success(categories))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun addCategory(category: Category): Result<String> {
        return try {
            val documentRef = categoriesCollection.add(category.toMap()).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
