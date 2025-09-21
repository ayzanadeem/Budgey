package com.example.budgey.data.repository

import com.example.budgey.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for user authentication operations
 * Follows clean architecture principles with proper error handling and loading states
 */
interface AuthRepository {
    /**
     * Authentication state flow for observing loading states
     */
    val authState: StateFlow<AuthState>

    /**
     * Authenticates user with email and password. Creates account if it doesn't exist.
     * @param email User's email address
     * @param password User's password
     * @return Result containing domain User on success, AuthException on failure
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Gets the currently authenticated user as a reactive stream
     * @return Flow emitting domain User or null if not authenticated
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Signs out the current user
     * @return Result indicating success or failure of logout operation
     */
    suspend fun logout(): Result<Unit>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        _authState.value = AuthState.Loading

        return try {
            // First try to sign in existing user
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.toDomainUser()

            if (user != null) {
                _authState.value = AuthState.Success(user)
                Result.success(user)
            } else {
                val exception = AuthException.Unknown("User data not available")
                _authState.value = AuthState.Error(exception)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            // If sign in fails, try to create new user
            try {
                val createResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = createResult.user?.toDomainUser()

                if (user != null) {
                    _authState.value = AuthState.Success(user)
                    Result.success(user)
                } else {
                    val exception = AuthException.Unknown("User creation failed")
                    _authState.value = AuthState.Error(exception)
                    Result.failure(exception)
                }
            } catch (createException: Exception) {
                // If both operations fail, return the appropriate error
                val authException = mapFirebaseException(e)
                _authState.value = AuthState.Error(authException)
                Result.failure(authException)
            }
        }
    }

    override fun getCurrentUser(): Flow<User?> = flow {
        emit(firebaseAuth.currentUser?.toDomainUser())
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            _authState.value = AuthState.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            val authException = mapFirebaseException(e)
            _authState.value = AuthState.Error(authException)
            Result.failure(authException)
        }
    }

    /**
     * Maps Firebase exceptions to domain-specific AuthExceptions
     */
    private fun mapFirebaseException(exception: Exception): AuthException {
        return when {
            exception is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> AuthException.InvalidEmail
                    "ERROR_WRONG_PASSWORD" -> AuthException.InvalidCredentials
                    "ERROR_USER_NOT_FOUND" -> AuthException.InvalidCredentials
                    "ERROR_USER_DISABLED" -> AuthException.UserDisabled
                    "ERROR_TOO_MANY_REQUESTS" -> AuthException.TooManyRequests
                    "ERROR_WEAK_PASSWORD" -> AuthException.WeakPassword
                    "ERROR_NETWORK_REQUEST_FAILED" -> AuthException.NetworkError
                    else -> AuthException.Unknown(exception.message ?: "Unknown Firebase error")
                }
            }
            exception.message?.contains("network", ignoreCase = true) == true -> {
                AuthException.NetworkError
            }
            else -> AuthException.Unknown(exception.message ?: "Unknown error")
        }
    }
}

/**
 * Extension function to convert FirebaseUser to domain User
 */
private fun FirebaseUser.toDomainUser(): User {
    return User(
        email = this.email ?: "",
        createdAt = com.google.firebase.Timestamp.now(), // Firebase doesn't provide exact creation time
        lastLogin = com.google.firebase.Timestamp.now(),
        displayName = this.displayName,
        profileImageUrl = this.photoUrl?.toString()
    )
}
