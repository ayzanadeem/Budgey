package com.example.budgey.domain.usecase

import com.example.budgey.data.model.Category
import com.example.budgey.data.repository.CategoryRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for adding new categories with validation and duplicate checking
 * Follows single responsibility principle for category creation
 */
class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase
) {

    /**
     * Adds a new category with validation and duplicate checking
     * @param name The name of the category
     * @param userId The ID of the user creating the category
     * @param icon Optional icon name for the category
     * @param color Optional color hex code for the category
     * @param description Optional description for the category
     * @return Result indicating success or failure of the operation
     */
    suspend operator fun invoke(
        name: String,
        userId: String,
        icon: String? = null,
        color: String? = null,
        description: String? = null
    ): Result<Unit> {
        // Input validation
        val validationResult = validateInput(name, userId, color)
        if (validationResult != null) {
            return Result.failure(validationResult)
        }

        // Clean inputs
        val cleanName = name.trim()
        val cleanUserId = userId.trim()
        val cleanIcon = icon?.trim()
        val cleanColor = color?.trim()
        val cleanDescription = description?.trim()

        try {
            // Check for duplicate categories
            val duplicateCheckResult = checkForDuplicates(cleanName, cleanUserId)
            if (duplicateCheckResult != null) {
                return Result.failure(duplicateCheckResult)
            }

            // Create new category
            val newCategory = Category(
                name = cleanName,
                userId = cleanUserId,
                createdAt = Timestamp.now(),
                isActive = true,
                icon = cleanIcon,
                color = cleanColor,
                description = cleanDescription
            )

            // Add category through repository
            val addResult = categoryRepository.addCategory(newCategory)

            return addResult.fold(
                onSuccess = {
                    // Clear cache to force refresh on next get
                    getCategoriesUseCase.clearCache()
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    Result.failure(mapCategoryException(exception))
                }
            )

        } catch (e: Exception) {
            return Result.failure(AddCategoryException.UnknownError(e.message ?: "Unknown error occurred"))
        }
    }

    /**
     * Validates input parameters for category creation
     */
    private fun validateInput(name: String, userId: String, color: String?): AddCategoryException? {
        if (name.isBlank()) {
            return AddCategoryException.EmptyName
        }

        if (name.trim().length < MIN_CATEGORY_NAME_LENGTH) {
            return AddCategoryException.NameTooShort
        }

        if (name.trim().length > MAX_CATEGORY_NAME_LENGTH) {
            return AddCategoryException.NameTooLong
        }

        if (userId.isBlank()) {
            return AddCategoryException.InvalidUserId
        }

        // Validate color format if provided
        color?.let { colorValue ->
            if (colorValue.isNotBlank() && !isValidHexColor(colorValue.trim())) {
                return AddCategoryException.InvalidColorFormat
            }
        }

        return null
    }

    /**
     * Checks for duplicate category names for the user
     */
    private suspend fun checkForDuplicates(name: String, userId: String): AddCategoryException? {
        return try {
            val categoriesResult = getCategoriesUseCase(userId, forceRefresh = true).first()

            categoriesResult.fold(
                onSuccess = { categories ->
                    val isDuplicate = categories.any { category ->
                        category.name.equals(name, ignoreCase = true)
                    }

                    if (isDuplicate) {
                        AddCategoryException.DuplicateName
                    } else {
                        null
                    }
                },
                onFailure = { exception ->
                    // If we can't check for duplicates, we'll allow the operation to proceed
                    // but log this issue
                    null
                }
            )
        } catch (e: Exception) {
            // If duplicate check fails, allow operation to proceed
            null
        }
    }

    /**
     * Validates hex color format (#RRGGBB or #RGB)
     */
    private fun isValidHexColor(color: String): Boolean {
        val hexPattern = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toRegex()
        return hexPattern.matches(color)
    }

    /**
     * Maps repository exceptions to use case specific exceptions
     */
    private fun mapCategoryException(exception: Throwable): AddCategoryException {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> {
                AddCategoryException.NetworkError
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                AddCategoryException.PermissionDenied
            }
            exception.message?.contains("quota", ignoreCase = true) == true -> {
                AddCategoryException.QuotaExceeded
            }
            else -> AddCategoryException.UnknownError(exception.message ?: "Failed to add category")
        }
    }

    companion object {
        private const val MIN_CATEGORY_NAME_LENGTH = 1
        private const val MAX_CATEGORY_NAME_LENGTH = 50
    }
}

/**
 * Domain-specific exceptions for add category use case
 */
sealed class AddCategoryException(message: String) : Exception(message) {
    object EmptyName : AddCategoryException("Category name cannot be empty")
    object NameTooShort : AddCategoryException("Category name must be at least 1 character long")
    object NameTooLong : AddCategoryException("Category name cannot exceed 50 characters")
    object InvalidUserId : AddCategoryException("User ID cannot be empty")
    object DuplicateName : AddCategoryException("A category with this name already exists")
    object InvalidColorFormat : AddCategoryException("Color must be in hex format (#RRGGBB or #RGB)")
    object NetworkError : AddCategoryException("Network connection failed. Please check your internet connection")
    object PermissionDenied : AddCategoryException("You don't have permission to add categories")
    object QuotaExceeded : AddCategoryException("Category limit exceeded. Please contact support")
    data class UnknownError(val details: String) : AddCategoryException("Failed to add category: $details")
}
