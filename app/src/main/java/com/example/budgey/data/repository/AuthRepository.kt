package com.example.budgey.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?>
    suspend fun signUp(email: String, password: String): Result<FirebaseUser?>
    suspend fun signOut()
    fun getCurrentUser(): FirebaseUser?
    fun isUserSignedIn(): Flow<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun isUserSignedIn(): Flow<Boolean> = flow {
        emit(firebaseAuth.currentUser != null)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
