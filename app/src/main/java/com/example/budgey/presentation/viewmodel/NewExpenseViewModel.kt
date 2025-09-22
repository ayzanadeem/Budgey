package com.example.budgey.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgey.data.model.Category
import com.example.budgey.data.model.ExpenseType
import com.example.budgey.data.repository.UserPreferencesRepository
import com.example.budgey.domain.usecase.AddExpenseUseCase
import com.example.budgey.domain.usecase.GetCategoriesUseCase
import com.example.budgey.presentation.ui.state.NewExpenseUiState
import com.example.budgey.presentation.ui.state.UiMessage
import com.example.budgey.presentation.ui.state.UiMessageType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for New Expense screen following MVVM with clean architecture
 * Handles UI state management, form validation, and expense creation logic
 */
@HiltViewModel
class NewExpenseViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewExpenseUiState())
    val uiState: StateFlow<NewExpenseUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadPersistedDates()
    }

    /**
     * Loads categories from repository with loading state management
     */
    fun loadCategories() {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = _uiState.value.copy(
                uiMessage = UiMessage(UiMessageType.ERROR, "User not authenticated")
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoadingCategories = true)

        viewModelScope.launch {
            getCategoriesUseCase(currentUserId)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingCategories = false,
                        uiMessage = UiMessage(UiMessageType.ERROR, "Failed to load categories: ${exception.message}")
                    )
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { categories ->
                            _uiState.value = _uiState.value.copy(
                                isLoadingCategories = false,
                                categories = categories,
                                uiMessage = null
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoadingCategories = false,
                                uiMessage = UiMessage(UiMessageType.ERROR, "Failed to load categories: ${exception.message}")
                            )
                        }
                    )
                }
        }
    }

    /**
     * Loads persisted budget dates from SharedPreferences or sets current date
     */
    private fun loadPersistedDates() {
        viewModelScope.launch {
            try {
                val startDate = userPreferencesRepository.getBudgetStartDate().first()
                val endDate = userPreferencesRepository.getBudgetEndDate().first()

                if (startDate != null && endDate != null) {
                    _uiState.value = _uiState.value.copy(
                        budgetStartDate = Timestamp(startDate),
                        budgetEndDate = Timestamp(endDate)
                    )
                } else {
                    // Set current date as default
                    val currentDate = Date()
                    _uiState.value = _uiState.value.copy(
                        budgetStartDate = Timestamp(currentDate),
                        budgetEndDate = Timestamp(currentDate)
                    )
                }
            } catch (e: Exception) {
                // Fallback to current date on error
                val currentDate = Date()
                _uiState.value = _uiState.value.copy(
                    budgetStartDate = Timestamp(currentDate),
                    budgetEndDate = Timestamp(currentDate)
                )
            }
        }
    }

    /**
     * Updates amount field with validation
     */
    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            uiMessage = null // Clear error when user types
        )
    }

    /**
     * Updates selected category
     */
    fun selectCategory(categoryName: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryName = categoryName,
            showCategoryPicker = false,
            uiMessage = null
        )
    }

    /**
     * Updates description field
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description
        )
    }

    /**
     * Updates budget start date and persists to preferences
     */
    fun updateBudgetStartDate(date: Timestamp) {
        _uiState.value = _uiState.value.copy(
            budgetStartDate = date,
            showDatePicker = false
        )
        persistBudgetDates()
    }

    /**
     * Updates budget end date and persists to preferences
     */
    fun updateBudgetEndDate(date: Timestamp) {
        _uiState.value = _uiState.value.copy(
            budgetEndDate = date,
            showDatePicker = false
        )
        persistBudgetDates()
    }

    /**
     * Toggles category picker visibility
     */
    fun toggleCategoryPicker() {
        _uiState.value = _uiState.value.copy(
            showCategoryPicker = !_uiState.value.showCategoryPicker
        )
    }

    /**
     * Toggles date picker visibility
     */
    fun toggleDatePicker() {
        _uiState.value = _uiState.value.copy(
            showDatePicker = !_uiState.value.showDatePicker
        )
    }

    /**
     * Saves expense with validation and error handling
     */
    fun saveExpense() {
        val currentState = _uiState.value
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            _uiState.value = currentState.copy(
                uiMessage = UiMessage(UiMessageType.ERROR, "User not authenticated")
            )
            return
        }

        // Validate form
        val validationError = validateForm(currentState)
        if (validationError != null) {
            _uiState.value = currentState.copy(
                uiMessage = UiMessage(UiMessageType.ERROR, validationError)
            )
            return
        }

        // Start saving process
        viewModelScope.launch {
            try {
                val amount = currentState.amount.toDouble()
                val selectedCategory = currentState.categories.find { it.name == currentState.selectedCategoryName }

                if (selectedCategory == null) {
                    _uiState.value = currentState.copy(
                        uiMessage = UiMessage(UiMessageType.ERROR, "Selected category not found")
                    )
                    return@launch
                }

                val result = addExpenseUseCase(
                    userId = currentUserId,
                    categoryId = selectedCategory.name, // Using name as ID for now
                    categoryName = selectedCategory.name,
                    amount = amount,
                    type = ExpenseType.EXPENSE, // Default to expense
                    description = currentState.description.takeIf { it.isNotBlank() },
                    budgetStartDate = currentState.budgetStartDate!!,
                    budgetEndDate = currentState.budgetEndDate!!
                )

                result.fold(
                    onSuccess = { expenseId ->
                        _uiState.value = currentState.copy(
                            uiMessage = UiMessage(UiMessageType.SUCCESS, "Expense saved successfully!")
                        )
                        clearForm()
                    },
                    onFailure = { exception ->
                        _uiState.value = currentState.copy(
                            uiMessage = UiMessage(UiMessageType.ERROR, exception.message ?: "Failed to save expense")
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    uiMessage = UiMessage(UiMessageType.ERROR, "An unexpected error occurred: ${e.message}")
                )
            }
        }
    }

    /**
     * Validates form inputs
     */
    private fun validateForm(state: NewExpenseUiState): String? {
        return when {
            state.amount.isBlank() -> "Amount cannot be empty"
            state.amount.toDoubleOrNull() == null -> "Please enter a valid amount"
            state.amount.toDouble() <= 0 -> "Amount must be greater than 0"
            state.selectedCategoryName.isBlank() -> "Please select a category"
            state.budgetStartDate == null -> "Please select a start date"
            state.budgetEndDate == null -> "Please select an end date"
            state.budgetStartDate!!.toDate().after(state.budgetEndDate!!.toDate()) -> "Start date must be before end date"
            else -> null
        }
    }

    /**
     * Persists budget dates to SharedPreferences
     */
    private fun persistBudgetDates() {
        val currentState = _uiState.value
        viewModelScope.launch {
            try {
                currentState.budgetStartDate?.let { startDate ->
                    userPreferencesRepository.saveBudgetStartDate(startDate.toDate())
                }
                currentState.budgetEndDate?.let { endDate ->
                    userPreferencesRepository.saveBudgetEndDate(endDate.toDate())
                }
            } catch (e: Exception) {
                // Silently handle persistence errors - not critical for UX
            }
        }
    }

    /**
     * Clears form after successful save while keeping categories and dates
     */
    private fun clearForm() {
        _uiState.value = _uiState.value.copy(
            amount = "",
            selectedCategoryName = "",
            description = "",
            showCategoryPicker = false,
            showDatePicker = false
        )
    }

    /**
     * Clears UI message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(uiMessage = null)
    }

    /**
     * Retries loading categories
     */
    fun retryLoadCategories() {
        loadCategories()
    }

    /**
     * Checks if form is valid for submission
     */
    fun isFormValid(): Boolean {
        val currentState = _uiState.value
        return validateForm(currentState) == null
    }
}
