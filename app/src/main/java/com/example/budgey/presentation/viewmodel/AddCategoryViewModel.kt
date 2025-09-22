package com.example.budgey.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgey.domain.usecase.AddCategoryUseCase
import com.example.budgey.domain.usecase.GetCategoriesUseCase
import com.example.budgey.presentation.ui.state.AddCategoryUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Add Category screen following MVVM with clean architecture
 * Handles form validation, state management, and category creation
 */
@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCategoryUiState())
    val uiState: StateFlow<AddCategoryUiState> = _uiState.asStateFlow()

    /**
     * Updates the category name input field
     * @param name The new category name
     */
    fun updateCategoryName(name: String) {
        _uiState.value = _uiState.value.copy(
            categoryName = name,
            errorMessage = null // Clear error when user starts typing
        )

        // Validate in real-time
        validateCategoryName(name)
    }

    /**
     * Validates category name with real-time feedback
     * @param name The category name to validate
     */
    private fun validateCategoryName(name: String) {
        viewModelScope.launch {
            val trimmedName = name.trim()

            when {
                trimmedName.isEmpty() -> {
                    // Don't show error for empty field unless user is trying to save
                    return@launch
                }
                trimmedName.length < 2 -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Category name must be at least 2 characters"
                    )
                }
                trimmedName.length > 20 -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Category name must be less than 20 characters"
                    )
                }
                else -> {
                    // Check for duplicates
                    checkForDuplicateCategory(trimmedName)
                }
            }
        }
    }

    /**
     * Checks if category name already exists for the current user
     * @param name The category name to check
     */
    private suspend fun checkForDuplicateCategory(name: String) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        try {
            val categoriesResult = getCategoriesUseCase(currentUserId, forceRefresh = false).first()
            categoriesResult.fold(
                onSuccess = { categories ->
                    val isDuplicate = categories.any {
                        it.name.trim().equals(name.trim(), ignoreCase = true)
                    }
                    if (isDuplicate) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "A category with this name already exists"
                        )
                    } else {
                        // Clear error if no duplicate found
                        _uiState.value = _uiState.value.copy(
                            errorMessage = null
                        )
                    }
                },
                onFailure = {
                    // Don't show error for duplicate check failure during typing
                    // Will be handled during save operation
                }
            )
        } catch (e: Exception) {
            // Silent failure for real-time validation
            // Will be handled properly during save
        }
    }

    /**
     * Validates the complete form before saving
     * @return true if form is valid, false otherwise
     */
    private fun validateForm(): Boolean {
        val currentState = _uiState.value
        val trimmedName = currentState.categoryName.trim()

        when {
            trimmedName.isEmpty() -> {
                _uiState.value = currentState.copy(
                    errorMessage = "Category name is required"
                )
                return false
            }
            trimmedName.length < 2 -> {
                _uiState.value = currentState.copy(
                    errorMessage = "Category name must be at least 2 characters"
                )
                return false
            }
            trimmedName.length > 50 -> {
                _uiState.value = currentState.copy(
                    errorMessage = "Category name must be less than 50 characters"
                )
                return false
            }
            currentState.errorMessage != null -> {
                // Don't proceed if there are existing validation errors
                return false
            }
            else -> {
                return true
            }
        }
    }

    /**
     * Saves the new category with comprehensive validation and error handling
     */
    fun saveCategory() {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not authenticated. Please log in again."
            )
            return
        }

        // Validate form before proceeding
        if (!validateForm()) {
            return
        }

        val categoryName = _uiState.value.categoryName.trim()

        _uiState.value = _uiState.value.copy(
            isSaving = true,
            errorMessage = null,
            isSaved = false
        )

        viewModelScope.launch {
            try {
                val result = addCategoryUseCase(
                    name = categoryName,
                    userId = currentUserId
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSaved = true,
                            errorMessage = null
                        )
                        // Clear form after successful save
                        clearForm()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSaved = false,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = false,
                    errorMessage = "Failed to save category. Please try again."
                )
            }
        }
    }

    /**
     * Converts exceptions to user-friendly error messages
     * @param exception The exception to convert
     * @return User-friendly error message
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("already exists", ignoreCase = true) == true -> {
                "A category with this name already exists"
            }
            exception.message?.contains("network", ignoreCase = true) == true -> {
                "Network error. Please check your connection and try again."
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                "You don't have permission to add categories. Please log in again."
            }
            exception.message?.contains("invalid", ignoreCase = true) == true -> {
                "Invalid category name. Please use only letters, numbers, and spaces."
            }
            !exception.message.isNullOrBlank() -> {
                exception.message!!
            }
            else -> {
                "Failed to save category. Please try again."
            }
        }
    }

    /**
     * Clears the form after successful save
     */
    private fun clearForm() {
        _uiState.value = _uiState.value.copy(
            categoryName = "",
            errorMessage = null,
            isSaved = false
        )
    }

    /**
     * Manually clears the form (for UI reset button)
     */
    fun resetForm() {
        _uiState.value = AddCategoryUiState()
    }

    /**
     * Clears error messages (for user dismissal)
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    /**
     * Clears the saved state (for navigation or UI updates)
     */
    fun clearSavedState() {
        _uiState.value = _uiState.value.copy(
            isSaved = false
        )
    }

    /**
     * Checks if the current input is valid for saving
     * @return true if form can be saved
     */
    fun canSave(): Boolean {
        val currentState = _uiState.value
        return !currentState.isSaving &&
                currentState.categoryName.trim().isNotEmpty() &&
                currentState.categoryName.trim().length >= 2 &&
                currentState.categoryName.trim().length <= 50 &&
                currentState.errorMessage == null
    }

    /**
     * Gets the current category name for display
     */
    fun getCurrentCategoryName(): String {
        return _uiState.value.categoryName
    }

    /**
     * Checks if currently saving
     */
    fun isSaving(): Boolean {
        return _uiState.value.isSaving
    }

    /**
     * Checks if category was successfully saved
     */
    fun isSaved(): Boolean {
        return _uiState.value.isSaved
    }

    /**
     * Gets current error message
     */
    fun getErrorMessage(): String? {
        return _uiState.value.errorMessage
    }
}
