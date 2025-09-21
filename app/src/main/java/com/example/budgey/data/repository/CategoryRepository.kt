package com.example.budgey.data.repository

import android.util.Log
import com.example.budgey.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore collection and query constants
 */
object CategoryConstants {
    const val COLLECTION_CATEGORIES = "categories"
    const val FIELD_USER_ID = "user_id"
    const val FIELD_IS_ACTIVE = "is_active"
    const val FIELD_NAME = "name"
    const val TAG = "CategoryRepository"
}

/**
 * Repository interface for expense category operations
 * Follows clean architecture principles with proper error handling
 */
interface CategoryRepository {
    /**
     * Retrieves all active categories for a specific user with real-time updates
     * @param userId The ID of the user whose categories to retrieve
     * @return Flow emitting Result with list of categories (without document IDs)
     */
    fun getCategories(userId: String): Flow<Result<List<Category>>>

    /**
     * Adds a new category for a user
     * @param category The category to add
     * @return Result indicating success or failure of the operation
     */
    suspend fun addCategory(category: Category): Result<Unit>
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val categoriesCollection = firestore.collection(CategoryConstants.COLLECTION_CATEGORIES)

    override fun getCategories(userId: String): Flow<Result<List<Category>>> = callbackFlow {
        Log.d(CategoryConstants.TAG, "Setting up real-time listener for user categories: $userId")

        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = categoriesCollection
                .whereEqualTo(CategoryConstants.FIELD_USER_ID, userId)
                .whereEqualTo(CategoryConstants.FIELD_IS_ACTIVE, true)
                .orderBy(CategoryConstants.FIELD_NAME)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e(CategoryConstants.TAG, "Error listening to categories", exception)
                        trySend(Result.failure(exception))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        try {
                            val categories = snapshot.documents.mapNotNull { doc ->
                                doc.toCategory()?.also { category ->
                                    Log.d(CategoryConstants.TAG, "Retrieved category: ${category.name}")
                                }
                            }
                            Log.d(CategoryConstants.TAG, "Retrieved ${categories.size} categories for user $userId")
                            trySend(Result.success(categories))
                        } catch (e: Exception) {
                            Log.e(CategoryConstants.TAG, "Error parsing category documents", e)
                            trySend(Result.failure(e))
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(CategoryConstants.TAG, "Error setting up categories listener", e)
            trySend(Result.failure(e))
        }

        awaitClose {
            Log.d(CategoryConstants.TAG, "Closing categories listener for user: $userId")
            listenerRegistration?.remove()
        }
    }

    override suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            Log.d(CategoryConstants.TAG, "Adding new category: ${category.name} for user: ${category.userId}")

            categoriesCollection.add(category.toMap()).await()

            Log.d(CategoryConstants.TAG, "Successfully added category: ${category.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(CategoryConstants.TAG, "Failed to add category: ${category.name}", e)
            Result.failure(e)
        }
    }
}
