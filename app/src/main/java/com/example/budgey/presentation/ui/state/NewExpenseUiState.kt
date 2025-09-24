package com.example.budgey.presentation.ui.state

import com.example.budgey.data.model.Category
import com.google.firebase.Timestamp

/**
 * UI message types for user feedback
 */
enum class UiMessageType {
    SUCCESS,
    ERROR
}

/**
 * UI message data class
 */
data class UiMessage(
    val type: UiMessageType,
    val message: String
)

/**
 * UI state for New Expense screen with simplified state management
 */
data class NewExpenseUiState(
    // Loading states
    val isLoadingCategories: Boolean = false,

    // Data states
    val categories: List<Category> = emptyList(),

    // Message state
    val uiMessage: UiMessage? = null,

    // Form fields
    val amount: String = "",
    val selectedCategoryName: String = "",
    val description: String = "",
    val budgetStartDate: Timestamp? = null,
    val budgetEndDate: Timestamp? = null,

    // UI states
    val showCategoryPicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val isSavingExpense: Boolean = false,
)
