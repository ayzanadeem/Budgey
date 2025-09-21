package com.example.budgey.presentation.ui.state

import com.example.budgey.data.model.User

/**
 * UI state for Login screen with comprehensive state management
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    // Form fields
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
) {
    /**
     * Resets to idle state
     */
    fun reset(): LoginUiState {
        return LoginUiState()
    }

    /**
     * Determines if login was successful
     */
    val isSuccess: Boolean
        get() = !isLoading && errorMessage == null && user != null
}
