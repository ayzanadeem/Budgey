package com.example.budgey.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for user authentication operations
 * Follows clean architecture principles with proper error handling
 */
interface AuthRepository {
    /**
     * Authenticates user with email and password
     * @param email User's email address
     * @param password User's password
     * @return Result containing FirebaseUser on success, exception on failure
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser>

    /**
     * Gets the currently authenticated user
     * @return Flow emitting Result with current FirebaseUser or null if not authenticated
     */
    fun getCurrentUser(): Flow<Result<FirebaseUser?>>

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

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            // First try to sign in existing user
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            // If sign in fails, try to create new user
            try {
                val createResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                Result.success(createResult.user!!)
            } catch (createException: Exception) {
                // If both operations fail, return the original sign-in error
                Result.failure(e)
            }
        }
    }

    override fun getCurrentUser(): Flow<Result<FirebaseUser?>> = flow {
        emit(Result.success(firebaseAuth.currentUser))
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
