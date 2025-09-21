package com.example.budgey.domain.usecase

import android.util.Patterns
import com.example.budgey.data.model.User
import com.example.budgey.data.repository.AuthRepository
import com.example.budgey.data.repository.AuthException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for user login with input validation and business logic
 * Follows single responsibility principle for authentication
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * Executes login with email and password validation
     * @param email User's email address
     * @param password User's password
     * @return Flow emitting Result with User or validation/auth errors
     */
    operator fun invoke(email: String, password: String): Flow<Result<User>> = flow {
        // Input validation
        val validationResult = validateInput(email, password)
        if (validationResult != null) {
            emit(Result.failure(validationResult))
            return@flow
        }

        // Clean inputs
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        // Perform login
        try {
            val loginResult = authRepository.login(cleanEmail, cleanPassword)
            loginResult.fold(
                onSuccess = { user ->
                    emit(Result.success(user))
                },
                onFailure = { exception ->
                    emit(Result.failure(mapAuthException(exception)))
                }
            )
        } catch (e: Exception) {
            emit(Result.failure(LoginException.UnknownError(e.message ?: "Unknown error occurred")))
        }
    }

    /**
     * Validates login input parameters
     * @param email Email to validate
     * @param password Password to validate
     * @return LoginException if validation fails, null if valid
     */
    private fun validateInput(email: String, password: String): LoginException? {
        if (email.isBlank()) {
            return LoginException.EmptyEmail
        }

        if (!isValidEmail(email.trim())) {
            return LoginException.InvalidEmailFormat
        }

        if (password.isBlank()) {
            return LoginException.EmptyPassword
        }

        if (password.trim().length < MIN_PASSWORD_LENGTH) {
            return LoginException.PasswordTooShort
        }

        return null
    }

    /**
     * Validates email format using Android patterns
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Maps repository auth exceptions to use case specific exceptions
     */
    private fun mapAuthException(exception: Throwable): LoginException {
        return when (exception) {
            is AuthException.InvalidCredentials -> LoginException.InvalidCredentials
            is AuthException.WeakPassword -> LoginException.PasswordTooShort
            is AuthException.InvalidEmail -> LoginException.InvalidEmailFormat
            is AuthException.UserDisabled -> LoginException.AccountDisabled
            is AuthException.NetworkError -> LoginException.NetworkError
            is AuthException.TooManyRequests -> LoginException.TooManyAttempts
            else -> LoginException.UnknownError(exception.message ?: "Authentication failed")
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 4
    }
}

/**
 * Domain-specific exceptions for login use case
 */
sealed class LoginException(message: String) : Exception(message) {
    object EmptyEmail : LoginException("Email cannot be empty")
    object InvalidEmailFormat : LoginException("Please enter a valid email address")
    object EmptyPassword : LoginException("Password cannot be empty")
    object PasswordTooShort : LoginException("Password must be at least 4 characters long")
    object InvalidCredentials : LoginException("Invalid email or password")
    object AccountDisabled : LoginException("Your account has been disabled")
    object NetworkError : LoginException("Network connection failed. Please check your internet connection")
    object TooManyAttempts : LoginException("Too many login attempts. Please try again later")
    data class UnknownError(val details: String) : LoginException("Login failed: $details")
}
