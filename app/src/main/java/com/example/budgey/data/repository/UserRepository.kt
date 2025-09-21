package com.example.budgey.data.repository

import com.example.budgey.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    suspend fun createUser(userId: String, user: User): Result<Unit>
    suspend fun getUser(userId: String): Result<User?>
    suspend fun updateUser(userId: String, user: User): Result<Unit>
    suspend fun updateLastLogin(userId: String): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    fun getUserFlow(userId: String): Flow<User?>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    override suspend fun createUser(userId: String, user: User): Result<Unit> {
        return try {
            usersCollection.document(userId).set(user.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUser(userId: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(userId: String, user: User): Result<Unit> {
        return try {
            usersCollection.document(userId).set(user.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastLogin(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                FirestoreFields.LAST_LOGIN, Timestamp.now()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserFlow(userId: String): Flow<User?> = flow {
        try {
            val snapshot = usersCollection.document(userId).get().await()
            emit(snapshot.toUser())
        } catch (e: Exception) {
            emit(null)
        }
    }
}
