package com.example.budgey.domain.usecase

import com.example.budgey.data.model.Category
import com.example.budgey.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for fetching user categories with caching support
 * Follows single responsibility principle for category retrieval
 */
@Singleton
class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    // Simple in-memory cache for categories
    private var cachedCategories: List<Category>? = null
    private var cachedUserId: String? = null

    /**
     * Fetches categories for a user with caching mechanism
     * @param userId The ID of the user whose categories to fetch
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     * @return Flow emitting Result with list of categories
     */
    operator fun invoke(userId: String, forceRefresh: Boolean = false): Flow<Result<List<Category>>> {
        // Input validation
        if (userId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(
                Result.failure(CategoriesException.InvalidUserId)
            )
        }

        // Check cache first if not forcing refresh and same user
        if (!forceRefresh && cachedUserId == userId && cachedCategories != null) {
            return kotlinx.coroutines.flow.flowOf(
                Result.success(cachedCategories!!)
            )
        }

        return categoryRepository.getCategories(userId)
            .onStart {
                // Could emit loading state here if needed
            }
            .map { result ->
                result.fold(
                    onSuccess = { categories ->
                        // Update cache with successful result
                        updateCache(userId, categories)
                        Result.success(categories)
                    },
                    onFailure = { exception ->
                        Result.failure(mapCategoryException(exception))
                    }
                )
            }
            .catch { exception ->
                emit(Result.failure(mapCategoryException(exception)))
            }
    }

    /**
     * Clears the cached categories (useful when user logs out or data changes)
     */
    fun clearCache() {
        cachedCategories = null
        cachedUserId = null
    }

    /**
     * Updates the in-memory cache
     */
    private fun updateCache(userId: String, categories: List<Category>) {
        cachedUserId = userId
        cachedCategories = categories
    }

    /**
     * Maps repository exceptions to use case specific exceptions
     */
    private fun mapCategoryException(exception: Throwable): CategoriesException {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> {
                CategoriesException.NetworkError
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                CategoriesException.PermissionDenied
            }
            exception.message?.contains("timeout", ignoreCase = true) == true -> {
                CategoriesException.RequestTimeout
            }
            else -> CategoriesException.UnknownError(exception.message ?: "Failed to fetch categories")
        }
    }
}

/**
 * Domain-specific exceptions for categories use case
 */
sealed class CategoriesException(message: String) : Exception(message) {
    object InvalidUserId : CategoriesException("User ID cannot be empty")
    object NetworkError : CategoriesException("Network connection failed. Please check your internet connection")
    object PermissionDenied : CategoriesException("You don't have permission to access these categories")
    object RequestTimeout : CategoriesException("Request timed out. Please try again")
    data class UnknownError(val details: String) : CategoriesException("Failed to fetch categories: $details")
}
