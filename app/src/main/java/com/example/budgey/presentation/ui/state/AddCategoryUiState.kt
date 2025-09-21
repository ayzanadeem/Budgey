package com.example.budgey.presentation.ui.state

/**
 * UI state for Add Category screen with comprehensive state management
 */
data class AddCategoryUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val categoryName: String = "",
    val isSaved: Boolean = false
)
