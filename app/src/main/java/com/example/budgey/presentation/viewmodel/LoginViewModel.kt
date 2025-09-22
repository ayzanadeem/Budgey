package com.example.budgey.presentation.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgey.domain.usecase.LoginUseCase
import com.example.budgey.domain.usecase.LoginException
import com.example.budgey.presentation.ui.state.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Login screen following MVVM with clean architecture
 * Handles UI state management, form validation, and authentication logic
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Handles user login with email and password
     * Manages loading states and error handling
     */
    fun login(email: String, password: String) {
        // First validate inputs
        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)

        // Update state with form validation results
        _uiState.value = _uiState.value.copy(
            email = email,
            password = password,
            emailError = emailValidation,
            passwordError = passwordValidation
        )

        // Don't proceed if validation fails
        if (emailValidation != null || passwordValidation != null) {
            return
        }

        // Start login process
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            loginUseCase(email.trim(), password.trim())
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "An unexpected error occurred. Please try again."
                    )
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { user ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                user = user,
                                errorMessage = null
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = mapErrorToUserFriendlyMessage(exception)
                            )
                        }
                    )
                }
        }
    }

    /**
     * Validates email format and returns error message if invalid
     * @param email Email to validate
     * @return Error message if invalid, null if valid
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Please enter a valid email address"
            else -> null
        }
    }

    /**
     * Validates password length and returns error message if invalid
     * @param password Password to validate
     * @return Error message if invalid, null if valid
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.trim().length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters long"
            else -> null
        }
    }

    /**
     * Clears all error messages
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            emailError = null,
            passwordError = null
        )
    }

    /**
     * Resets the UI state to initial state
     */
    fun resetState() {
        _uiState.value = LoginUiState()
    }

    /**
     * Checks if the current form is valid for submission
     */
    fun isFormValid(): Boolean {
        val currentState = _uiState.value
        return currentState.email.isNotBlank() &&
               currentState.password.isNotBlank() &&
               currentState.emailError == null &&
               currentState.passwordError == null
    }

    /**
     * Maps domain exceptions to user-friendly error messages
     * Keeps error handling logic separate from UI
     */
    private fun mapErrorToUserFriendlyMessage(exception: Throwable): String {
        return when (exception) {
            is LoginException.EmptyEmail -> "Email cannot be empty"
            is LoginException.InvalidEmailFormat -> "Please enter a valid email address"
            is LoginException.EmptyPassword -> "Password cannot be empty"
            is LoginException.PasswordTooShort -> "Password must be at least $MIN_PASSWORD_LENGTH characters long"
            is LoginException.InvalidCredentials -> "Invalid email or password. Please try again."
            is LoginException.AccountDisabled -> "Your account has been disabled. Please contact support."
            is LoginException.NetworkError -> "Network connection failed. Please check your internet connection and try again."
            is LoginException.TooManyAttempts -> "Too many login attempts. Please wait a moment and try again."
            is LoginException.UnknownError -> "Login failed. Please try again."
            else -> "An unexpected error occurred. Please try again."
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 4
    }
}
