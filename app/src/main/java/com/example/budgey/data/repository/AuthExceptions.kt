package com.example.budgey.data.repository

/**
 * Domain-specific exceptions for authentication operations
 */
sealed class AuthException(message: String) : Exception(message) {
    object InvalidCredentials : AuthException("Invalid email or password")
    object WeakPassword : AuthException("Password is too weak. Please use at least 6 characters")
    object InvalidEmail : AuthException("Please enter a valid email address")
    object UserDisabled : AuthException("This account has been disabled")
    object NetworkError : AuthException("Network connection failed. Please check your internet connection")
    object TooManyRequests : AuthException("Too many attempts. Please try again later")
    data class Unknown(val originalMessage: String) : AuthException("Authentication failed: $originalMessage")
}

/**
 * Authentication loading states
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: com.example.budgey.data.model.User) : AuthState()
    data class Error(val exception: AuthException) : AuthState()
}
